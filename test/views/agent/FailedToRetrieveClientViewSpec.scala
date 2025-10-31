package views.agent

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.agent.FailedToRetrieveClientView

class FailedToRetrieveClientViewSpec extends SpecBase {

  "FailedToRetrieveClientView" - {

    "must render the page with the correct heading, paragraphs and links" in new Setup {
      val html: HtmlFormat.Appendable = view()
      val doc: Document               = Jsoup.parse(html.body)

      doc.title                                   must include(messages("agent.failedToRetrieveClient.title"))
      doc.select("h1").text                       must include(messages("agent.failedToRetrieveClient.heading"))
      doc.select("p").text                        must include(messages("agent.failedToRetrieveClient.p1"))
      doc.select("li").text                       must include(messages("agent.failedToRetrieveClient.listItem1"))
      doc.select("li").text                       must include(messages("agent.failedToRetrieveClient.listItem2"))
      doc.select("li").text                       must include(messages("agent.failedToRetrieveClient.listItem3"))
      doc.select("p").text                        must include(messages("agent.failedToRetrieveClient.p2"))
      doc.getElementsByClass("govuk-link").text   must include(messages("agent.failedToRetrieveClient.link"))
      doc.select("h2").text                       must include(messages("agent.failedToRetrieveClient.inset.h2"))
      doc.select("p").text                        must include(messages("agent.failedToRetrieveClient.inset.p3"))
      doc.select("p").text                        must include(messages("agent.failedToRetrieveClient.inset.p3"))
      doc.getElementsByClass("govuk-link").text   must include(messages("agent.failedToRetrieveClient.inset.link"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: FailedToRetrieveClientView          = app.injector.instanceOf[FailedToRetrieveClientView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
