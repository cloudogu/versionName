package de.triology.versionname;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        // Scan annotations, to find ServletContainerInitializers, e.g. for JAX-RS
        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
                           "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                           "org.eclipse.jetty.plus.webapp.PlusConfiguration");
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                            "org.eclipse.jetty.annotations.AnnotationConfiguration");

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
