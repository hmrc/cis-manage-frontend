package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.UnsuccessfulAutomaticSubcontractorUpdateView
class UnsuccessfulAutomaticSubcontractorUpdateViewSpec extends SpecBase {

  "UnsuccessfulAutomaticSubcontractorUpdateView" - {

    "must render the page with the correct heading, paragraphs and buttons" in new Setup {
      val html: HtmlFormat.Appendable = view()
      val doc: Document               = Jsoup.parse(html.body)

      doc.title                                   must include(messages("unsuccessfulAutomaticSubcontractorUpdate.title"))
      doc.select("h1").text                       must include(messages("unsuccessfulAutomaticSubcontractorUpdate.heading"))
      doc.select("p").text                        must include(messages("unsuccessfulAutomaticSubcontractorUpdate.p1"))
      doc.select("p").text                        must include(messages("unsuccessfulAutomaticSubcontractorUpdate.p2"))
      doc.select("p").text                        must include(messages("unsuccessfulAutomaticSubcontractorUpdate.p3"))
      doc.getElementsByClass("govuk-button").text must include(messages("site.continue"))

    }
  }

  trait Setup {
    val app: Application                                   = applicationBuilder().build()
    val view: UnsuccessfulAutomaticSubcontractorUpdateView =
      app.injector.instanceOf[UnsuccessfulAutomaticSubcontractorUpdateView]
    implicit val request: play.api.mvc.Request[_]          = FakeRequest()
    implicit val messages: Messages                        = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
