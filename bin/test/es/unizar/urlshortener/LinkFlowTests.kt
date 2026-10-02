package es.unizar.urlshortener

import com.jayway.jsonpath.JsonPath.read
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
class LinkFlowTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `create then redirect updates stats`() {
        val create =
            mockMvc
                .perform(
                    post("/api/link")
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("url", "https://example.com/seed"),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.hash").isString)
                .andReturn()

        val hash = read<String>(create.response.contentAsString, "$.hash")

        mockMvc
            .perform(get("/$hash"))
            .andExpect(status().isTemporaryRedirect)
            .andExpect(header().string("Location", "https://example.com/seed"))

        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        var ok = false
        while (System.nanoTime() < deadline) {
            val result = mockMvc.perform(get("/api/stats/$hash")).andReturn()
            if (result.response.status == 200 && result.response.contentAsString.contains("\"totalClicks\":1")) {
                ok = true
                break
            }
            Thread.sleep(100)
        }
        assert(ok) { "link stats did not reach totalClicks=1 within timeout" }
    }
}
