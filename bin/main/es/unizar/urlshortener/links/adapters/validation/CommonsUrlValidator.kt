package es.unizar.urlshortener.links.adapters.validation

import es.unizar.urlshortener.links.application.UrlValidator
import org.springframework.stereotype.Component
import org.apache.commons.validator.routines.UrlValidator as ApacheUrlValidator

@Component
class CommonsUrlValidator : UrlValidator {
    private val validator = ApacheUrlValidator(arrayOf("http", "https"))

    override fun isValid(url: String): Boolean = validator.isValid(url)
}
