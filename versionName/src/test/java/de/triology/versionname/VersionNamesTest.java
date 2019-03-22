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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import uk.org.lidalia.slf4jtest.TestLoggerFactoryResetRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static de.triology.versionname.VersionNames.*;
import static de.triology.versionname.VersionNames.VersionName.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link VersionNames}.
 */
public class VersionNamesTest {

    /**
     * Allows for mocking behavior of the static methods of {@link VersionNames}.
     */
    private ClassLoader classLoader = mock(ClassLoader.class);

    /**
     * Logger of class under test.
     */
    private static final TestLogger LOG = TestLoggerFactory.getTestLogger(VersionNames.class);

    /**
     * Rest logger before each test.
     **/
    @Rule public TestLoggerFactoryResetRule testLoggerFactoryResetRule = new TestLoggerFactoryResetRule();

    @Before
    public void setUp() throws Exception {
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, for specific properties file and property.
     */
    @Test
    public void testGetVersionNameFromPropertiesNonDefault() throws Exception {
        String expectedPath = "path";
        String expectedProperty = "someOtherProperty";
        String expectedVersionName = "42";
        ByteArrayInputStream resourceStream =
            new ByteArrayInputStream((expectedProperty + "=" + expectedVersionName).getBytes());
        mockManifest(expectedPath, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties(expectedPath, expectedProperty);

        // Assertions
        assertEquals("Unexpected version name", expectedVersionName, actualVersionName);
        verify(classLoader).getResources(expectedPath);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, where the property parameter is
     * <code>null</code>
     */
    @Test
    public void testGetVersionNameFromPropertiesPropertyNull() throws Exception {
        String expectedPath = "path";
        ByteArrayInputStream resourceStream = new ByteArrayInputStream(("someProperty=42").getBytes());
        mockManifest(expectedPath, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties(expectedPath, null);

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_KEY_NULL, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, where the path parameter is
     * <code>null</code>
     */
    @Test
    public void testGetVersionNameFromPropertiesPathNull() throws Exception {

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties(null, "someProperty");

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_RESOURCE_PATH_NULL, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Positive test for {@link VersionNames#getVersionNameFromProperties(String, String)}.
     */
    @Test
    public void testGetVersionNameFromPropertiesResource() {
        String expectedVersionName = "42L";
        ByteArrayInputStream resourceStream =
            new ByteArrayInputStream((DEFAULT_PROPERTY + "=" + expectedVersionName).getBytes());
        mockManifest(DEFAULT_PROPERTIES_FILE_PATH, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", expectedVersionName, actualVersionName);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, where the properties file is
     * <code>null</code>.
     */
    @Test
    public void testGetVersionNameFromPropertiesResourceNull() throws Exception {
        mockManifest(DEFAULT_PROPERTIES_FILE_PATH, null);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_RESOURCE_NOT_FOUND, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, when an IOException occurs when
     * loading the properties.
     */
    @Test
    public void testGetVersionNameFromPropertiesResourceIOExceptionOpenStream() throws Exception {
        InputStream resourceStream = mock(InputStream.class, new ThrowIOExceptionOnEachMethodCall());
        mockManifest(DEFAULT_PROPERTIES_FILE_PATH, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_EXCEPTION_READING_FROM_RESOURCE, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, when an IOException occurs when
     * reading the manifests from classpath
     */
    @Test
    public void testGetVersionNameFromPropertiesResourceIOExceptionGetResources() throws Exception {
        when(classLoader.getResources(anyString())).thenThrow(new IOException("Mocked Exception"));

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_EXCEPTION_GETTING_MANIFESTS_FROM_CLASSPATH, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    // TODO return multiple manifests where only one has the versionName

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, where the property is
     * <code>null</code>.
     */
    @Test
    public void testGetVersionNameFromPropertiesResourcePropertyNull() throws Exception {
        ByteArrayInputStream resourceStream = new ByteArrayInputStream("someOtherProperty=42L".getBytes());
        mockManifest(DEFAULT_PROPERTIES_FILE_PATH, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_NOT_FOUND_IN_RESOURCE, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, when an IOException occurs when
     * closing the resource stream.
     */
    @Test
    public void testGetVersionNameFromPropertiesResourceIOExceptionOnClose() throws Exception {
        String expectedVersionName = "42L";
        ByteArrayInputStream resourceStream =
            new ByteArrayInputStream((DEFAULT_PROPERTY + "=" + expectedVersionName).getBytes());
        mockManifest(DEFAULT_PROPERTIES_FILE_PATH, resourceStream);

        InputStream resourceStreamSpy = spy(resourceStream);
        doThrow(new IOException("Mocked exception")).when(resourceStreamSpy).close();
        mockManifest(DEFAULT_PROPERTIES_FILE_PATH, resourceStreamSpy);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", expectedVersionName, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_EXCEPTION_ON_CLOSE, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.WARN, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest(String, String)}.
     */
    @Test
    public void testGetVersionNameFromManifestNonDefault() throws Exception {
        String expectedPath = "path";
        String expectedAttribute = "someOtherAttribute";
        String expectedVersionName = "42";
        ByteArrayInputStream resourceStream = createManifest(expectedAttribute, expectedVersionName);

        mockManifest(expectedPath, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest(expectedPath, expectedAttribute);

        // Assertions
        assertEquals("Unexpected version name", expectedVersionName, actualVersionName);
        verify(classLoader).getResources(expectedPath);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest(String, String)}, where the attribute parameter is
     * <code>null</code>
     */
    @Test
    public void testGetVersionNameFromManifestParameterAttributeNull() throws Exception {
        String expectedPath = "path";
        ByteArrayInputStream resourceStream = createManifest("someAttribute", "42");
        mockManifest(expectedPath, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest(expectedPath, null);

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_KEY_NULL, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest(String, String)}, where the path parameter is
     * <code>null</code>
     */
    @Test
    public void testGetVersionNameFromManifestParameterPathNull() throws Exception {

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest(null, "someProperty");

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_RESOURCE_PATH_NULL, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Positive test for {@link VersionNames#getVersionNameFromManifest()}.
     */
    @Test
    public void testGetVersionNameFromManifest() throws Exception {
        String expectedVersionName = "42";
        ByteArrayInputStream resourceStream = createManifest(DEFAULT_MANIFEST_ATTRIBUTE, expectedVersionName);
        mockManifest(DEFAULT_MANIFEST_PATH, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", expectedVersionName, actualVersionName);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest()}., where the manifest file is
     * <code>null</code>.
     */
    @Test
    public void testGetVersionNameFromManifestResourceNull() throws Exception {
        mockManifest(DEFAULT_MANIFEST_PATH, null);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_RESOURCE_NOT_FOUND, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest()}., when an IOException occurs when
     * creating the manifest.
     */
    @Test
    public void testGetVersionNameFromManifestIOExceptionManifest() throws Exception {
        InputStream resourceStream = mock(InputStream.class, new ThrowIOExceptionOnEachMethodCall());
        mockManifest(DEFAULT_MANIFEST_PATH, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_EXCEPTION_READING_FROM_RESOURCE, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest()}., the attribute is <code>null</code>
     */
    @Test
    public void testGetVersionNameFromManifestAttributeNull() throws Exception {
        ByteArrayInputStream resourceStream = createManifest("someOtherAttribute", "42");
        mockManifest(DEFAULT_MANIFEST_PATH, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_NOT_FOUND_IN_RESOURCE, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.ERROR, logEvent.getLevel());
    }

    /**
     * Test for{@link VersionNames#getVersionNameFromManifest()}., when an IOException occurs when
     * closing the resource stream.
     */
    @Test
    public void testGetVersionNameFromManifestIOExceptionOnClose() throws Exception {
        InputStream resourceStream = mock(InputStream.class);
        doThrow(new IOException("Mocked exception")).when(resourceStream).close();
        mockManifest(DEFAULT_MANIFEST_PATH, resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
        LoggingEvent logEvent = getLogEvent(0);
        assertEquals("Unexpected log message", LOG_EXCEPTION_ON_CLOSE, logEvent.getMessage());
        assertEquals("Unexpected log level", Level.WARN, logEvent.getLevel());
    }

    /**
     * @return a {@link Manifest} as {@link ByteArrayInputStream}
     */
    private ByteArrayInputStream createManifest(String expectedAttribute, String expectedVersionName)
        throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(new Attributes.Name(expectedAttribute), expectedVersionName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        manifest.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * @return the logging event at <code>index</code>
     */
    public LoggingEvent getLogEvent(int index) {
        assertThat("Unexpected number of Log messages", LOG.getLoggingEvents().size(), greaterThan(index));
        return LOG.getLoggingEvents().get(index);
    }

    /**
     * Answer that makes mock throw an {@link IOException} on each method call.
     */
    private static class ThrowIOExceptionOnEachMethodCall implements Answer {
        public Object answer(InvocationOnMock invocation) throws Throwable {
            throw new IOException("Mocked Exception");
        }
    }

    private void mockManifest(String manifestPath, InputStream returnedStream) {
        final URLConnection mockUrlCon = mock(URLConnection.class);

        try {
            doReturn(returnedStream).when(mockUrlCon).getInputStream();
            URLStreamHandler stubUrlHandler = new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    return mockUrlCon;
                }
            };
            URL url = new URL("dont", "care", 0, "about-this", stubUrlHandler);
            Enumeration<URL> resources = Collections.enumeration(Collections.singletonList(url));
            when(classLoader.getResources(manifestPath)).thenReturn(resources);
        } catch (Exception e) {
            // We're in a test so save us the trouble of checked exceptions
            throw new RuntimeException(e);
        }
    }
}
