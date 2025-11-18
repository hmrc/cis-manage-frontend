package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.components.ParagraphWithButton

class ParagraphWithButtonSpec extends SpecBase with Matchers {

  "ParagraphWithButton" - {

    "render a paragraph containing a GOV.UK button with the correct attributes" in new Setup {
      val html: HtmlFormat.Appendable = paragraphWithButton(buttonContent)
      val doc: Document               = parse(html)
      val paragraph: Elements         = doc.select("p")
      val button: Elements            = doc.select("button")

      paragraph.size                   mustBe 1
      paragraph.hasClass("govuk-body") mustBe true

      button.size                     mustBe 1
      button.hasClass("govuk-button") mustBe true
      button.attr("type")             mustBe "submit"
      button.attr("data-module")      mustBe "govuk-button"
      button.text                     mustBe buttonContent
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val paragraphWithButton: ParagraphWithButton  = app.injector.instanceOf[ParagraphWithButton]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )

    val buttonContent = "Continue"
    def parse(html: HtmlFormat.Appendable): Document = Jsoup.parse(html.body)
  }
}
