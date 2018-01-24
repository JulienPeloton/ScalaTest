package grid

import java.io.{File, FileOutputStream}

// Small wrapper around the spark memory profiler
class Prof {
  def estimate[R](text: String, block: => AnyRef): Unit = {
    val size = SizeEstimator.estimate(block) / 1024.0
    println(text + " " + size.toString + " KB")
  }
}

// Timing
class Timing {

  def timeit[R](text: String, block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()

    val dt = t1 - t0
    val sec = dt / 1000000000
    val ns = dt % 1000000000

    println(text + " " + sec + "." + ns + " s")
    result
  }
}
