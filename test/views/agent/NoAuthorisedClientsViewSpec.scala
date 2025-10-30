package views.agent

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.agent.NoAuthorisedClientsView

class NoAuthorisedClientsViewSpec extends SpecBase {

  "NoAuthorisedClientsView" - {

    "must render the page with the correct heading, paragraphs, list items and links" in new Setup {
      val html: HtmlFormat.Appendable = view()
      val doc: Document = Jsoup.parse(html.body)

      doc.title                                 must include(messages("agent.noAuthorisedClients.title"))
      doc.select("h1").text                     must include(messages("agent.noAuthorisedClients.heading"))
      doc.select("p").text                      must include(messages("agent.noAuthorisedClients.p1"))
      doc.select("p").text                      must include(messages("agent.noAuthorisedClients.p2"))
      doc.select("h2").text                     must include(messages("agent.noAuthorisedClients.h2"))
      doc.select("p").text                      must include(messages("agent.noAuthorisedClients.p3"))
      doc.select("li").text                     must include(messages("agent.noAuthorisedClients.listItem1"))
      doc.getElementsByClass("govuk-link").text must include(messages("agent.noAuthorisedClients.listItem1.link"))
      doc.select("li").text                     must include(messages("agent.noAuthorisedClients.listItem2"))
      doc.getElementsByClass("govuk-link").text must include(messages("agent.noAuthorisedClients.listItem2.link"))
      doc.select("li").text                     must include(messages("agent.noAuthorisedClients.listItem2.afterLinkText"))
      doc.select("p").text                      must include(messages("agent.noAuthorisedClients.p4"))
      doc.getElementsByClass("govuk-link").text must include(messages("agent.noAuthorisedClients.returnToHome.link"))
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: NoAuthorisedClientsView             = app.injector.instanceOf[NoAuthorisedClientsView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
