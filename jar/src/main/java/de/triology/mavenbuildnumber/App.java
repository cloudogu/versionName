package de.triology.mavenbuildnumber;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        System.out.println(getBuildNumberFromManifest());
    }

    private static String getBuildNumberFromManifest() throws IOException {
        InputStream manifestStream = App.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
        if (manifestStream != null) {
            Manifest manifest = new Manifest(manifestStream);
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("build");
        }
        return null;
    }
}
