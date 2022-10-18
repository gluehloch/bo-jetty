package de.betoffice.jetty;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.NoJspServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class BetofficeJettyStarterMain {

    private static final int PORT = 9290;

    private static final String CONTEXT_PATH = "/";
    private static final String CONFIG_LOCATION_PACKAGE = "de.betoffice";
    private static final String MAPPING_URL = "/";
    private static final String WEBAPP_DIRECTORY = "WEB-INF"; // "webapp";

    public static void main(String[] args) throws Exception {
        new BetofficeJettyStarterMain().startJetty(PORT);
    }

    private void startJetty(int port) throws Exception {
        System.out.printf("Starting server at port {}", port);
        Server server = new Server(port);

        server.setHandler(getServletContextHandler());

        addRuntimeShutdownHook(server);

        server.start();
        System.out.printf("Server started at port {}", port);
        server.join();
    }

    private static ServletContextHandler getServletContextHandler() throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS); 
        contextHandler.setErrorHandler(null);

        contextHandler.setResourceBase(new ClassPathResource(WEBAPP_DIRECTORY).getURI().toString());
        contextHandler.setContextPath(CONTEXT_PATH);

        // JSP
        // contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        // contextHandler.addServlet("jsp", J);

        // Spring
        WebApplicationContext webAppContext = getWebApplicationContext();
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webAppContext);
        ServletHolder springServletHolder = new ServletHolder("mvc-dispatcher", dispatcherServlet);
        contextHandler.addServlet(springServletHolder, MAPPING_URL);
        contextHandler.addEventListener(new ContextLoaderListener(webAppContext));

        return contextHandler;
    }

    private static WebApplicationContext getWebApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(CONFIG_LOCATION_PACKAGE);
        return context;
    }

    private static void addRuntimeShutdownHook(final Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (server.isStarted()) {
                    server.setStopAtShutdown(true);
                    try {
                        server.stop();
                    } catch (Exception e) {
                        System.out.println("Error while stopping jetty server: " + e.getMessage());
                        System.out.printf("Error while stopping jetty server: " + e.getMessage(), e);
                    }
                }
            }
        }));
    }

}
