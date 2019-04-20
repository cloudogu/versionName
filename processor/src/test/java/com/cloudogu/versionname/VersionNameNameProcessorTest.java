package com.cloudogu.versionname;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubject;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.tools.JavaFileObject;

import java.util.Collections;

public class VersionNameNameProcessorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String expectedErrorMissingCompilerArg = "Compile Arg \"versionName\" not set.";
    private String expectedVersion = "1.2.3";

    private final JavaFileObject clazzInput = JavaFileObjects.forSourceString(
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
    public void happyDays() {
        process(clazzInput, "-AversionName=" + expectedVersion)
            .compilesWithoutError()
            .and()
            .generatesSources(expectedOutput);
    }

    @Test
    public void compilerArgNotSet() {

        process(clazzInput, null)
            .failsToCompile()
            .withErrorContaining(expectedErrorMissingCompilerArg);
    }

    @Test
    public void compilerArgEmtpy() {
        process(clazzInput, "-AversionName=")
            .failsToCompile()
            .withErrorContaining(expectedErrorMissingCompilerArg);
    }

    private CompileTester process(JavaFileObject sources, String compilerArgString) {

        JavaSourcesSubject src = Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(Collections.singletonList(sources));

        if (compilerArgString != null && !compilerArgString.isEmpty()) {
            src = src.withCompilerOptions(compilerArgString);
        }

        return src.processedWith(new VersionNameProcessor());
    }
}
