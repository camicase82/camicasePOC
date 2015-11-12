package com.client.inbound.grandcentral.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.client.inbound.grandcentral.service.SIContextLoader;

/**
 * Servlet implementation class ReloadPageServlet
 */

@Controller

public class DirectoryController  {
	
	@Autowired
	private SIContextLoader siBeansFactory;
	
	@Autowired
	private ConfigurableApplicationContext appContext;
	
	private static final long serialVersionUID = 1L;
	
	
	@RequestMapping("/dinamicSI")
	public ModelAndView showMessage() throws IOException{
		
		String path = "D:/DEVELOPMENT/TMP/";
		File f = new File(path);																				// directory
		File[] files = f.listFiles();
		
		String returnValue = "";
		URL urls[] = {};
		//JarFileLoader cl = new JarFileLoader(urls);
		
		returnValue += "<UL>";
		for (File file : files) {
			//cl.loadJar(file.getAbsolutePath());
			//SimpleJarLoader.loadJar(file, appContext);
			siBeansFactory.loadJar(file);
			//siBeansFactory.addClassesFromAnnotatedPackage("com.mozido.inboundtest.newclass");
			//file.toURI().toURL()
			if (file.getCanonicalPath().contains(".jar")){
				returnValue += "<LI>";
				returnValue += file.getCanonicalPath().replace(path, "");
				returnValue += "<br>";
			}
		}
		returnValue += "</UL>";
		
 
		ModelAndView mv = new ModelAndView("helloworld");
		mv.addObject("message", "Loaded classes");
		mv.addObject("name", returnValue);
		return mv;
	}

	
}
