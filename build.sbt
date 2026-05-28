import com.briskware.sbt.columnar.ColumnarFormatPlugin.autoImport.ColumnarSection
import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "cis-manage-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

onLoad in Global ~= (_ andThen ("git config core.hooksPath hooks" :: _))

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    scalaSettings,
    defaultSettings(),
    Test / parallelExecution := false,
    Test / fork := false,
    Runtime / fork := true,
    routesGenerator := InjectedRoutesGenerator,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 6996,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;" +
      ".*Routes.*;.*viewmodels.govuk.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 78,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Wconf:src=html/.*:s",
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(concat),
    columnarFmtConfig := Seq(
      ColumnarConfig(
        sections        = appRoutesSections,
        lineLimit       = 160 * 1,
        fileGlob        = "conf/app.routes",
        fileHeader      = "# Routes",
        formatterConfig = ColumnarFormatterConfig.playRoutes
      ),
      ColumnarConfig(
        sections        = messagesSections,
        lineLimit       = 120 * 1,
        fileGlob        = "conf/messages.*",
        formatterConfig = ColumnarFormatterConfig(
          parse        = ColumnarFormatterConfig.playRoutes.parse,
          primaryCol   = 0,
          secondaryCol = 0,
          dedupeKey    = cols => cols(0),
          subkeyFn     = cols => {
            val key      = cols(0)
            val firstDot = key.indexOf('.')
            if (firstDot < 0) key
            else {
              val secondDot = key.indexOf('.', firstDot + 1)
              if (secondDot >= 0) key.take(secondDot)
              else key.take(firstDot)
            }
          }
        )
      )
    )
  )

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")

lazy val messagesSections = Seq(
  ColumnarSection("# Infrastructure",
    primaryPrefixes = Seq("service", "site", "date", "error", "timeout", "index",
                          "checkYourAnswers", "journeyRecovery", "signedOut")),
  ColumnarSection("# Errors & Auth",
    primaryPrefixes = Seq("pageNotFound", "systemError", "accessDenied", "unauthorised")),
  ColumnarSection("# Introduction & Landing",
    primaryPrefixes = Seq("introduction", "contractorLanding", "returnsLanding")),
  ColumnarSection("# Agent",
    primaryPrefixes = Seq("agent")),
  ColumnarSection("# Subcontractors",
    primaryPrefixes = Seq("subcontractorsLandingPage", "retrievingSubcontractors",
                          "checkSubcontractorRecords", "successfulAutomaticSubcontractorUpdate",
                          "unsuccessfulAutomaticSubcontractorUpdate", "successfulNoRecordsFound")),
  ColumnarSection("# Returns & Submissions",
    primaryPrefixes = Seq("submissionConfirmation", "incompleteReturns", "addContractorDetails")),
  ColumnarSection("# History",
    primaryPrefixes = Seq("history")),
  ColumnarSection("# Delete",
    primaryPrefixes = Seq("delete")),
  ColumnarSection("# Amend",
    primaryPrefixes = Seq("amend")),
  ColumnarSection("# Notices & Statements",
    primaryPrefixes = Seq("manageNoticesStatements")),
  ColumnarSection("# Verify",
    primaryPrefixes = Seq("verify")),
  ColumnarSection("# Client Details",
    primaryPrefixes = Seq("clientdetails")),
)

lazy val appRoutesSections = Seq(
  ColumnarSection("# Infrastructure"),
  ColumnarSection("# Errors & Auth",
    primaryPrefixes = Seq("/there-is-a-problem", "/page-not-found", "/access-denied",
      "/account/", "/unauthorised", "/system-error/")),
  ColumnarSection("# Agent",
    primaryPrefixes   = Seq("/agent/"),
    secondaryPrefixes = Seq("controllers.agent.")),
  ColumnarSection("# Contractor Landing",
    primaryPrefixes   = Seq("/org/"),
    secondaryPrefixes = Seq("controllers.contractor.")),
  ColumnarSection("# Introduction",
    primaryPrefixes   = Seq("/sign-into-cis")),
  ColumnarSection("# Subcontractors",
    primaryPrefixes   = Seq("/your-subcontractors/", "/check-subcontractor-records/",
      "/automatic-subcontractor-update", "/manage-subcontractors/")),
  ColumnarSection("# Returns Landing",
    primaryPrefixes   = Seq("/manage-cis-return/", "/manage-your-cis-return/")),
  ColumnarSection("# Delete",
    primaryPrefixes   = Seq("/delete/"),
    secondaryPrefixes = Seq("controllers.delete.")),
  ColumnarSection("# History",
    primaryPrefixes   = Seq("/history/", "/monthly-return/"),
    secondaryPrefixes = Seq("controllers.history.")),
  ColumnarSection("# Amend",
    primaryPrefixes   = Seq("/amend-monthly-return/"),
    secondaryPrefixes = Seq("controllers.amend.")),
  ColumnarSection("# Notices & Statements",
    primaryPrefixes   = Seq("/manage-notices-statements/"),
    secondaryPrefixes = Seq("controllers.notices.")),
  ColumnarSection("# Verify",
    primaryPrefixes   = Seq("/verify/"),
    secondaryPrefixes = Seq("controllers.verify.")),
  ColumnarSection("# Client Details",
    primaryPrefixes   = Seq("/client-details/"),
    secondaryPrefixes = Seq("controllers.clientdetails.")),
  ColumnarSection("# Add Contractor Details",
    primaryPrefixes   = Seq("/add-contractor-details")),
)
