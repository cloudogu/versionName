package de.triology.mavenbuildnumber;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        // Deploy war
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        File warFile = new File("war/target/war.war");
        webapp.setWar(warFile.getAbsolutePath());

        server.setHandler(webapp);

        server.start();
        server.dumpStdErr();
        server.join();
    }
}
