package ir;

import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchUnitTest {

    @Test
    void projectShouldHavePomXml() {
        java.io.File pomFile = new java.io.File("pom.xml");
        assert pomFile.exists() : "pom.xml file does not exist in the project root. This project should use Maven.";
    }
    
}
