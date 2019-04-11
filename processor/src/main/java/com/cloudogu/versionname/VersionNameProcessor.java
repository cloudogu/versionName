package com.cloudogu.versionname;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Set;


@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.cloudogu.versionname.VersionName")
// <artifactId>maven-compiler-plugin</artifactId>
//<compilerArgs>
//<arg>-AversionName=${versionName}</arg>
//</compilerArgs>
@SupportedOptions({"versionName"})

public class VersionNameProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // TODO make this code clean
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                VersionName[] annotationInstances = element.getAnnotationsByType(VersionName.class);
                for (VersionName annotationInstance : annotationInstances) {

                    try {
                        writeVersionClass(processingEnv.getOptions().get("versionName"),
                            annotationInstance,
                            element
                        );
                    } catch (IOException e) {
                        error(e);
                    }
                }
            }
        }
        return true;
    }

    private void writeVersionClass(String versionName, VersionName versionNameAnnotation, Element element) throws IOException {
        Filer filer = processingEnv.getFiler();
        // TODO what about root package?
        String packageName = element.getEnclosingElement().toString();

        TypeSpec VersionClass = TypeSpec.classBuilder(versionNameAnnotation.className())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(FieldSpec.builder(String.class, versionNameAnnotation.fieldName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S",versionName)
                .build())
            .build();

        JavaFile.builder(packageName, VersionClass).build().writeTo(filer);
    }

    private void error(IOException e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: " + e.getMessage());
    }
}