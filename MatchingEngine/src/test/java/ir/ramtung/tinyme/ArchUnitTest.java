package ir.ramtung.tinyme;

import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

class ArchUnitTest {
    // @Test
    // void servicesShouldNotDependOnControllers() {
    //     // assert false;
    //     JavaClasses importedClasses = new ClassFileImporter().importPackages("ir");
    //     ArchRuleDefinition.noClasses()
    //         .that().resideInAPackage("..service..")
    //         .should().dependOnClassesThat().resideInAPackage("..controller..")
    //         .allowEmptyShould(true)
    //         .check(importedClasses);
    // }

    @Test
    void projectShouldHavePomXml() {
        java.io.File pomFile = new java.io.File("pom.xml");
        assert pomFile.exists() : "pom.xml file does not exist in the project root. This project should use Maven.";
    }

    @Test
    void projectShouldUseSpringBoot() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir");
        boolean usesSpringBoot = importedClasses.stream()
            .anyMatch(javaClass -> javaClass.getPackageName().startsWith("org.springframework.boot")
                || javaClass.getAnnotations().stream().anyMatch(a -> a.getType().getName().startsWith("org.springframework.boot")));
        assert usesSpringBoot : "Project does not use Spring Boot (no classes or annotations from 'org.springframework.boot' found).";
    }
}
