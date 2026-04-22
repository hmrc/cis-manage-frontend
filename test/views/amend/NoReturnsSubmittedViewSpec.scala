package views.amend

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.amend.NoReturnsSubmittedView

class NoReturnsSubmittedViewSpec extends SpecBase {

  "NoReturnsSubmittedView" - {

    "must render the page with the correct content" in new Setup {
      val html: HtmlFormat.Appendable = view(cisId = "1")
      val doc: Document               = Jsoup.parse(html.toString)

      doc.title             must include(messages("amend.noReturnsSubmitted.title"))
      doc.select("h1").text must include(messages("amend.noReturnsSubmitted.heading"))

      doc.select("p.govuk-body").text must include(messages("amend.noReturnsSubmitted.p1"))

      doc.select("p.govuk-body").text must include(messages("amend.noReturnsSubmitted.p2"))
      doc.select("a.govuk-link").text must include(messages("amend.noReturnsSubmitted.p2.link"))
      doc.select("p.govuk-body").text must include(messages("amend.noReturnsSubmitted.p3"))
      doc.select("a.govuk-link").text must include(messages("amend.noReturnsSubmitted.p3.link"))
    }
  }

  trait Setup {
    val app: Application             = applicationBuilder().build()
    val view: NoReturnsSubmittedView = app.injector.instanceOf[NoReturnsSubmittedView]

    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               =
      play.api.i18n.MessagesImpl(
        play.api.i18n.Lang.defaultLang,
        app.injector.instanceOf[play.api.i18n.MessagesApi]
      )
  }
}
