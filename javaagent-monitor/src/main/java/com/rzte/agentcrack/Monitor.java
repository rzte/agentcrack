package com.rzte.agentcrack;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;


/**
 * Description:
 *
 * @author cincly
 * @version 1.0
 * @since 2023.10.2
 */
public class Monitor {
	public static void premain(String agentOps, Instrumentation inst) {
		instrument("premain", agentOps, inst);
	}

	public static void agentmain(String agentOps, Instrumentation inst) {
		instrument("agentmain", agentOps, inst);
	}

	private static void instrument(String method, String agentOps, Instrumentation inst) {
		System.out.printf("%s agentOps: %s\n", method, agentOps);

		String cached = "false";
		String targetClass = "com.rzte.agentcrack.T";
		if (agentOps != null){
			String[] arr = agentOps.split(":");
			if (arr.length == 2){
				cached = arr[0];
				targetClass = arr[1].trim();
			}
		}

		ClassFileTransformer aop = new AopTransformer(targetClass.replace(".", "/"), "true".equals(cached));
		inst.addTransformer(aop, true);
		if ("agentmain".equals(method)){
			Class[] classes = inst.getAllLoadedClasses();
			try {
				for (int i = 0; i < classes.length; i++){
					if (classes[i].getName().equals(targetClass)){
						System.out.println("will retransform class: " + classes[i].getName());
						inst.retransformClasses(classes[i]);
						break;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			inst.removeTransformer(aop);
		}
	}

	private static void attach(String pid, boolean cached, String targetClass) throws Exception{
		String selfPath = Monitor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println("path: " + selfPath);

		Class vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
		Method vmAttach = vmClass.getMethod("attach", String.class);

		Method vmLoadAgent = vmClass.getMethod("loadAgent", String.class, String.class);
		Method vmDetach = vmClass.getMethod("detach");

		Object vm = vmAttach.invoke(null, pid);
		vmLoadAgent.invoke(vm, selfPath, String.valueOf(cached) + ":" + targetClass);
		vmDetach.invoke(vm);
	}

	public static void main(String args[]) throws Exception{
		if (args.length != 1 && args.length != 2 && args.length != 3){
			System.out.println("Usage: java -jar javaagent.jar <target_pid> [cached] [target_class]");
			System.out.println("target_class default: com.rzte.agentcrack.T");
			return;
		}
		String pid = args[0];
		boolean cached = false;
		String targetClass = "com.rzte.agentcrack.T";

		for (int i = 1; i < args.length; i++){
			if ("cached".equals(args[i])){
				cached = true;
			}else{
				targetClass = args[i];
			}
		}

		System.out.printf("will attach %s, cached: %s\n", pid, cached);
		
		attach(pid, cached, targetClass);
	}
}