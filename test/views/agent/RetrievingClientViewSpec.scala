package views.agent

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.agent.RetrievingClientView

class RetrievingClientViewSpec extends SpecBase {

  "RetrievingClientView" - {

    "must render the page with the correct heading, paragraphs and link" in new Setup {
      val html: HtmlFormat.Appendable = view()
      val doc: Document               = Jsoup.parse(html.body)


      doc.title                                 must include(messages("agent.retrievingClient.title"))
      doc.select("h1").text                     must include(messages("agent.retrievingClient.h1"))
      doc.select("p").text                      must include(messages("agent.retrievingClient.p1"))
      doc.select("p").text                      must include(messages("agent.retrievingClient.p2"))
      doc.select("p").text                      must include(messages("agent.retrievingClient.inset.p3"))
      doc.select("p").text                      must include(messages("agent.retrievingClient.inset.p4"))
      doc.select("h2").text                     must include(messages("agent.retrievingClient.inset.h2"))
      doc.getElementsByClass("govuk-link").text must include(messages("agent.retrievingClient.inset.link"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: RetrievingClientView                = app.injector.instanceOf[RetrievingClientView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
