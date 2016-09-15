package de.triology.versionname;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Provides access to a version names written to files such as the manifest or a properties file.
 */
public class VersionNames {

    /**
     * The path of the properties file that is used for looking up the version name by default.
     */
    static final String DEFAULT_PROPERTIES_FILE_PATH = "/app.properties";

    /**
     * The property within {@link #DEFAULT_PROPERTIES_FILE_PATH} that is used for looking up the version name by
     * default.
     */
    static final String DEFAULT_PROPERTY = "versionName";

    /**
     * The path of the manifest that is used for looking up the version name by default.
     */
    static final String DEFAULT_MANIFEST_PATH = "META-INF/MANIFEST.MF";

    /**
     * The attribute within {@link #DEFAULT_MANIFEST_PATH} that is used for looking up the version name by
     * default.
     */
    static final String DEFAULT_MANIFEST_ATTRIBUTE = "versionName";

    /**
     * The version string that is returned if anything goes wrong.
     */
    static final String VERSION_STRING_ON_ERROR = "";

    /**
     * Class that allows access to resources. Can be overwritten in a test.
     */
    private static ClassLoader classLoader = VersionNames.class.getClassLoader();

    /**
     * Utility class. Do not instantiate.
     */
    private VersionNames() {
    }

    /**
     * Reads the version name from a default properties file <code>/app.properties</code> and property
     * <code>versionName</code>.
     *
     * @return the version name or empty string if anything goes wrong. In case of error, see log for details.
     */
    public static String getVersionNameFromProperties() {
        return getVersionNameFromProperties(DEFAULT_PROPERTIES_FILE_PATH, DEFAULT_PROPERTY);
    }

    /**
     * Reads the version name from <code>propertiesFilePath</code> and <code>property</code>.
     *
     * @param propertiesFilePath path to the properties file to open from classpath, relative to this class.
     * @param property           property within <code>propertiesFilePath</code> that is used for looking up the version name
     * @return the version name or empty string if anything goes wrong. In case of error, see log for details.
     */
    public static String getVersionNameFromProperties(String propertiesFilePath, final String property) {
        return new VersionName() {
            @Override
            protected String handleResourceStream(InputStream resourceAsStream) throws IOException {
                Properties props = new Properties();

                props.load(resourceAsStream);
                Object versionNameObject = props.get(property);
                // Properties.get() seems to always return strings. But: In theory, it could return any other object.
                if (versionNameObject instanceof String) {
                    return (String) versionNameObject;
                } else if (versionNameObject != null) {
                    return versionNameObject.toString();
                }
                return null;
            }
        }.fromResource(propertiesFilePath);
    }

    /**
     * Reads the version name from manifest file located at <code>META-INF/MANIFEST.MF</code> and attribute
     * <code>versionName</code>.
     *
     * @return the version name or empty string if anything goes wrong. In case of error, see log for details.
     */
    public static String getVersionNameFromManifest() {
        return getVersionNameFromManifest(DEFAULT_MANIFEST_PATH, DEFAULT_MANIFEST_ATTRIBUTE);
    }

    /**
     * Reads the version name from <code>manifestFilePath</code> and <code>attribute</code>.
     *
     * @param manifestFilePath path to the manifest file to open from classpath, relative to this class.
     * @param attribute        attribute within <code>manifestFilePath</code> that is used for looking up the version name
     * @return the version name or empty string if anything goes wrong. In case of error, see log for details.
     */
    public static String getVersionNameFromManifest(String manifestFilePath, final String attribute) {
        return new VersionName() {
            @Override
            protected String handleResourceStream(InputStream resourceAsStream) throws IOException {
                Manifest manifest = new Manifest(resourceAsStream);
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue(attribute);
            }
        }.fromResource(manifestFilePath);
    }

    // TODO Logging

    /**
     * Handles the generic part of version number loading. Gets specific resource stream from classloader and takes care of
     * the error handling and stream closing.
     */
    private abstract static class VersionName {
        public String fromResource(String resourcePath) {
            String versionName = VERSION_STRING_ON_ERROR;
            InputStream resourceAsStream = classLoader.getResourceAsStream(resourcePath);
            try {
                if (resourceAsStream != null) {
                    versionName = handleResourceStream(resourceAsStream);
                }
            } catch (IOException e) {
                // TODO log
            } finally {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close();
                    } catch (IOException e) {
                        // TODO log
                    }
                }
            }
            if (versionName == null) {
                versionName = VERSION_STRING_ON_ERROR;
            }

            return versionName;
        }

        /**
         * Template method that implements the actual version number logic.
         *
         * @param resourceAsStream the resource to load the version number from
         * @return the version number, or <code>null</code>, if none.
         * @throws IOException - if an I/O error has occurred
         */
        protected abstract String handleResourceStream(InputStream resourceAsStream) throws IOException;
    }
}

