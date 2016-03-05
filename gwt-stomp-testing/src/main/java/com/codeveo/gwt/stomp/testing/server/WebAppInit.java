package com.codeveo.gwt.stomp.testing.server;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class WebAppInit implements WebApplicationInitializer {


    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.setId(getRootApplicationContextId(servletContext));
        rootContext.register(WebSocketConfig.class);

        servletContext.addListener(new ContextLoaderListener(rootContext));

        ServletRegistration.Dynamic registration = servletContext.addServlet("springDispatcher", new DispatcherServlet(rootContext));
        registration.setLoadOnStartup(0);
        registration.addMapping("/stomp/*");
    }

    public static String getRootApplicationContextId(ServletContext servletContext) {
        return servletContext.getContextPath() + "/rootContext";
    }
}
