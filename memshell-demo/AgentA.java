import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import sun.misc.Unsafe;


class R implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("transform class ==============> " + className);
        if ("com/rzte/agentcrack/T".equals(className)){
            String data = "yv66vgAAADQAKAoABAAVCQAWABcIABgHABkKABoAGwoAHAAdBwAeAQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBABdMY29tL3J6dGUvYWdlbnRjcmFjay9UOwEAA3J1bgEABChJKVYBAAFpAQABSQEAClNvdXJjZUZpbGUBAAZULmphdmEMAAgACQcAHwwAIAAhAQBIdGhlIGNsYXNzIGhhcyBiZWVuIGhhY2sgPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PiAlMmQKAQAQamF2YS9sYW5nL09iamVjdAcAIgwAIwAkBwAlDAAmACcBABVjb20vcnp0ZS9hZ2VudGNyYWNrL1QBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQARamF2YS9sYW5nL0ludGVnZXIBAAd2YWx1ZU9mAQAWKEkpTGphdmEvbGFuZy9JbnRlZ2VyOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAZwcmludGYBADwoTGphdmEvbGFuZy9TdHJpbmc7W0xqYXZhL2xhbmcvT2JqZWN0OylMamF2YS9pby9QcmludFN0cmVhbTsAIQAHAAQAAAAAAAIAAQAIAAkAAQAKAAAALwABAAEAAAAFKrcAAbEAAAACAAsAAAAGAAEAAAADAAwAAAAMAAEAAAAFAA0ADgAAAAkADwAQAAEACgAAAEMABgABAAAAFbIAAhIDBL0ABFkDGrgABVO2AAZXsQAAAAIACwAAAAoAAgAAAAYAFAAHAAwAAAAMAAEAAAAVABEAEgAAAAEAEwAAAAIAFA==";
            System.out.println("will hook class ======================> " + className);
            return Base64.getDecoder().decode(data);
        }
        
        return null;
    }
}


/**
 * 
 *
 */
public class AgentA
{
    private Unsafe unsafe;
    private int addressSize;
    private long arrayBaseOffset;
    private String jvmPath;
    private long jvmBaseAddr;
    private String javaVersion;
    private HashMap<String, Long> symbols;

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
    
    public Instrumentation createNewInst(ClassFileTransformer transformer) throws Exception {
        // fetch jvm from global variable: main_vm
        long mainVMOffset = find_symbol("main_vm");
        System.out.printf("main_vm offset: 0x%x\n", mainVMOffset);

        long mainVMPointerAddr = jvmBaseAddr + mainVMOffset;
        // long mainVMAddr = unsafe.getLong(mainVMPointerAddr);
        long mainVMAddr = mainVMPointerAddr;
        long ptrSize = 8;
        long agentAddr = unsafe.allocateMemory(200); // _JPLISAgent
        unsafe.setMemory(agentAddr, 200L, (byte)0);

        Instrumentation instrumentation = null;
        Long instrumentationAddr = 0L;
        Class clazz = Class.forName("sun.instrument.InstrumentationImpl");
        try{
            Constructor constructor = clazz.getDeclaredConstructor(long.class, boolean.class, boolean.class);
            constructor.setAccessible(true);
            instrumentation = (Instrumentation) constructor.newInstance(agentAddr, true, false);
            System.gc();
            Thread.sleep(3000);
            instrumentationAddr = getObjectAddress(instrumentation);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        System.out.printf("main_vm addr: 0x%x, agent addr: 0x%x, instImpl addr: 0x%x\n", mainVMAddr, agentAddr, instrumentationAddr);
        unsafe.putLong(agentAddr, mainVMAddr);

        unsafe.putLong(agentAddr + 2 * ptrSize, agentAddr); // corresponding agent: agent->mNormalEnvironment.mAgent
        unsafe.putLong(agentAddr + 5 * ptrSize, agentAddr); // corresponding agent: agent->mRetransformEnvironment.mAgent
        unsafe.putLong(agentAddr + 6 * ptrSize, 1L); // agent->mRetransformEnvironment.mIsRetransformer = true

        long instPointerAddr = unsafe.allocateMemory(128);
        System.out.printf("inst pointer addr: 0x%x\n", instPointerAddr);
        unsafe.putLong(instPointerAddr, instrumentationAddr);
        unsafe.putLong(agentAddr + 7 * ptrSize, instPointerAddr); // instImpl jobject

        // TODO: add jmethodID for agent->mTransform
        // reference: JPLISAgent.c: transformClassFile
        System.out.println("will set method id");
        long jmethodID = getTransformMethodId(instrumentation);
        System.out.printf("transform methodID is: 0x%x\n", jmethodID);
        unsafe.putLong(agentAddr + 10 * ptrSize, jmethodID); // write transform jmethodID to agent->mTransform

        System.out.println("will add re transformer");

        instrumentation.addTransformer(transformer, true);
        
        // Under normal circumstances, the mJVMTIEnv of mNormalEnvironment and mTransformEnvironment are different, but we are only here to construct...
        // update agent->mNormalEnvironment.mJVMTIEnv for 
        //    JPLISAgent.c: transformClassFile
        //      Reentrancy.c: tryToAcquireReentrancyToken
        long jvmtiEnvAddr = unsafe.getLong(agentAddr + 4 * ptrSize); // get from agent->mRetransformEnvironment.mJVMTIEnv
        unsafe.putLong(agentAddr + 1 * ptrSize, jvmtiEnvAddr); // write to agent->mNormalEnvironment.mJVMTIEnv

        return instrumentation;
    }

    /**
     * get transform jmethod from InstrumentationImpl instanceKlass
     * @param instrumentation
     * @return
     */
    public long getTransformMethodId(Object instrumentation) throws Exception{
        long metadata = unsafe.getLong(instrumentation, 8L); // oopDesc._metadata
        System.out.printf("metadata addr: 0x%x\n", metadata);

        long klassAddr = 0;
        boolean useCompressed = true; // default use compressed

        long useCompressedClassPointierOffset = find_symbol("^UseCompressedClassPointers$");
        if (useCompressedClassPointierOffset != 0){
            byte useCompressedClass = unsafe.getByte(jvmBaseAddr + useCompressedClassPointierOffset);
            if (useCompressedClass == 0){
                useCompressed = false;
            }
        }

        if (useCompressed){
            long narroKlassOffset = find_symbol("^.*Universe.*_narrow_klassE.*$");
            long narroKlassAddr = narroKlassOffset + jvmBaseAddr;
            System.out.printf("narroKlass offset: 0x%x, addr: 0x%x\n", narroKlassOffset, narroKlassAddr);

            long narroKlassBase = unsafe.getLong(narroKlassAddr);
            int narroKlassShift = unsafe.getInt(narroKlassAddr + 8);
            System.out.printf("narro_klass._base: 0x%x, narro_klass._shift: %d\n", narroKlassBase, narroKlassShift);

            klassAddr = narroKlassBase +  ((metadata & 0xffffffffL) << narroKlassShift); // type: InstanceKlass*
        }else {
            klassAddr = metadata;
        }


        System.out.printf("klass addr: 0x%x\n", klassAddr);

        long methodsOffset = 0x180L;

        long minorOffset = 0x108;
        short minorVersion = -1;
        short majorVersion = -1;
        { // find methods addr
            int[] ts = { 0, -2, 2, -4, 4, -6, 6, -8, 8, 10, -10, 12, -12 };
            boolean found = false;
            for (int i = 0; i < ts.length; i++){
                minorVersion = unsafe.getShort(klassAddr + minorOffset + ts[i]);

                if (minorVersion != 0){
                    continue;
                }
                majorVersion = unsafe.getShort(klassAddr + minorOffset + 2 + ts[i]);
                if (majorVersion < 40 || majorVersion > 70){
                    continue;
                }
                minorOffset += ts[i];

                found = true;
                break;
            }
            
            if (!found){
                throw new Exception("not found the methods addr");
            }

            methodsOffset = minorOffset + 0x74;
        }

        // Data Alignment
        if (methodsOffset % 8 != 0){
            methodsOffset = 8 * ((methodsOffset / 8) + 1);
        }

        System.out.printf("minor offset: 0x%x, major version: %d, minor version: %d, will use the methods offset: 0x%x\n", minorOffset, majorVersion, minorVersion, methodsOffset);

        boolean notProduct = false;
        long methodsAddr = unsafe.getLong(klassAddr + methodsOffset);// methods
        if (methodsAddr == 0){
            notProduct = true;
            methodsOffset -= 8;
            System.out.printf("InstanceKlass field '_verify_count' maybe not exist, which mean the NOT_PRODUCT(code) is invalid, reuse methods offset: 0x%x\n", methodsOffset);
            methodsAddr = unsafe.getLong(klassAddr + methodsOffset); // methods
        }

        System.out.printf("methods addr: 0x%x\n", methodsAddr);
        long methodCount = unsafe.getLong(methodsAddr + 0L); // methods length
        long methodDataAddr = methodsAddr + 8;
        System.out.printf("methodCount: 0x%x\n", methodCount);

        int idx = -1;
        for (int i = 0; i < methodCount; i++){
            long methodAddr = unsafe.getLong(methodDataAddr + i * 8L); // gdb: p ((*((InstanceKlass*)0x100038468)._methods)._data[i])

            long constMethodAddr = 0;
            if (notProduct) {
                constMethodAddr = unsafe.getLong(methodAddr + 0x8L); // ._constMethod
            }else{
                constMethodAddr = unsafe.getLong(methodAddr + 0x10L); // ._constMethod, NOT_PRODUCT(code) is valid
            }

            if (constMethodAddr == 0){
                continue;
            }

            short n = unsafe.getShort(constMethodAddr + 0x2c); // ._size_of_parameters
            System.out.printf("\n%d size_of_parameters: %d\n", i, n);
            if (n != 7) {
                continue;
            }

            idx = i;
            System.out.printf("the transform method is %dth\n", i);
            break;
        }

        if (idx == -1){
            throw new Exception("not found the transform method");
        }
        
        return methodDataAddr + idx * 8L;
    }
    
    public void hack() throws Exception
    {
        if (symbols.size() == 0 || addressSize != 8 || !javaVersion.startsWith("1.8")){
            System.out.println("unsupport java version");
            return;
        }

        R r = new R();
        Instrumentation instrumentation = createNewInst(r);
        System.out.println("add transform succeed!");
        System.out.printf("instrumentation object addr: 0x%x\n", getObjectAddress(instrumentation));

        System.out.println("will retransform class: T");
        Class t = Class.forName("com.rzte.agentcrack.T");
        instrumentation.retransformClasses(t);
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

    static {
        new AgentA().start();
    }
}