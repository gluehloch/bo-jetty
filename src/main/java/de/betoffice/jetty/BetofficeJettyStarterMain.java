package de.betoffice.jetty;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class BetofficeJettyStarterMain {

    private static final int PORT = 9290;

    private static final String CONTEXT_PATH = "/bo";
    private static final String CONFIG_LOCATION_PACKAGE = "de.betoffice";
    private static final String MAPPING_URL = "/";
    private static final String WEBAPP_DIRECTORY = "WEB-INF"; // "webapp";

    public static void main(String[] args) throws Exception {
        new BetofficeJettyStarterMain().startJetty(PORT);
    }

    private void startJetty(int port) throws Exception {
        System.out.printf("Starting server at port %s", port);
        Server server = new Server(port);

        server.setHandler(getServletContextHandler());

        addRuntimeShutdownHook(server);

        server.start();
        System.out.printf("Server started at port %s", port);
        server.join();
    }

    private static HandlerCollection getServletContextHandler() throws IOException {
        HandlerCollection handlerCollection = new HandlerCollection();
        
        ServletContextHandler pingContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        pingContextHandler.setErrorHandler(null);
        pingContextHandler.setContextPath("/ping");
        pingContextHandler.addServlet(new ServletHolder(new HelloServlet()), "/*");

        handlerCollection.addHandler(pingContextHandler);
        
        //contextHandler.setResourceBase(new ClassPathResource(WEBAPP_DIRECTORY).getURI().toString());
        //contextHandler.setContextPath(CONTEXT_PATH);

        // JSP
        // contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        // contextHandler.addServlet("jsp", J);

        // Spring
        /*
        WebApplicationContext webAppContext = getWebApplicationContext();
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webAppContext);
        ServletHolder springServletHolder = new ServletHolder("bo", dispatcherServlet);
        contextHandler.addServlet(springServletHolder, MAPPING_URL);
        contextHandler.getInitParams().put("contextConfigLocation", "classpath:/betoffice.xml");
        contextHandler.addEventListener(new ContextLoaderListener(webAppContext));
        */
        WebApplicationContext webAppContext = getWebApplicationContext();
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webAppContext);
        ServletHolder springServletHolder = new ServletHolder("bo", dispatcherServlet);
        
        ServletContextHandler betofficeContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        betofficeContextHandler.setErrorHandler(null);
        betofficeContextHandler.setContextPath("/betoffice");
        betofficeContextHandler.addEventListener(new ContextLoaderListener());
        betofficeContextHandler.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
        betofficeContextHandler.addServlet(springServletHolder, "/*");
        
        handlerCollection.addHandler(betofficeContextHandler);

        return handlerCollection;
    }

    private static WebApplicationContext getWebApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.scan(CONFIG_LOCATION_PACKAGE);
        context.setConfigLocation("classpath:/betoffice.xml");
        context.refresh();
        context.start();
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

    public static class HelloServlet extends HttpServlet {
        private static final long serialVersionUID = -6154475799000019575L;
        private static final String greeting = "Hello World";

        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(greeting);
            response.getWriter().println(LocalDateTime.now());
        }
    }

}
