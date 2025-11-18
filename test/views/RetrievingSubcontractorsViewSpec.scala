package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.RetrievingSubcontractorsView

class RetrievingSubcontractorsViewSpec extends SpecBase {

  "RetrievingSubcontractorsView" - {

    "must render the header and paragraphs on the page" in new Setup {
      val html  = view()
      val doc   = Jsoup.parse(html.body)

      doc.title must include(messages("retrievingSubcontractors.title"))
      doc.select("h1").text must include(messages("retrievingSubcontractors.heading"))
      doc.select("p").text must include(messages("retrievingSubcontractors.p1"))
      doc.select("p").text must include(messages("retrievingSubcontractors.p2"))
      doc.select("p").text must include(messages("retrievingSubcontractors.p3.bold"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: RetrievingSubcontractorsView                    = app.injector.instanceOf[RetrievingSubcontractorsView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
