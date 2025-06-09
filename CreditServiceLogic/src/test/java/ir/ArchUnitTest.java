package ir;

import org.junit.jupiter.api.Test;

class ArchUnitTest {

    @Test
    void projectShouldHavePomXml() {
        java.io.File pomFile = new java.io.File("pom.xml");
        assert pomFile.exists() : "pom.xml file does not exist in the project root. This project should use Maven.";
    }

}
