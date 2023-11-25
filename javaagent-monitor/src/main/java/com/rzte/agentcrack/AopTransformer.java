package com.rzte.agentcrack;

import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Random;

public class AopTransformer implements ClassFileTransformer {
    private String target;
    private boolean cached;

    public AopTransformer(String target, boolean cached){
        this.target = target;
        this.cached = cached;
    }

    private Random random = new Random();

    private void write(String name, byte[] data){
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(name);
            fileOutputStream.write(data);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (fileOutputStream != null){
                try{
                    fileOutputStream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (target.equals(className)){
            String fileName = String.format("/tmp/T.%x.class", random.nextInt(0xffff));
            System.out.printf("class %s byte size %d, write to %s\n", target, classfileBuffer.length, fileName);
            write(fileName, classfileBuffer);
            if (cached){
                return classfileBuffer;
            }
        }
        
        return null;
    }
}
