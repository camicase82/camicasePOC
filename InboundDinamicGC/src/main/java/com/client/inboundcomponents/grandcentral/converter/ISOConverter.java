package com.client.inboundcomponents.grandcentral.converter;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;

@MessageEndpoint
public class ISOConverter implements IConverter{

	@Transformer(inputChannel="iSOConverterChannel", outputChannel="toBSChannel")
	public String iSOConverter1(Object payload) throws Exception {
		System.out.println("Generic ISO converter");
		if (payload instanceof byte[]) {
			return new String((byte[]) payload);
		} 
		else if (payload instanceof char[]) {
			return new String((char[]) payload);
		} 
		else { 
			return payload.toString();
		} 
	}
	
	@Transformer(inputChannel="iSOConverterChannel", outputChannel="toBSChannel")
	public String iSOConverter1(byte[] payload) throws Exception {
		System.out.println("Generic ISO converter");
		if (payload instanceof byte[]) {
			return new String((byte[]) payload);
		} 
		else { 
			return payload.toString();
		} 
	}

}
