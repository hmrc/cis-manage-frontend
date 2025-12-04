package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.components.ParagraphWithLinks

class ParagraphWithLinksSpec extends SpecBase {

  "ParagraphWithLinks" - {

    "must render a paragraph with text and 2 links with bold an new tab" in new Setup {
      val html: HtmlFormat.Appendable = view(
        linkTextOne,
        linkUrlOne,
        linkTextTwo,
        linkUrlTwo,
        prefixTextKey = prefixText,
        suffixTextKey = suffixText,
        bold = true,
        isNewTab = true
      )

      val paragraphElement: Elements = getParagraphElement(html)
      paragraphElement.hasClass("govuk-body") mustBe true
      val links: Elements            = paragraphElement.select("a")
      links.size() mustBe 2

      val firstLink: Element = links.get(0)
      firstLink.text mustBe linkTextOne
      firstLink.attr("href") mustBe linkUrlOne
      firstLink.attr("rel") mustBe "noreferrer noopener"
      firstLink.attr("target") mustBe "_blank"
      firstLink.hasClass("govuk-link govuk-!-font-weight-bold")

      val secondLink: Element = links.get(1)
      secondLink.text mustBe linkTextTwo
      secondLink.attr("href") mustBe linkUrlTwo
      secondLink.attr("rel") mustBe "noreferrer noopener"
      secondLink.attr("target") mustBe "_blank"
      secondLink.hasClass("govuk-link govuk-!-font-weight-bold")

      val paragraphText: String = paragraphElement.text()
      paragraphText must include(prefixText)
      paragraphText must include(suffixText)
    }

    "must render a paragraph with text and links with no bold text" in new Setup {
      val html: HtmlFormat.Appendable = view(
        linkTextOne,
        linkUrlOne,
        linkTextTwo,
        linkUrlTwo,
        prefixTextKey = prefixText,
        suffixTextKey = suffixText
      )

      val paragraphElement: Elements = getParagraphElement(html)
      paragraphElement.hasClass("govuk-body") mustBe true
      val links: Elements            = paragraphElement.select("a")
      links.size() mustBe 2

      val firstLink: Element = links.get(0)
      firstLink.text mustBe linkTextOne
      firstLink.attr("href") mustBe linkUrlOne
      firstLink.hasAttr("rel") mustBe false
      firstLink.hasAttr("target") mustBe false
      firstLink.hasClass("govuk-link") mustBe true

      val secondLink: Element = links.get(1)
      secondLink.text mustBe linkTextTwo
      secondLink.attr("href") mustBe linkUrlTwo
      secondLink.hasAttr("rel") mustBe false
      secondLink.hasAttr("target") mustBe false
      secondLink.hasClass("govuk-link") mustBe true

      val paragraphText: String = paragraphElement.text()
      paragraphText must include(prefixText)
      paragraphText must include(suffixText)

    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val view: ParagraphWithLinks                  = app.injector.instanceOf[ParagraphWithLinks]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )

    val paragraphWithLinkText = "Test paragraph content."
    val linkTextOne           = "First link"
    val linkUrlOne            = "/first-link-url"
    val linkTextTwo           = "Second link"
    val linkUrlTwo            = "/second-link-url"
    val prefixText            = "Jump to"
    val suffixText            = "After link text"

    def getParagraphElement(html: play.twirl.api.Html): org.jsoup.select.Elements = {
      val doc = Jsoup.parse(html.body)
      doc.select("p")
    }
  }
}
