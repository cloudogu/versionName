package com.cloudogu.versionname;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.kohsuke.MetaInfServices;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
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
// compilerArgs: "-AversionName=${versionName}"
@SupportedOptions({"versionName"})
@MetaInfServices(Processor.class)
public class VersionNameProcessor extends AbstractProcessor {

    private static final boolean CLAIM_ANNOTATIONS = true;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            processAnnotatedElements(roundEnv.getElementsAnnotatedWith(annotation));
        }

        return CLAIM_ANNOTATIONS;
    }

    private void processAnnotatedElements(Set<? extends Element> annotatedElements) {

        for (Element annotatedElement : annotatedElements) {
            processAnnotationInstances(annotatedElement.getAnnotationsByType(VersionName.class), annotatedElement);
        }
    }

    private void processAnnotationInstances(VersionName[] annotationInstances, Element annotatedElement) {

        for (VersionName annotationInstance : annotationInstances) {
            processAnnotationInstance(annotationInstance, annotatedElement);
        }
    }

    private void processAnnotationInstance(VersionName annotationInstance, Element annotatedElement) {

        try {
            String versionName = readAndValidateVersionNameFromCompilerArg();

            writeVersionClass(versionName, annotationInstance, annotatedElement);

        } catch (IOException e) {
            error(e);
        }
    }

    private String readAndValidateVersionNameFromCompilerArg() throws IOException {
        String versionName = processingEnv.getOptions().get("versionName");
        if (versionName == null || versionName.isEmpty()) {
            throw new IOException("Compile Arg \"versionName\" not set.");
        }
        return versionName;
    }

    private void writeVersionClass(String versionName, VersionName versionNameAnnotation, Element element) throws IOException {
        Filer filer = processingEnv.getFiler();
        String packageName = findPackageName(element);

        TypeSpec VersionClass = TypeSpec.classBuilder(versionNameAnnotation.className())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(FieldSpec.builder(String.class, versionNameAnnotation.fieldName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S",versionName)
                .build())
            .build();

        JavaFile.builder(packageName, VersionClass).build().writeTo(filer);
    }

    private String findPackageName(Element element) {
        if (element.getEnclosingElement() != null) {
            return findPackageNameForClass(element);
        }
        return findPackageNameForPackage(element);
    }

    private String findPackageNameForPackage(Element element) {
        return element.toString();
    }

    private String findPackageNameForClass(Element element) {
        if (element.getEnclosingElement().getSimpleName().toString().isEmpty()) {
            return findPackageNameForClassInDefaultPackage();
        }
        return element.getEnclosingElement().toString();
    }

    private String findPackageNameForClassInDefaultPackage() {
        return "";
    }

    private void error(IOException e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write extension file: " + e.getMessage());
    }
}
