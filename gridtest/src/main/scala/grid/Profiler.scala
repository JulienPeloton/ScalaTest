package grid

import java.io.{File, FileOutputStream}

import org.apache.spark.util.SizeEstimator

/** Wrapper for monitoring execution time and memory usage of object */
class Profiler {

  /** Method to estimate the memory usage of an object.
      @constructor Profiler.memory_usage("text", object)
      @param text Message to print after the execution
      @param block object to be monitored
      @return Unit */
  def memory_usage[R](text : String, block : => AnyRef) : Unit = {
    val size = SizeEstimator.estimate(block) / 1024.0
    println(text + " " + size.toString + " KB")
  }

  /** Method to estimate the execution time of a block R.
      @constructor val t = Profiler.timeit("text", function)
      @param text Message to print after the execution
      @param block code block to be benchmarked
      @return the block of code */
  def timeit[R](text : String, block: => R): R = {
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
