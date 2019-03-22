/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 TRIOLOGY GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.triology.versionname;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Provides access to version names written to files such as the manifest or a properties file.
 */
public class VersionNames {

    private static final Logger LOG = LoggerFactory.getLogger(VersionNames.class);

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
    public static String getVersionNameFromProperties(String propertiesFilePath, String property) {
        return new VersionName() {
            @Override
            protected String handleResourceStream(InputStream resourceAsStream, String key) throws IOException {
                Properties props = new Properties();

                props.load(resourceAsStream);
                Object versionNameObject = props.get(key);
                // Properties.get() seems to always return strings. But: In theory, it could return any other object.
                if (versionNameObject instanceof String) {
                    return (String) versionNameObject;
                } else if (versionNameObject != null) {
                    return versionNameObject.toString();
                } else {
                    return null;
                }
            }
        }.fromResource(propertiesFilePath, property);
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
    public static String getVersionNameFromManifest(String manifestFilePath, String attribute) {
        return new VersionName() {
            @Override
            protected String handleResourceStream(InputStream resourceAsStream, String key) throws IOException {
                Manifest manifest = new Manifest(resourceAsStream);
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue(key);
            }
        }.fromResource(manifestFilePath, attribute);
    }

    /**
     * Handles the generic part of version number loading. Gets specific resource stream from classloader and takes care
     * of the error handling, logging and stream closing.
     * <p>Concrete classes need to implement the logic for getting the resource stream in
     * {@link #handleResourceStream(InputStream, String)}.</p>
     */
    abstract static class VersionName {
        static final String LOG_RESOURCE_NOT_FOUND = "Cannot read version name. Resource \"{}\" not found on classpath";
        static final String LOG_KEY_NULL = "Cannot read version name from resource. Key is null";
        static final String LOG_RESOURCE_PATH_NULL = "Cannot read version name. Resource path is null";
        static final String LOG_NOT_FOUND_IN_RESOURCE = "Version name not found in {}";
        static final String LOG_EXCEPTION_READING_FROM_RESOURCE = "Exception while reading version name from {}";
        static final String LOG_EXCEPTION_GETTING_MANIFESTS_FROM_CLASSPATH = "Exception while reading manifests from classpath: {}";
        static final String LOG_EXCEPTION_ON_CLOSE = "Unable to close resource stream after reading version number";

        /**
         * Template method that implements the actual version number logic.
         *
         * @param resourceStream the resource to load the version number from. Never <code>null</code>
         * @param key            the key within the resource stream. Never <code>null</code>
         * @return the version number, or <code>null</code>, if none found
         * @throws IOException - if an I/O error has occurred
         */
        protected abstract String handleResourceStream(InputStream resourceStream, String key) throws IOException;

        /**
         * Get the version name from the specified <code>resourcePath</code> at the specified <code>key</code>.
         *
         * @param resourcePath path to the resource to open from classpath, relative to this class. Can be <code>null</code>
         * @param key          the key within the resource stream. Can be <code>null</code>
         * @return the version number or {@link #VERSION_STRING_ON_ERROR}, if none found. Never <code>null</code>
         */
        public String fromResource(String resourcePath, String key) {
            String versionName = VERSION_STRING_ON_ERROR;
            if (resourcePath == null) {
                LOG.error(LOG_RESOURCE_PATH_NULL);
            } else if (key == null) {
                LOG.error(LOG_KEY_NULL);
            } else {
                versionName = processResource(resourcePath, key);
            }

            // Never return null
            if (versionName == null) {
                LOG.error(LOG_NOT_FOUND_IN_RESOURCE, resourcePath);
                versionName = VERSION_STRING_ON_ERROR;
            }

            return versionName;
        }

        /**
         * Actual logic for opening and closing the stream. Calls template method
         * {@link #handleResourceStream(InputStream, String)}.
         */
        private String processResource(String resourcePath, String key) {
            Enumeration<URL> resources = getResources(resourcePath);

            while (resources.hasMoreElements()) {
                String potentialVersion = processUrl(resourcePath, key, resources.nextElement());
                if (potentialVersion != null) {
                    return potentialVersion;
                }
            }
            return null;
        }

        @SuppressWarnings("squid:S2583") // How can sonar be sure that openStream() always returns non-null?
        private String processUrl(String resourcePath, String key, URL url) {
            InputStream resourceStream = null;
            try {
                resourceStream = url.openStream();

                if (resourceStream != null) {
                    String potentialVersion = handleResourceStream(resourceStream, key);
                    if (potentialVersion != null && !potentialVersion.isEmpty()) {
                        return potentialVersion;
                    }
                } else {
                    LOG.error(LOG_RESOURCE_NOT_FOUND, resourcePath);
                }
            } catch (IOException e) {
                LOG.error(LOG_EXCEPTION_READING_FROM_RESOURCE, resourcePath, e);
            } finally {
                closeStreamIfNotNull(resourceStream);
            }
            return null;
        }

        private Enumeration<URL> getResources(String resourcePath) {
            try {
                return Thread.currentThread().getContextClassLoader().getResources(resourcePath);
            } catch (IOException e) {
                LOG.error(LOG_EXCEPTION_GETTING_MANIFESTS_FROM_CLASSPATH, resourcePath, e);
                return Collections.enumeration(Collections.emptyList());
            }
        }

        private void closeStreamIfNotNull(InputStream resourceStream) {
            if (resourceStream != null) {
                try {
                    resourceStream.close();
                } catch (IOException e) {
                    LOG.warn(LOG_EXCEPTION_ON_CLOSE, e);
                }
            }
        }
    }
}

