package ir;

import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchUnitTest {
    @Test
    void servicesShouldNotDependOnControllers() {
        // assert false;
        JavaClasses importedClasses = new ClassFileImporter().importPackages("ir");
        ArchRuleDefinition.noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .allowEmptyShould(true)
            .check(importedClasses);
    }
}
