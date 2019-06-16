package com.zk.io.bio.tomcat.servlet;


import com.zk.io.bio.tomcat.http.GPRequest;
import com.zk.io.bio.tomcat.http.GPResponse;
import com.zk.io.bio.tomcat.http.GPServlet;

public class FirstServlet extends GPServlet {

	public void doGet(GPRequest request, GPResponse response) throws Exception {
		this.doPost(request, response);
	}

	public void doPost(GPRequest request, GPResponse response) throws Exception {
		response.write("This is First Serlvet");
	}

}
