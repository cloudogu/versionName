package de.triology.mavenbuildnumber;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Path("version")
public class VersionResource {
    private static final String VERSION_NAME = getBuildNumberFromProperties();

    @GET
    public String getVersion() {
        return VERSION_NAME;
    }

    private static String getBuildNumberFromProperties() {
        String versionName = null;
        InputStream resourceAsStream = VersionResource.class.getResourceAsStream("/app.properties");
        try {
            if (resourceAsStream != null) {
                Properties props = new Properties();

                props.load(resourceAsStream);
                Object versionNameObject = props.get("versionName");
                if (versionNameObject instanceof String) {
                    versionName = (String) versionNameObject;
                }
            }
        } catch (IOException e) {
            versionName = "Unable to read build number";
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    System.err.println("Error closing properties stream");
                }
            }
        }
        return versionName;
    }
}
