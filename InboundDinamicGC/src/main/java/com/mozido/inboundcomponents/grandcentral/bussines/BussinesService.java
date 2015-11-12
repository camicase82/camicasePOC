package com.mozido.inboundcomponents.grandcentral.bussines;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;


@MessageEndpoint
public class BussinesService {
	
	public BussinesService(){
		System.out.println("contrucvtor BussinesService llamado");
	}
	
	@ServiceActivator(inputChannel="toBSChannel")
	public String processIncomingMessage(String message) {
		System.out.println("Bussines logic: " + message);
		
		return "respondemos:" + message;
		
	}

}