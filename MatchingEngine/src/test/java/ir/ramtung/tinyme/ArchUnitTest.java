package ir.ramtung.tinyme;

import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
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

    @Test
    void projectShouldUseJmsForMessaging() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir");
        boolean usesJms = importedClasses.stream()
            .anyMatch(javaClass -> javaClass.getPackageName().startsWith("javax.jms")
                || javaClass.getPackageName().startsWith("jakarta.jms")
                || javaClass.getPackageName().startsWith("org.springframework.jms")
                || javaClass.getAnnotations().stream().anyMatch(a -> a.getType().getName().startsWith("javax.jms")
                    || a.getType().getName().startsWith("jakarta.jms")
                    || a.getType().getName().startsWith("org.springframework.jms")));
        assert usesJms : "Project does not use JMS for messaging (no classes or annotations from 'javax.jms', 'jakarta.jms', or 'org.springframework.jms' found).";
    }

    @Test
    void projectShouldUseOpenCsvForCsvFiles() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir");
        boolean usesOpenCsv = importedClasses.stream()
            .anyMatch(javaClass -> javaClass.getDirectDependenciesFromSelf().stream()
                .anyMatch(dep -> dep.getTargetClass().getPackageName().startsWith("com.opencsv"))
            );
        assert usesOpenCsv : "Project does not use OpenCSV (no classes from 'com.opencsv' found in dependencies).";
    }

    @Test
    void allControlsShouldImplementMatchingControl() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir.ramtung.tinyme.domain.service");
        importedClasses.stream()
            .filter(javaClass -> javaClass.getSimpleName().endsWith("Control"))
            .filter(javaClass -> !javaClass.isInterface() && !javaClass.getModifiers().contains(JavaModifier.ABSTRACT))
            .forEach(javaClass -> {
                boolean implementsMatchingControl = javaClass.getAllRawInterfaces().stream()
                    .anyMatch(i -> i.getFullName().equals("ir.ramtung.tinyme.domain.service.MatchingControl"));
                assert implementsMatchingControl : javaClass.getName() + " does not implement MatchingControl interface.";
            });
    }

    @Test
    void allJmsInteractionsShouldUseLogger() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir");
        importedClasses.stream()
            .filter(javaClass -> javaClass.getAllFields().stream()
                .anyMatch(field -> field.getRawType().getFullName().equals("org.springframework.jms.core.JmsTemplate")))
            .filter(javaClass -> !javaClass.getName().toLowerCase().contains("test")) // Exclude test classes
            .forEach(javaClass -> {
                boolean hasLogger = javaClass.getAllFields().stream()
                    .anyMatch(field -> field.getRawType().getFullName().equals("java.util.logging.Logger"));
                assert hasLogger : javaClass.getName() + " interacts with JMS but does not declare a java.util.logging.Logger field.";
            });
    }

    @Test
    void onlyAllowedClassesShouldAccessBroker() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir.ramtung.tinyme");
        var allowed = java.util.Set.of(
            "ir.ramtung.tinyme.domain.entity.Order",
            "ir.ramtung.tinyme.domain.entity.Order$OrderBuilder",
            "ir.ramtung.tinyme.domain.entity.IcebergOrder",
            "ir.ramtung.tinyme.repository.BrokerRepository",
            "ir.ramtung.tinyme.repository.DataLoader",
            "ir.ramtung.tinyme.domain.entity.Broker",
            "ir.ramtung.tinyme.domain.entity.Broker$BrokerBuilder",
            "ir.ramtung.tinyme.utils.FixtureDefaults",
            "ir.ramtung.tinyme.domain.service.NewOrderProcessor",
            // why should CreditControl access Broker?
            "ir.ramtung.tinyme.domain.service.CreditControl"
        );
        importedClasses.stream()
            .filter(javaClass -> javaClass.getDirectDependenciesFromSelf().stream()
                .anyMatch(dep -> dep.getTargetClass().getFullName().equals("ir.ramtung.tinyme.domain.entity.Broker")))
            .forEach(javaClass -> {
                boolean isTest = javaClass.getName().contains("Test");
                assert allowed.contains(javaClass.getFullName()) || isTest :
                    javaClass.getFullName() + " is not allowed to access Broker class.";
            });
    }

    @Test
    void allProcessorsShouldExtendCommandProcessor() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir.ramtung.tinyme.domain.service");
        importedClasses.stream()
            .filter(javaClass -> javaClass.getSimpleName().endsWith("Processor"))
            .filter(javaClass -> !javaClass.getSimpleName().equals("CommandProcessor"))
            .filter(javaClass -> !javaClass.isInterface() && !javaClass.getModifiers().contains(JavaModifier.ABSTRACT))
            .forEach(javaClass -> {
                boolean extendsCommandProcessor = javaClass.getAllRawSuperclasses().stream()
                    .anyMatch(superClass -> superClass.getFullName().equals("ir.ramtung.tinyme.domain.service.CommandProcessor"));
                assert extendsCommandProcessor : javaClass.getName() + " does not extend CommandProcessor.";
            });
    }

}
