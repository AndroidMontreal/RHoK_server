package com.androidmontreal.rhok.server;

import org.eclipse.jetty.embedded.SecuredHelloHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.androidmontreal.rhok.server.hibernate.HibernateUtil;
import com.google.inject.servlet.GuiceFilter;

/**
 * <p>Based on examples and tutorials found on the web.
 * <p>The security part of this is inspired from {@link SecuredHelloHandler} in example-jetty-embedded.
 * 
 */
public class Launcher {
	
	public static void main(String[] args) throws Exception {
		//FIXME: Quick hack for PoC [review]
		HibernateUtil.initSessionFactory();
		
		// Create the server.
		Server server = new Server(8080);
		
		// ** SERVLET CONTEXT HANDLER
		// Create a servlet context and add the jersey servlet.
		ServletContextHandler sch = new ServletContextHandler(server, "/");
		
		// Add our Guice listener that includes our bindings
		sch.addEventListener(new RHoKServletConfig());
		
		// Then add GuiceFilter and configure the server to
		// reroute all requests through this filter.
		sch.addFilter(GuiceFilter.class, "/*", null);
		
		
		// Must add DefaultServlet for embedded Jetty.
		// Failing to do this will cause 404 errors.
		// This is not needed if web.xml is used instead.
		sch.addServlet(DefaultServlet.class, "/");
		
		// ** FILE SERVER HANDLER
		
		// Originally included to serve a test AJAX web client. 
		// NOTE: Only include this if you serve *actual resources*.
//		ResourceHandler resourceHandler = new ResourceHandler();
//		resourceHandler.setDirectoriesListed(true);
//		resourceHandler.setWelcomeFiles( new String[] { "sandbox2.html" });
//		URL webURL = Launcher.class.getResource("/web/");
//		resourceHandler.setResourceBase(webURL.getPath());
		
		// ** COMBINE HANDLERS [Only really needed if FILE SERVER is enabled above.]
		
		HandlerList handlerList = new HandlerList();
//		handlerList.addHandler(resourceHandler);
		handlerList.addHandler(sch);
		
		server.setHandler(handlerList);
		
		// Start the server
		server.start();
		server.join();
	}
	
}