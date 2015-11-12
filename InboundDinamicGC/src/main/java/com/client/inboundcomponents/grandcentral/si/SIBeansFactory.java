package com.client.inboundcomponents.grandcentral.si;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.client.inboundcomponents.grandcentral.converter.IConverter;

@Component
public class SIBeansFactory {

	@Autowired
	private ConfigurableApplicationContext appContext;
	
	private Map<String, String> channelsMap = new HashMap<String, String>();
	
	private Map<String, String> inboundGWMap = new HashMap<String, String>();

	@PostConstruct
	public void init() throws Exception{
		
		setupContext();
	}
	
	/**
	 * Loads the classes found in a given jar file define as a File pointer
	 * into the spring context, it scans the anottations, and initializes
	 * all requiered beans
	 * @param file
	 */
	public void loadJar(File file) {
		try {
			JarFile jar = new JarFile(file);
			URL[] urls = { new URL("jar:" + file.toURI().toURL() + "!/") };

			// It is important to set the parent ClassLoader in order to make
			// sure that the new plugins
			// and their folders will be added to the classpath correctly
			URLClassLoader cl = new URLClassLoader(urls, appContext.getClassLoader());

			Enumeration<JarEntry> e = jar.entries();
			while (e.hasMoreElements()) {
				JarEntry entry = (JarEntry) e.nextElement();

				if (entry.getName().endsWith(".class") && !entry.getName().contains("IConverter")) {
					String classPath = entry.getName().replaceAll("/", ".");
					classPath = classPath.substring(0, classPath.lastIndexOf("."));
					String className = classPath.substring(classPath.lastIndexOf(".") + 1);
					
					System.out.println("Loading: " + classPath);
					Class<?> clazz = cl.loadClass(classPath);
					BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(clazz);
					builder.setScope(BeanDefinition.SCOPE_SINGLETON);
					//bd.setScope(BeanDefinition.SCOPE_SINGLETON);
					DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) appContext).getBeanFactory();
					beanFactory.registerBeanDefinition(className, builder.getBeanDefinition());
					((ConfigurableApplicationContext) appContext).getBean(className);
					
				}

			}
			//classLoader.
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper method to add clases from packages to spring context that weren't loaded at startup
	 * @param packageName
	 */
	public void addClassesFromAnnotatedPackage(String packageName){
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
		  scanner.setResourceLoader(new PathMatchingResourcePatternResolver(appContext.getClassLoader())); 
		  scanner.addIncludeFilter(new AssignableTypeFilter(IConverter.class));
		  //scanner.addIncludeFilter(new AnnotationTypeFilter(MappedSuperclass.class));
		  for (  BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
		    String className=bd.getBeanClassName();
		    Class<?> type=ClassUtils.resolveClassName(className, appContext.getClassLoader());
		    //addAnnotatedClass(type);
		    System.out.println("Added class " + className);
		    bd.setScope(BeanDefinition.SCOPE_SINGLETON);
		    DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) appContext).getBeanFactory();
		    beanFactory.registerBeanDefinition(className, bd);
		    //When I ask sroping context for the bean, it actually initilizes it!!
		    Object obj = ((ConfigurableApplicationContext) appContext).getBean(className);
		    System.out.println(((ConfigurableApplicationContext) appContext).containsBean(className));
		  }
		}
	
	@Autowired
	Environment env;
	
	/**
	 * Helper method to create channels dynamically
	 * @param name
	 * @return
	 */
	public PublishSubscribeChannel createChannelBean(String name){
		if(!channelsMap.containsKey(name)){
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(PublishSubscribeChannel.class);
			builder.addPropertyValue("beanName", name);
			builder.addPropertyValue("beanFactory", appContext);
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) appContext).getBeanFactory();
			beanFactory.registerBeanDefinition(name, builder.getBeanDefinition());
			channelsMap.put(name, name);
		}
		return (PublishSubscribeChannel) ((ConfigurableApplicationContext)appContext).
				getBeanFactory().getBean(name); 
	}
	
	/**
	 * Helper method to create inbound gateway dynamically
	 * @param name
	 * @param port
	 * @return
	 */
	public TcpInboundGateway createTcpInboundGateway(String name, int port){
		if(!inboundGWMap.containsKey(name)){
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((ConfigurableApplicationContext) appContext).getBeanFactory();
			//first we crate the definition for the connection factory
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TcpNetServerConnectionFactory.class);
			builder.addConstructorArgValue(port);
			builder.addPropertyValue("beanName", name + "ConnFactory");
			
			beanFactory.registerBeanDefinition(name + "ConnFactory", builder.getBeanDefinition());
			
			builder = BeanDefinitionBuilder.rootBeanDefinition(TcpInboundGateway.class);
			builder.addPropertyReference("connectionFactory", name + "ConnFactory");  // add dependency to other bean
			builder.addPropertyReference("requestChannel", "iSOConverterChannel");      // set property value
			beanFactory.registerBeanDefinition(name, builder.getBeanDefinition());
			
			inboundGWMap.put(name, name);
		}
		return (TcpInboundGateway) ((ConfigurableApplicationContext)appContext).
				getBeanFactory().getBean(name); 
	}
	
	//TCP listener Done
	
	public void setupContext() {
		
//		this.createChannelBean("toBSChannel");
//		this.createChannelBean("iSOConverterChannel");
//		this.createChannelBean("restRequestChannel");
		//we'll try to load n incoming ports
		//for(int i = 0 ; i < 1 ; i++){
		
			TcpInboundGateway listener = this.createTcpInboundGateway("server0", 9877);
			listener.start();
		//}
		//then we define the channels that re going to be used
		//TODO If we put the channels by hand, the newly added beans aren't catched, if I removed, the systems finds the new beans
		
	}
}
