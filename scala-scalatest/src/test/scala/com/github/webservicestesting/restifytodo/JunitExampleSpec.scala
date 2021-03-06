package com.github.webservicestesting.restifytodo

import com.github.webservicestesting.model.QueryResult
import com.github.webservicestesting.model.QueryResult.Query
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.jayway.restassured.http.ContentType
import com.jayway.restassured.response.{Header, Response}
import com.tngtech.java.junit.dataprovider.{DataProviderRunner, DataProvider, UseDataProvider}
import org.apache.commons.lang3.StringEscapeUtils
import org.assertj.core.api.{SoftAssertions, JUnitSoftAssertions}
import org.junit.runner.RunWith
import org.junit.{Before, Rule, Test}
import org.scalatest.Matchers
import org.scalatest.junit.{ShouldMatchersForJUnit, AssertionsForJUnit}

import scala.collection.mutable

class JunitExampleSpec extends AssertionsForJUnit with Matchers {

  @Before def initBaseUrl {
    RestAssured.baseURI = "https://query.yahooapis.com"
    RestAssured.basePath = "/v1/public/yql"
  }
  @Test def localSearchByZipCodeReturnsCompleteResponse {
    val softly: SoftAssertions = new SoftAssertions
    val zipCode: String = "97006"
    val query: String = "pizza"
    val businessToTest: String = "Bellagios Pizza"
    val response: Response = given.header(new Header("Accept-Encoding", "gzip, deflate")).log.all.queryParam("q", generateSearchQuery(zipCode, query)).queryParam("format", "json").accept(ContentType.JSON).get
    softly.assertThat(response.getStatusCode).isEqualTo(200)
    softly.assertThat(response.getHeader("Content-Type")).contains("application/json")
    val queryResult: QueryResult = response.then.log.all.extract.as(classOf[QueryResult])
    val queryResultQuery: QueryResult.Query = queryResult.getQuery
    softly.assertThat(queryResultQuery.getCount).isEqualTo(10)
    softly.assertThat(queryResultQuery.getLang).isEqualToIgnoringCase("en-us")
    softly.assertAll()
    val results: mutable.Buffer[Query.SearchResult] = scala.collection.JavaConversions.asScalaBuffer(queryResultQuery.getResults.getResult)
    val resultsWithReviews = results.filter( s => !(s.getRating().getTotalReviews() == "0") ).map(x=> x.getTitle())
    resultsWithReviews should contain (businessToTest)
  }

  protected def generateSearchQuery(zip: String, query: String): String = {
    val templateString: String = s"select * from local.search where zip=${zip} and query='${query}'"
    return StringEscapeUtils.escapeHtml4(templateString)
  }
}
