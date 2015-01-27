package com.gotcake.protobuf;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by aaron on 12/12/14.
 */
public class Server {

    private static final Logger LOG = Log.getLogger(Server.class);

    public static void main(final String[] args) {

        try {

            final int port = args.length == 1 ? Integer.parseInt(args[0]) : 8080;

            final org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);

            final Thread stopCheckThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {}
                        final File checkFile = new File(".test_server");
                        if (!checkFile.exists()) {
                            try {
                                server.stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                }
            });
            stopCheckThread.setDaemon(true);

            final ResourceHandler fileHandler = new ResourceHandler();
            fileHandler.setDirectoriesListed(false);
            fileHandler.setWelcomeFiles(new String[]{ "index.html" });
            fileHandler.setResourceBase("web");

            final ServletHandler servletHandler = new ServletHandler();
            servletHandler.addServletWithMapping(ProtobufProducingServlet.class, "/get");

            final HandlerList handlerList = new HandlerList();
            handlerList.setHandlers(new Handler[] {
                    servletHandler,
                    fileHandler
            });

            server.setHandler(handlerList);

            server.start();

            final File checkFile = new File(".test_server");
            checkFile.createNewFile();

            stopCheckThread.start();

            server.join();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}
