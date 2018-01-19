/**
  * FILE : Timing.scala
  * PATH : astrospark.sims
  * AUTHOR : Christian Arnault <arnault@lal.in2p3.fr>
  * AUTHOR : Julien Peloton <peloton@lal.in2p3.fr>
  */
package healpix_test

import java.io.{File, FileOutputStream}

class Timing {
  // val fos = new FileOutputStream(new File(filename))

  def timeit[R](text: String, block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()

    val dt = t1 - t0
    val sec = dt / 1000000000
    val ns = dt % 1000000000

    println(text + " " + sec + "." + ns)
    // Console.withOut(fos) {
    //   println(text + " " + sec + "." + ns) }
    result
  }
}
