package es.unizar.urlshortener

import org.jmolecules.archunit.JMoleculesArchitectureRules.VerificationDepth.SEMI_STRICT
import org.jmolecules.archunit.JMoleculesArchitectureRules.ensureHexagonal
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules.of
import org.springframework.modulith.core.VerificationOptions.defaults
import org.springframework.modulith.docs.Documenter

class ModularityTests {
    private val modules = of(Application::class.java)

    @Test
    fun `verifies module structure and hexagonal stereotypes`() {
        val hexagonal = ensureHexagonal(SEMI_STRICT)
        modules.verify(defaults().withAdditionalVerifications(hexagonal))
    }

    @Test
    fun `writes module documentation`() {
        Documenter(modules).writeDocumentation()
    }
}
