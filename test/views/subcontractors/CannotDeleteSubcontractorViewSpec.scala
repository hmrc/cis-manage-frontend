package views.subcontractors

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.subcontractors.CannotDeleteSubcontractorView

class CannotDeleteSubcontractorViewSpec extends SpecBase {

  "CannotDeleteSubcontractorView" - {

    "must render the page with the correct content" in new Setup {

      val subcontractorName     = "ABC Ltd"
      val subcontractorsPageUrl = "/subcontractors"

      val html: HtmlFormat.Appendable = view(subcontractorName, subcontractorsPageUrl)
      val doc: Document               = Jsoup.parse(html.body)

      doc.title must include(messages("subcontractors.cannotDeleteSubcontractor.title"))

      doc.select("h1").text must include(
        messages("subcontractors.cannotDeleteSubcontractor.heading", subcontractorName)
      )

      doc.text() must include(messages("subcontractors.cannotDeleteSubcontractor.p1"))
      doc.text() must include(messages("subcontractors.cannotDeleteSubcontractor.p2"))

      doc.text()                                must include(messages("subcontractors.cannotDeleteSubcontractor.p3.text"))
      doc.getElementsByClass("govuk-link").text must include(
        messages("subcontractors.cannotDeleteSubcontractor.p3.link")
      )

      val links = doc.getElementsByClass("govuk-link")

      links.eachAttr("href").contains(subcontractorsPageUrl) mustBe true
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: CannotDeleteSubcontractorView       = app.injector.instanceOf[CannotDeleteSubcontractorView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()

    implicit val messages: Messages =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}
