package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.components.List as ListView

class ListSpec extends SpecBase with Matchers {

  "li" - {
    "must render a single item correctly" in new Setup {
      val liItem = "CIS List Item"
      val html = li(Seq(Html(liItem)))
      val liElements = getListItemElement(html)

      liElements.size mustBe 1
      liElements.first.text mustEqual liItem
    }


    "must render multiple list items correctly" in new Setup {
      val listItems = Seq("Item 1", "Item 2", "Item 3").map(item => Html(item))
      val html = li(listItems)
      val liListElements = getListItemElement(html)

      liListElements.size mustBe 3
      liListElements.get(0).text mustEqual "Item 1"
    }

    "must correctly apply the govuk-list govuk-list--bullet class to the ul tag" in new Setup {
      val liItem = "CIS List Item"
      val html = li(Seq(Html(liItem)))
      val doc = Jsoup.parse(html.body)

      doc.select("ul").first().attr("class") must include("govuk-list govuk-list--bullet")
    }

    "must render an empty ul when the content is an empty Seq" in new Setup {
      val emptyList = li(Seq.empty[Html])
      val doc = Jsoup.parse(emptyList.body)

      doc.select("li") must have size 0
      doc.select("ul") must have size 1
    }
  }

  trait Setup {
    val app: Application                          = applicationBuilder().build()
    val li: ListView                              = app.injector.instanceOf[ListView]
    implicit val request: play.api.mvc.Request[_] = FakeRequest()
    implicit val messages: Messages               = play.api.i18n.MessagesImpl(
      play.api.i18n.Lang.defaultLang,
      app.injector.instanceOf[play.api.i18n.MessagesApi]
    )

    def getListItemElement(html: Html): Elements = {
      val doc = Jsoup.parse(html.body)
      doc.select("li")
    }
  }
}
