package de.triology.versionname;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static de.triology.versionname.VersionNames.DEFAULT_MANIFEST_ATTRIBUTE;
import static de.triology.versionname.VersionNames.DEFAULT_MANIFEST_PATH;
import static de.triology.versionname.VersionNames.DEFAULT_PROPERTIES_FILE_PATH;
import static de.triology.versionname.VersionNames.DEFAULT_PROPERTY;
import static de.triology.versionname.VersionNames.VERSION_STRING_ON_ERROR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link VersionNames}.
 */
public class VersionNamesTest {

    /**
     * Allows for mocking behavior of the static methods of {@link VersionNames}.
     */
    private ClassLoader classLoader = mock(ClassLoader.class);

    @Before
    public void setUp() throws Exception {
        // Inject mocked class into static field
        setVersionNamesClassLoader(classLoader);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties()}, for specific properties file and property.
     */
    @Test
    public void testGetVersionNameFromPropertiesNonDefault() throws Exception {
        String expectedPath = "path";
        String expectedProperty = "someOtherProperty";
        String expectedVersionName = "42";
        ByteArrayInputStream resourceStream =
            new ByteArrayInputStream((expectedProperty + "=" + expectedVersionName).getBytes());
        when(classLoader.getResourceAsStream(expectedPath)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties(expectedPath, expectedProperty);

        // Assertions
        assertEquals("Unexpected version name", expectedVersionName, actualVersionName);
        verify(classLoader).getResourceAsStream(expectedPath);
    }

    /**
     * Positive test for {@link VersionNames#getVersionNameFromProperties(String, String)}.
     */
    @Test
    public void testGetVersionNameFromPropertiesResource() {
        String expectedVersionName = "42L";
        ByteArrayInputStream resourceStream =
            new ByteArrayInputStream((DEFAULT_PROPERTY + "=" + expectedVersionName).getBytes());
        when(classLoader.getResourceAsStream(DEFAULT_PROPERTIES_FILE_PATH)).thenReturn(resourceStream);

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
        when(classLoader.getResourceAsStream(DEFAULT_PROPERTIES_FILE_PATH)).thenReturn(null);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, when an IOException occurs when
     * loading the properties.
     */
    @Test
    public void testGetVersionNameFromPropertiesResourceIOExceptionProperties() throws Exception {
        InputStream resourceStream = mock(InputStream.class, new ThrowIOExceptionOnEachMethodCall());
        when(classLoader.getResourceAsStream(DEFAULT_PROPERTIES_FILE_PATH)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, the property is <code>null</code>
     */
    @Test
    public void testGetVersionNameFromPropertiesResourcePropertyNull() throws Exception {
        ByteArrayInputStream resourceStream = new ByteArrayInputStream("someOtherProperty=42L".getBytes());
        when(classLoader.getResourceAsStream(DEFAULT_PROPERTIES_FILE_PATH)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromProperties(String, String)}, when an IOException occurs when
     * closing the resource stream.
     */
    @Test
    public void testGetVersionNameFromPropertiesResourceIOExceptionOnClose() throws Exception {
        InputStream resourceStream = mock(InputStream.class);
        doThrow(new IOException("Mocked exception")).when(resourceStream).close();
        when(classLoader.getResourceAsStream(DEFAULT_PROPERTIES_FILE_PATH)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromProperties();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
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
        when(classLoader.getResourceAsStream(expectedPath)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest(expectedPath, expectedAttribute);

        // Assertions
        assertEquals("Unexpected version name", expectedVersionName, actualVersionName);
        verify(classLoader).getResourceAsStream(expectedPath);
    }

    /**
     * Positive test for {@link VersionNames#getVersionNameFromManifest()}.
     */
    @Test
    public void testGetVersionNameFromManifest() throws Exception {
        String expectedVersionName = "42";
        ByteArrayInputStream resourceStream = createManifest(DEFAULT_MANIFEST_ATTRIBUTE, expectedVersionName);
        when(classLoader.getResourceAsStream(DEFAULT_MANIFEST_PATH)).thenReturn(resourceStream);

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
    public void testGetVersionNameFromManifestManifestNull() throws Exception {
        when(classLoader.getResourceAsStream(DEFAULT_MANIFEST_PATH)).thenReturn(null);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest()}., when an IOException occurs when
     * creating the manifest.
     */
    @Test
    public void testGetVersionNameFromManifestIOExceptionManifest() throws Exception {
        InputStream resourceStream = mock(InputStream.class, new ThrowIOExceptionOnEachMethodCall());
        when(classLoader.getResourceAsStream(DEFAULT_MANIFEST_PATH)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
    }

    /**
     * Test for {@link VersionNames#getVersionNameFromManifest()}., the attribute is <code>null</code>
     */
    @Test
    public void testGetVersionNameFromManifestAttributeNull() throws Exception {
        ByteArrayInputStream resourceStream = createManifest("someOtherAttribute", "42");
        when(classLoader.getResourceAsStream(DEFAULT_MANIFEST_PATH)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
    }

    /**
     * Test for{@link VersionNames#getVersionNameFromManifest()}., when an IOException occurs when
     * closing the resource stream.
     */
    @Test
    public void testGetVersionNameFromManifestIOExceptionOnClose() throws Exception {
        InputStream resourceStream = mock(InputStream.class);
        doThrow(new IOException("Mocked exception")).when(resourceStream).close();
        when(classLoader.getResourceAsStream(DEFAULT_MANIFEST_PATH)).thenReturn(resourceStream);

        // Call method under test
        String actualVersionName = VersionNames.getVersionNameFromManifest();

        // Assertions
        assertEquals("Unexpected version name", VERSION_STRING_ON_ERROR, actualVersionName);
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
     * Allows for replacing {@link VersionNames}' clazz field by a mock.
     */
    private void setVersionNamesClassLoader(ClassLoader classLoader) throws Exception {
        Field field = VersionNames.class.getDeclaredField("classLoader");
        field.setAccessible(true);

        field.set(null, classLoader);
    }

    /**
     * Answer that makes mock throw an {@link IOException} on each method call.
     */
    private static class ThrowIOExceptionOnEachMethodCall implements Answer {
        public Object answer(InvocationOnMock invocation) throws Throwable {
            throw new IOException("Mocked Exception");
        }
    }
}
