package healpix_test

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import healpix.essentials.HealpixBase
import healpix.essentials.Pointing
import healpix.essentials.Scheme.RING

import org.apache.log4j.Level
import org.apache.log4j.Logger

object HelloPixelWorld extends App {
  // Simple object to retrieve pixel number
  // given (theta, phi) position of a point.

  // Set to Level.WARN is you want verbosity
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  // For timing tests
  val T = new Timing()

  // Instanciate our HealpixBase class
  val nside: Int = 512
  val hp = new HealpixBase(nside, RING)

  // Define number of points on the sky
  val npoints = 100000000

  // Our data generator
  val r = scala.util.Random
  def point(n : Int) = {
    r.setSeed(n)
    new Pointing(r.nextFloat * Math.PI, r.nextFloat * 2.0 * Math.PI)
  }

  // Initialise Spark context
  val conf = new SparkConf()
  val sc = new SparkContext(conf)
  var rdd = sc.parallelize((0 to npoints - 1), 1000)
              .map(p => point(p))
              .map(p => hp.ang2pix(p))

  // Trigger the action
  val ipix4 = T.timeit("Spark took", rdd.count())

}
