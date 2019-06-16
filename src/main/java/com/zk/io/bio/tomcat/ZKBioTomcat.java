package com.zk.io.bio.tomcat;

import com.zk.io.bio.tomcat.http.GPRequest;
import com.zk.io.bio.tomcat.http.GPResponse;
import com.zk.io.bio.tomcat.http.GPServlet;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @program: gupao_dc
 * @description:
 * @author: zk
 * @create: 2019-06-16 12:06
 **/
public class ZKBioTomcat {
    private int port = 8080;
    private ServerSocket server;
    private Map<String,GPServlet> servletMapping =new HashMap<String,GPServlet>();

    private Properties webxml = new Properties();

    private void init(){
        try{
            //加载web.xml文件，同时初始化servletMapping对象
            String WEB_INF = this.getClass().getResource("/").getPath();
            FileInputStream fis = new FileInputStream(WEB_INF + "web.properties");

            webxml.load(fis);
            for(Object obj :webxml.keySet()){
                String key=obj.toString();
                if(key.endsWith(".url")) {
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = webxml.getProperty(key);
                    String className = webxml.getProperty(servletName + ".className");
                    GPServlet obj1 = (GPServlet)Class.forName(className).newInstance();
                    servletMapping.put(url, obj1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start(){

        //1、加载配置文件，初始化ServeltMapping
        init();
        try {
            server = new ServerSocket(this.port);

            System.out.println("ZK Tomcat 已启动，监听的端口是：" + this.port);

            //2、等待用户请求,用一个死循环来等待用户请求
            while (true) {
                Socket client = server.accept();
                //4、HTTP请求，发送的数据就是字符串，有规律的字符串（HTTP协议）
                process(client);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(Socket client) throws Exception {
        InputStream is = client.getInputStream();
        OutputStream os = client.getOutputStream();

        //7、Request(InputStrean)/Response(OutputStrean)
        GPRequest request = new GPRequest(is);
        GPResponse response = new GPResponse(os);

        //5、从协议内容中拿到URL，把相应的Servlet用反射进行实例化
        String url = request.getUrl();

        if(servletMapping.containsKey(url)){
            //6、调用实例化对象的service()方法，执行具体的逻辑doGet/doPost方法
            servletMapping.get(url).service(request,response);
        }else{
            response.write("404 - Not Found");
        }

        os.flush();
        os.close();

        is.close();
        client.close();
    }

    public static void main(String[] args) {
        new ZKBioTomcat().start();
    }
}
