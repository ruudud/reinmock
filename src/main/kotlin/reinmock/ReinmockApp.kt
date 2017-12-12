package reinmock

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import no.api.freemarker.java8.Java8ObjectWrapper
import java.time.LocalDate

data class Mark(val firstName: String, val lastName: String, val id: Int? = null, val regDate: LocalDate? = null,
                val districtName: String = "", val c1: String = "", val c2: String = "", val c3: String = "",
                val c4: String = "", val c5: String = "", val c6: String = "")

val blankCutsAndDistrictMark = Mark("Mia Ánnelé", "Bals", -1, LocalDate.of(2007, 3, 1))
val blankDistrict = Mark("KRISTINE M. SARA", "BULJO", 49, LocalDate.of(1936, 11, 13), c1 = "aga", c3 = "a", c4 = "ag", c6 = "aa")
val blankAllButCutMark = Mark("", "", c2 = "p")
val plainMark = Mark("Niila", "Blind", -1, LocalDate.of(2003, 12, 22))

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(FreeMarker) {
        objectWrapper = Java8ObjectWrapper(Configuration.VERSION_2_3_23)
        templateLoader = ClassTemplateLoader(Application::class.java.classLoader, "templates")
    }
    install(Routing) {
        get("/merkeregister/innsyn/") {
            call.respond(FreeMarkerContent("frontpage.ftl", {}))
        }
        get("/merkeregister/innsyn/Merkedetaljer.aspx") {
            val markId: Int = call.request.queryParameters.get("merkenr").orEmpty().toInt()
            when (markId) {
                in 0..5000 -> {
                    val mark: Mark = when {
                        markId % 10 == 1 -> blankCutsAndDistrictMark.copy(id = markId)
                        markId % 10 == 2 -> blankAllButCutMark.copy()
                        markId % 10 == 3 -> blankDistrict.copy(id = markId)
                        else -> plainMark.copy(id = markId, districtName = getPseudoRandomDistrict(markId))
                    }
                    call.respond(FreeMarkerContent("mark.ftl", mapOf("mark" to mark)))
                }
                else -> call.respondRedirect("/merkeregister/innsyn/Feilside.aspx")
            }
        }
        get("/") {
            call.respondText("Reinmerker Mocks", ContentType.Text.Html)
        }
        get("/merkeregister/innsyn/Feilside.aspx") {
            call.respondText("En slags 404", ContentType.Text.Html)
        }
    }
}