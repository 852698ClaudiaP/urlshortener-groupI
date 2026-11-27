package es.unizar.urlshortener.links.adapters.web

import es.unizar.urlshortener.links.application.LinkCSVService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/links")
class LinkCSVController(
    private val linkCSVService: LinkCSVService,
) {
    @PostMapping(
        "/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun upload(
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ByteArray> {
        val csv = linkCSVService.processCsv(file.inputStream)

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType("text/csv"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition
                    .attachment()
                    .filename("shortened-links.csv")
                    .build()
                    .toString(),
            ).body(csv)
    }
}
