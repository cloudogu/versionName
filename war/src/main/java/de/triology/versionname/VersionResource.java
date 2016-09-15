package de.triology.versionname;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("version")
public class VersionResource {
    private static final String VERSION_NAME = VersionNames.getVersionNameFromProperties();

    @GET
    public String getVersion() {
        return VERSION_NAME;
    }
}
