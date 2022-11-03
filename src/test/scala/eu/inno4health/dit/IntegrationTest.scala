package eu.inno4health.dit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import io.onfhir.api.Resource
import io.onfhir.client.OnFhirNetworkClient
import io.onfhir.path.FhirPathEvaluator
import io.onfhir.util.JsonFormatter._
import io.tofhir.engine.mapping.{FhirMappingFolderRepository, FhirMappingJobManager, IFhirMappingRepository, IMappingContextLoader, MappingContextLoader, SchemaFolderRepository}
import io.tofhir.engine.model.{FhirMappingTask, FhirRepositorySinkSettings, FileSystemSource, FileSystemSourceSettings}
import io.tofhir.engine.util.FhirMappingUtility
import org.json4s.JArray
import org.json4s.JsonAST.JObject

import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

class IntegrationTest extends PilotTestSpec {

  val mappingRepository: IFhirMappingRepository =
    new FhirMappingFolderRepository(Paths.get("mappings/inno4health").toAbsolutePath.toUri)

  val contextLoader: IMappingContextLoader = new MappingContextLoader(mappingRepository)

  val schemaRepository = new SchemaFolderRepository(Paths.get("schemas/inno4health").toAbsolutePath.toUri)

  val dataSourceSettings = Map("source" -> FileSystemSourceSettings("test-source-1", "https://inno4health.eu/emg-data", Paths.get("test-data/inno4health").toAbsolutePath.toString))

  val fhirMappingJobManager = new FhirMappingJobManager(mappingRepository, contextLoader, schemaRepository, sparkSession, mappingErrorHandling)

  val fhirSinkSetting: FhirRepositorySinkSettings = FhirRepositorySinkSettings(fhirRepoUrl = "http://localhost:8081/fhir", errorHandling = Some(fhirWriteErrorHandling))
  implicit val actorSystem = ActorSystem("IntegrationTest")

  val onFhirClient = OnFhirNetworkClient.apply(fhirSinkSetting.fhirRepoUrl)

  val fhirServerIsAvailable =
    Try(Await.result(onFhirClient.search("Patient").execute(), FiniteDuration(5, TimeUnit.SECONDS)).httpStatus == StatusCodes.OK)
      .getOrElse(false)

  val observationMappingTask = FhirMappingTask(
    mappingRef = "https://inno4health.eu/fhir/mappings/observation-mapping",
    sourceContext = Map("source" -> FileSystemSource(path = "label1/set1-adductor-side-lying-rep1.csv"))
  )

  "observation mapping" should "map test data" in {
    //Some semantic tests on generated content
    fhirMappingJobManager.executeMappingTaskAndReturn(task = observationMappingTask, sourceSettings = dataSourceSettings) map { mappingResults =>
      val results = mappingResults.map(r => {
        r.mappedResource shouldBe defined
        r.mappedResource.get.parseJson
      })

      results.length shouldBe 8
      (results.apply(1) \ "status").extract[String] shouldBe "final"
      ((results.apply(1) \ "code" \ "coding").extract[Seq[JObject]].head \ "code").extract[String] shouldBe "athlete-activity-emg-data"
      ((results.apply(1) \ "bodySite" \ "coding").extract[Seq[JObject]].head \ "code").extract[String] shouldBe "85629004 | Adductor longus muscle structure |: 272741003 | laterality | = 24028007 | right |"
      ((results.apply(1) \ "component").extract[Seq[JObject]].apply(1) \ "valueString").extract[String] shouldBe "set1"
      ((results.apply(1) \ "component").extract[Seq[JObject]].apply(2) \ "valueString").extract[String] shouldBe "rep1"
      ((results.apply(1) \ "component").extract[Seq[JObject]].apply(3) \ "valueSampledData" \ "data").extract[String].startsWith("0.0003893034798758688 0.007393087659563369") shouldBe true
    }
  }

  it should "map test data and write it to FHIR repo successfully" in {
    //Send it to our fhir repo if they are also validated
    assume(fhirServerIsAvailable)
    fhirMappingJobManager
      .executeMappingJob(tasks = Seq(observationMappingTask), sourceSettings = dataSourceSettings, sinkSettings = fhirSinkSetting)
      .map(unit =>
        unit shouldBe()
      )
  }

}
