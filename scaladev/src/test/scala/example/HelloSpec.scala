package example

import org.scalatest._

class HelloSpec extends FlatSpec with Matchers {
  "The Hello object" should "say its argument" in {
    Hello.main("Toto") shouldEqual "Toto"
  }
}
