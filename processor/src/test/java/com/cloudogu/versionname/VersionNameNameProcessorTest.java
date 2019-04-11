package com.cloudogu.versionname;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.tools.JavaFileObject;
import java.util.Collections;

public class VersionNameNameProcessorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    final JavaFileObject clazzInput = JavaFileObjects.forSourceString(
        "com.example.A",
        Joiner.on(System.lineSeparator()).join(
            "package com.example;",
            "",
            "import com.cloudogu.versionname.VersionName;",
            "",
            // Declare generation of Version class
            "@VersionName",
            "public class A {",
            "",
            "  public String hello() {",
            // Use generated class
            "    return Version.NAME;",
            "  }",
            "",
            "}"
        )
    );

    private String expectedVersion = "1.2.3";

    final JavaFileObject expectedOutput = JavaFileObjects.forSourceString(
        //"com.example.VersionName",
        "VersionName",
        Joiner.on(System.lineSeparator()).join(
            "package com.example;",
            "",
            "import java.lang.String;",
            "",
            "public final class Version {",
            "",
            "  public static final String NAME = \"" + expectedVersion + "\";",
            "",
            "}"
        )
    );

    @Test
    public void processClass() {
        processAndAssert(clazzInput, expectedOutput);
    }

    private void processAndAssert(JavaFileObject input, JavaFileObject output) {

        Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(Collections.singletonList(input))
            .withCompilerOptions("-AversionName=" + expectedVersion)
            .processedWith(new VersionNameProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(output);
    }
}
