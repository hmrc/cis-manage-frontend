package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.CheckSubcontractorRecordsView

class CheckSubcontractorRecordsViewSpec extends SpecBase {
  "UnsuccessfulAutomaticSubcontractorUpdateView" - {

    "must render the page with the correct heading, paragraphs and button" in new Setup {
      val html: HtmlFormat.Appendable = view()
      val doc: Document               = Jsoup.parse(html.body)

      doc.title must include(messages("checkSubcontractorRecords.title"))
      doc.select("h1").text must include(messages("checkSubcontractorRecords.heading"))
      doc.select("p").text must include(messages("checkSubcontractorRecords.p1"))
      doc.select("p").text must include(messages("checkSubcontractorRecords.p2"))
      doc.getElementsByClass("govuk-button").text must include(messages("site.continue"))

    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    val view: CheckSubcontractorRecordsView       =  app.injector.instanceOf[CheckSubcontractorRecordsView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
