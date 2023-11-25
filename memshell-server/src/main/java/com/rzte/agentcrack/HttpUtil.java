package com.rzte.agentcrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// By New Bing ~
public class HttpUtil {

    public static String sendHttpGetRequest(String url) throws IOException {
        // 创建一个URL对象
        URL obj = new URL(url);
        // 打开一个连接
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // 设置请求方法为GET
        con.setRequestMethod("GET");
        // 获取响应码
        int responseCode = con.getResponseCode();
        // 如果响应码为200，表示成功
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 创建一个缓冲读取器，读取输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            // 创建一个字符串缓冲区，存储响应体
            StringBuffer response = new StringBuffer();
            // 逐行读取响应体
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            // 关闭读取器
            in.close();
            
            return response.toString().trim();
        } else {
            // 如果响应码不为200，表示失败
            System.out.println("GET request failed: " + responseCode);
        }

        return "";
    }

    public static String sendHttpPostRequest(String url, String body) throws IOException {
        // 创建一个URL对象
        URL obj = new URL(url);
        // 打开一个连接
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // 设置请求方法为POST
        con.setRequestMethod("POST");
        // 设置请求头的内容类型为application/x-www-form-urlencoded
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // 设置请求体的长度
        con.setRequestProperty("Content-Length", String.valueOf(body.length()));
        // 设置允许输出
        con.setDoOutput(true);
        // 获取输出流
        OutputStream os = con.getOutputStream();
        // 写入请求体
        os.write(body.getBytes());
        // 关闭输出流
        os.close();
        // 获取响应码
        int responseCode = con.getResponseCode();
        // 如果响应码为200，表示成功
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 创建一个缓冲读取器，读取输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            // 创建一个字符串缓冲区，存储响应体
            StringBuffer response = new StringBuffer();
            // 逐行读取响应体
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            // 关闭读取器
            in.close();
            // 打印响应体
            return response.toString().trim();
        } else {
            // 如果响应码不为200，表示失败
            System.out.println("POST request failed: " + responseCode);
        }
        return "";
    }

    // 测试方法
    public static void main(String[] args) throws IOException {
        // 调用get方法，向百度发送请求
        sendHttpGetRequest("https://www.baidu.com");
        // 调用post方法，向本地服务器发送请求
        sendHttpPostRequest("http://127.0.0.1:8080/demo/NoAgent.jsp", "1234567");
    }
}

