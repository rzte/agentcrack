package com.rzte.agentcrack;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Base64;

public class HackTransformer implements ClassFileTransformer {
    private String target;
    private final static String evilClassT = "yv66vgAAADQAKAoABAAVCQAWABcIABgHABkKABoAGwoAHAAdBwAeAQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBABdMY29tL3J6dGUvYWdlbnRjcmFjay9UOwEAA3J1bgEABChJKVYBAAFpAQABSQEAClNvdXJjZUZpbGUBAAZULmphdmEMAAgACQcAHwwAIAAhAQBIdGhlIGNsYXNzIGhhcyBiZWVuIGhhY2sgPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PiAlMmQKAQAQamF2YS9sYW5nL09iamVjdAcAIgwAIwAkBwAlDAAmACcBABVjb20vcnp0ZS9hZ2VudGNyYWNrL1QBABBqYXZhL2xhbmcvU3lzdGVtAQADb3V0AQAVTGphdmEvaW8vUHJpbnRTdHJlYW07AQARamF2YS9sYW5nL0ludGVnZXIBAAd2YWx1ZU9mAQAWKEkpTGphdmEvbGFuZy9JbnRlZ2VyOwEAE2phdmEvaW8vUHJpbnRTdHJlYW0BAAZwcmludGYBADwoTGphdmEvbGFuZy9TdHJpbmc7W0xqYXZhL2xhbmcvT2JqZWN0OylMamF2YS9pby9QcmludFN0cmVhbTsAIQAHAAQAAAAAAAIAAQAIAAkAAQAKAAAALwABAAEAAAAFKrcAAbEAAAACAAsAAAAGAAEAAAADAAwAAAAMAAEAAAAFAA0ADgAAAAkADwAQAAEACgAAAEMABgABAAAAFbIAAhIDBL0ABFkDGrgABVO2AAZXsQAAAAIACwAAAAoAAgAAAAYAFAAHAAwAAAAMAAEAAAAVABEAEgAAAAEAEwAAAAIAFA==";

    public HackTransformer(String target){
        this.target = target;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (target.equals(className)){
            System.out.println("hack: will hack the class: " + className);
            return Base64.getDecoder().decode(evilClassT);
        }
        
        return null;
    }

    public static byte[] evilClassBytes(){
        return Base64.getDecoder().decode(evilClassT);
    }

}
