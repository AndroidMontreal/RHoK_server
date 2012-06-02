package com.androidmontreal.rhok.server;

import com.androidmontreal.rhok.server.hibernate.TransactionInterceptor;
import com.androidmontreal.rhok.server.hibernate.Transactionnal;
import com.androidmontreal.rhok.server.service.AuthenticationWebService;
import com.androidmontreal.rhok.server.service.UserManagement;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * <p>This is our master bean-wiring.
 * <p>TODO: More doc explaining the nuts and bolts of guice, jersey and jetty.
 */
public class RHoKServletConfig extends GuiceServletContextListener {
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new JerseyServletModule() {
			@Override
			protected void configureServlets() {
				// @Transactionnal configuration.
				// TODO: Use module instead if possible. It would be cleaner I think. (Need to read more on this...)
				TransactionInterceptor interceptor = new TransactionInterceptor();
				requestInjection(interceptor);
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactionnal.class), interceptor);
				
				// Register the management services
				bind(UserManagement.class);
				bind(AuthenticationWebService.class);

				// Important. Jersey hookup.
				bind(GuiceContainer.class);
				
				// Route all requests through GuiceContainer.
				serve("/*").with(GuiceContainer.class);
			}
		});
	}

	/**
				// Old examples, once you got a good grasp on Guice, remove this test junk.
				// Admin test
//				bind(AdminGuicyServiceWrapper.class);
//				bind(AdminGuicyInterfaceImpl.class);

				// Must configure at least one JAX-RS resource or the server
				// will fail to start.
//				bind(GuicyServiceWrapper.class);

				// Then bind what implementation we want to GuicyInterface.
//				bind(GuicyInterface.class).to(GuicyInterfaceImpl.class);

	 */
}