package com.zk.io.nio.tomcat.servlet;


import com.zk.io.nio.tomcat.http.GPRequest;
import com.zk.io.nio.tomcat.http.GPResponse;
import com.zk.io.nio.tomcat.http.GPServlet;

public class SecondServlet extends GPServlet {

	public void doGet(GPRequest request, GPResponse response) throws Exception {
		this.doPost(request, response);
	}

	public void doPost(GPRequest request, GPResponse response) throws Exception {
		response.write("This is Second Serlvet");
	}

}
