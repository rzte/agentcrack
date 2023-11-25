package com.rzte.agentcrack;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println("Hello World!");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++){
                    T.run(i);
                    try{
                        Thread.sleep(1000);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            
        });
        t.start();

        if (args.length == 1){ // 模拟引入指定类
            try {
                System.out.printf("will load class %s, please input 'Enter' key\n", args[0]);
                System.in.read();
                Class.forName(args[0]);
            }catch(Exception e){

            }
        }
    }
}