package de.triology.versionname;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        System.out.println(VersionNames.getVersionNameFromManifest());
    }
}
