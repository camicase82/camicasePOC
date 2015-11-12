package com.client.inbound.grandcentral.service;

import java.io.File;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.stereotype.Component;

import com.client.inboundcomponents.grandcentral.si.SIBeansFactory;

@Component
@Scope("singleton")
public class SIContextLoader {

	@Autowired
	private ConfigurableApplicationContext appContext;

	private SIBeansFactory siBeanFactory;

	@PostConstruct
	public void init() {

		URL url = appContext.getClassLoader()
				.getResource("applicationContext.xml");
		System.out.println(url.getPath());

		final GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		context.load("applicationContext.xml");
		context.setParent(appContext);
		context.registerShutdownHook();
		context.refresh();

		siBeanFactory = context.getBean(SIBeansFactory.class);
		/*
		 * DynamicApplication dynamicApplication = context
		 * .getBean(DynamicApplication.class); for (int i = 0; i < 1; i++) {
		 * TcpInboundGateway listener = dynamicApplication
		 * .createTcpInboundGateway("server" + i, 9877 + i); listener.start(); }
		 */
	}

	public void loadJar(File file) {
		siBeanFactory.loadJar(file);
	}

}
