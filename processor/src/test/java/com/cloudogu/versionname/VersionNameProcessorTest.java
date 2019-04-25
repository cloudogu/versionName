package com.cloudogu.versionname;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubject;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.Arrays;

public class VersionNameProcessorTest {

    private static final String DEFAULT_CLASS_NAME = "Version";
    private static final String DEFAULT_FIELD_NAME = "NAME";
    private static final String OTHER_FIELD_NAME = "OTHER";
    private static final String OTHER_CLASS_NAME = "Other";

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

    private final JavaFileObject clazzDefaultPackageInput = JavaFileObjects.forSourceString(
        "A",
        Joiner.on(System.lineSeparator()).join(
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

    private final JavaFileObject packageInput = JavaFileObjects.forSourceString(
        "com.example.package-info",
        Joiner.on(System.lineSeparator()).join(
            "@VersionName",
            "package com.example;",
            "import com.cloudogu.versionname.VersionName;"
        )
    );

    private final JavaFileObject packageInputDifferentField = JavaFileObjects.forSourceString(
        "com.example.package-info",
        Joiner.on(System.lineSeparator()).join(
            "@VersionName(fieldName = \"" + OTHER_FIELD_NAME + "\")",
            "package com.example;",
            "import com.cloudogu.versionname.VersionName;"
        )
    );

    private final JavaFileObject packageInputDifferentClass = JavaFileObjects.forSourceString(
        "com.example.package-info",
        Joiner.on(System.lineSeparator()).join(
            "@VersionName(className = \"" + OTHER_CLASS_NAME + "\")",
            "package com.example;",
            "import com.cloudogu.versionname.VersionName;"
        )
    );

    @Test
    public void generateFromClass() {
        process("-AversionName=" + expectedVersion, clazzInput)
            .compilesWithoutError()
            .and()
            .generatesSources(expectedOutput());
    }

    @Test
    public void generateFromPackage() {
        process("-AversionName=" + expectedVersion, packageInput)
            .compilesWithoutError()
            .and()
            .generatesSources(expectedOutput());
    }

    @Test
    public void differentField() {
        process("-AversionName=" + expectedVersion, packageInputDifferentField)
            .compilesWithoutError()
            .and()
            .generatesSources(expectedOutput(DEFAULT_CLASS_NAME, OTHER_FIELD_NAME));
    }

    @Test
    public void differentClass() {
        process("-AversionName=" + expectedVersion, packageInputDifferentClass)
            .compilesWithoutError()
            .and()
            .generatesSources(expectedOutput(OTHER_CLASS_NAME, DEFAULT_FIELD_NAME));
    }

    @Test
    public void generateFromClassInRootPackage() {
        process("-AversionName=" + expectedVersion, clazzDefaultPackageInput)
            .compilesWithoutError()
            .and()
            .generatesSources(expectedOutput(DEFAULT_CLASS_NAME, DEFAULT_FIELD_NAME, ""));
    }

    @Test
    public void generateFromPackageInRootPackage() {
        // It is syntactically impossible to use an annotation on the default package, right?!
        // How would it look like? "package;"
    }

    @Test
    public void conflictingAnnotations() {
        process("-AversionName=" + expectedVersion, clazzInput, packageInputDifferentField)
            .failsToCompile()
            .withErrorContaining("Attempt to recreate a file");
    }

    @Test
    public void compilerArgNotSet() {

        process(null, clazzInput)
            .failsToCompile()
            .withErrorContaining(expectedErrorMissingCompilerArg);
    }

    @Test
    public void compilerArgEmpty() {
        process("-AversionName=", clazzInput)
            .failsToCompile()
            .withErrorContaining(expectedErrorMissingCompilerArg);
    }

    private CompileTester process(String compilerArgString, JavaFileObject... sources) {

        JavaSourcesSubject src = Truth.assert_()
            .about(JavaSourcesSubjectFactory.javaSources())
            .that(Arrays.asList(sources));

        if (compilerArgString != null && !compilerArgString.isEmpty()) {
            src = src.withCompilerOptions(compilerArgString);
        }

        return src.processedWith(new VersionNameProcessor());
    }

    private JavaFileObject expectedOutput() {
        return expectedOutput(DEFAULT_CLASS_NAME, DEFAULT_FIELD_NAME);
    }

    private JavaFileObject expectedOutput(String className, String fieldName) {
       return expectedOutput(className, fieldName, "com.example");
    }

    private JavaFileObject expectedOutput(String className, String fieldName, String packageName) {
        String optionalPackage = "";
        if (!packageName.isEmpty()) {
            optionalPackage = "package " + packageName + ";";
        }
        JavaFileObject src = JavaFileObjects.forSourceString(
            "VersionName",
            Joiner.on(System.lineSeparator()).join(
                optionalPackage,
                "",
                "import java.lang.String;",
                "",
                "public final class " + className + " {",
                "",
                "  public static final String " + fieldName + " = \"" + expectedVersion + "\";",
                "",
                "}"
            )
        );
        return src;
    }
}
