package example

// import org.scalatest._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

class countSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  /*
  Test class for countme object.
  Check that an empty file returns 0 word,
  and check the size of a test file.
  */

  // TODO: Need to understand if this is needed and why:
  private val master = "local[2]"
  private val appName = "countme"

  private var sc: SparkContext = _

  def BeforeAll() {
    val conf = new SparkConf()
      .setMaster(master)
      .setAppName(appName)
      // .set("spark.driver.allowMultipleContexts", "true")

    sc = new SparkContext(conf)
  }

  def AfterAll {
    if (sc != null) {
      sc.stop()
    }
  }
  // END TODO

  // Test an empty file
  "An empty file" should "have no word!" in {
    countWords.countme("data/empty.txt", false) shouldEqual 0
  }

  // Test a regular file
  "Principia by Newton" should "have 129363 words!" in {
    countWords.countme("data/principia.txt", true) shouldEqual 129363
  }

  // Test a regular file
  // val message = "Usage: countme <filename.txt> <interactive (true/false)>"
  // "Program" should "complain if args are missing" in {
  //   countWords.main(Array(" ")) shouldEqual message
  // }
}
