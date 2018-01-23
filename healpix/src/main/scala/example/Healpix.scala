package healpix_test

import jep.NDArray
import jep.JepConfig
import jep.Jep

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
  // val npoints = 10000

  // Our data generator
  val r = scala.util.Random
  def point(n : Int) = {
    r.setSeed(n)
    new Pointing(r.nextFloat * Math.PI, r.nextFloat * 2.0 * Math.PI)
  }

  // SPARK BENCHMARK
  // // Initialise Spark context
  // val conf = new SparkConf()
  // val sc = new SparkContext(conf)
  // var rdd = sc.parallelize((0 to npoints - 1), 1000)
  //             .map(p => point(p))
  //             .map(p => hp.ang2pix(p))
  //
  // // Trigger the action
  // val ipix = T.timeit("ang2pix Java took", rdd.count())


  def point_tuple(n : Int) {
    r.setSeed(n)
    (r.nextFloat * Math.PI, r.nextFloat * 2.0 * Math.PI)
  }

  val jep = new Jep(new JepConfig().addSharedModules("healpy"))
  jep.eval("import healpy")
  // Workaround... cannot access tuple info
  val theta = Math.PI
  val phi = 0.0

  for (exp <- 3 to 6) {
    var npoints = scala.math.pow(10, exp).toInt
    println("NGAL = ", npoints)
    // Pure Healpix Java benchmark in scala
    var bench_healpix = T.timeit("ang2pix Java took", (0 to npoints - 1)
                .map(p => point(p))
                .map(p => hp.ang2pix(p)))

    // JEP benchmark
    var bench_jep = T.timeit("ang2pix python + jep took", (0 to npoints - 1)
                .map(p => point_tuple(p))
                .map(p => jep.getValue(s"healpy.ang2pix($nside, $theta, $phi)")))
  }

}
