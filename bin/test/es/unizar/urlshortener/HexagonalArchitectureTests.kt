package es.unizar.urlshortener

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

/**
 * Extra package guards complementary to jMolecules [ModularityTests].
 */
@AnalyzeClasses(
    packages = ["es.unizar.urlshortener"],
    importOptions = [DoNotIncludeTests::class],
)
class HexagonalArchitectureTests {
    @ArchTest
    fun `domain is framework-free`(classes: JavaClasses) {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..adapters..",
                "org.springframework..",
                "jakarta.persistence..",
                "jakarta.servlet..",
            ).because("domain must stay framework-free")
            .check(classes)
    }

    @ArchTest
    fun `application does not depend on adapters`(classes: JavaClasses) {
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..adapters..")
            .because("application depends inward on ports/domain, not on adapters")
            .check(classes)
    }
}
