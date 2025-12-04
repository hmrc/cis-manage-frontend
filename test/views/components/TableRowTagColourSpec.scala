package views.components

import base.SpecBase
import org.scalatest.matchers.must.Matchers
import views.html.components.Paragraph
import play.api.test.FakeRequest
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Application
import play.twirl.api.Html
import views.html.components.TableRowTagColour

class TableRowTagColourSpec extends SpecBase with Matchers {

  "TableRowTagColour" - {
    "must render the correct text and class for the strong element" in new Setup {
      val html: Html    = view("Some text", "")
      val doc: Document = Jsoup.parse(html.body)

      val strong: Elements = doc.select("strong")
      strong.text mustBe "Some text"
      strong.hasClass("govuk-tag") mustBe true
    }

    "must render a strong element with text and colour class when is provided" in new Setup {
      val html: Html    = view("Some text", "green")
      val doc: Document = Jsoup.parse(html.body)

      val strong: Elements = doc.select("strong")
      strong.text mustBe "Some text"
      strong.hasClass("govuk-tag") mustBe true
      strong.hasClass("green") mustBe true
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: TableRowTagColour                   = app.injector.instanceOf[TableRowTagColour]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )
  }
}
