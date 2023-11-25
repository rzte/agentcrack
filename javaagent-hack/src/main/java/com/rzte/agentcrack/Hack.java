package com.rzte.agentcrack;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

/**
 * Hello world!
 *
 */
public class Hack 
{
    public static void agentmain(String agentOps, Instrumentation inst) {
        System.out.println("start hack class T, method: " + agentOps);

        Class t;
        try {
            t = Class.forName("com.rzte.agentcrack.T");
        }catch(Exception e){
            e.printStackTrace();
            return;
        }

        if ("redefine".equals(agentOps)){ // redefineClasses
            try {
                inst.redefineClasses(new ClassDefinition[]{
                    new ClassDefinition(t, HackTransformer.evilClassBytes())
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{ // default, retransformClasses
            ClassFileTransformer h = new HackTransformer("com/rzte/agentcrack/T");
            inst.addTransformer(h, true);
            try {
                inst.retransformClasses(t);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
	}

	private static void attach(String pid, String method) throws Exception{
		String selfPath = Hack.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println("path: " + selfPath);

		Class vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
		Method vmAttach = vmClass.getMethod("attach", String.class);

		Method vmLoadAgent = vmClass.getMethod("loadAgent", String.class, String.class);
		Method vmDetach = vmClass.getMethod("detach");

		Object vm = vmAttach.invoke(null, pid);
		vmLoadAgent.invoke(vm, selfPath, method);
		vmDetach.invoke(vm);
	}

	public static void main(String args[]) throws Exception{
		if (args.length != 2){
			System.out.println("Usage: java -jar javaagent.jar <target_pid> redefine/retransform");
			return;
		}
		String pid = args[0];
        String method = args[1];
		System.out.printf("will attach %s, method: %s\n", pid, method);
		
		attach(pid, method);
	}
}
