package grid

import java.nio.file.{Paths, Files}

import healpix.essentials.HealpixBase
import healpix.essentials.Pointing
import healpix.essentials.Scheme.RING

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import org.apache.log4j.Level
import org.apache.log4j.Logger

// import scala.concurrent._
// import ExecutionContext.Implicits.global

object MakeGrid {
  val usage =
    """
    Usage: MakeGrid <catalog> <nside> <npoints>

    MakeGrid catalog.txt 512 0 // work with catalog.txt on a grid at nside=512
    MakeGrid None 512 10000 // Simulate 10000 points on a grid at nside=512

    """

  // Set to Level.WARN is you want verbosity
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  // Initialise Spark context
  val conf = new SparkConf()
  val sc = new SparkContext(conf)
  val parts = 100

  // Initialise the profiler
  val P = new Profiler

  // Initialise random number generator
  // and set the seed for reproducibility.
  val r = scala.util.Random
  r.setSeed(0)

  /** Convert declination into theta
      @param dec declination coordinate in degree
      @return theta coordinate in radian */
  def dec2theta(dec : Double) : Double = {
    Math.PI / 2.0 - Math.PI / 180.0 * dec
  }

  /** Convert right ascension into phi
      @param dec RA coordinate in degree
      @return phi coordinate in radian */
  def ra2phi(ra : Double) : Double = {
    Math.PI / 180.0 * ra
  }

  /** Convert an array of String into a Point.
      If the redshift is not present, returns Point(ra, dec, 0.0).
      TODO: Need to catch other exceptions on ra and dec!
      @param x array of String ["RA", "Dec", "z"]
      @return Point(ra, dec, z) or Point(ra, dec, 0.0) if z undefined. */
  def convertToPoint(x : Array[String]) : Point = {
    scala.util.Try(
      Point(x(0).toDouble, x(1).toDouble, x(2).toDouble))
    .getOrElse(
      Point(x(0).toDouble, x(1).toDouble, 0.0))
  }

  // Initialise the coordinates (RA/Dec/redshift)
  // Notice that we work in Double (8B each).
  def fra = r.nextFloat() * Math.PI
  def fdec = r.nextFloat() * 2.0 * Math.PI
  def fz = r.nextFloat()

  // Case class for galaxy position
  case class Point(ra: Double, dec: Double, z : Double)

  // Serial version (i.e. pure Scala)
  case class Points(n : Int, grid : Grid) {
    // Use (for).par to parallelize
    val data = (for(i <- 1 to n) yield Point(fra, fdec, fz)) // Create the data
      .groupBy(x => grid.index(x.ra, x.dec)) // Indexing

    // Equivalent
    // val data = (1 to n).map(x => Point(fra, fdec, fz))
    //   .groupBy(x => grid.index(x.ra, x.dec))
  }

  // Spark version - data from catalog or simulation on the fly
  // TODO: Need doc!
  case class scPoints(n : Int, grid : Grid, catalog : String = "") {
    val iscatalog = Files.exists(Paths.get(catalog))

    // My basic operations (common for all the rest)
    val data = if (iscatalog) {
      sc.textFile(catalog, parts) // distribute the data from the cat
        .map(x => x.split(" ")) // make Array[String]
        .map(x => convertToPoint(x)) // Convert into Point
        .groupBy(x => grid.index(dec2theta(x.dec), ra2phi(x.ra))) // Indexing
    } else {
      sc.parallelize((1 to n), parts) // distribute the data
        .map(x => Point(fra, fdec, fz)) // Yield point
        .groupBy(x => grid.index(x.ra, x.dec)) // Indexing
    }
  }

  def main(args: Array[String]) {
    // Check you call the script with the correct number of args
    if (args.length < 3) {
      System.err.println(usage)
      System.exit(1)
    }

    // Define input parameters
    val catalog = args(0).toString
    val resolution = args(1).toInt
    val npoints = args(2).toInt

    val iscatalog = Files.exists(Paths.get(catalog))
    if (iscatalog) {
      println(s"Reading data from catalog $catalog")
    } else println(s"Simulate data on-the-fly with $npoints points")

    // Initialise the Pointing object
    var ptg = new ExtPointing

    // Initialise HealpixBase functionalities
    val hp = new HealpixBase(resolution, RING)

    // Instantiate the grid
    val mygrid = HealpixGrid(resolution, hp, ptg)

    // Put your code below //
    val g = scPoints(npoints, mygrid, catalog)

    // Just record the memory usage object case class vs direct tuple
    val po = Point(fra, fdec, fz)
    val sizePoint = P.memory_usage("Point", po)
    println("Partitions = " + (npoints/parts).toString + " points")
    println("Partitions = " + (npoints/parts * sizePoint).toString + " KB")

    // Just the timing for sort + count
    val p = P.timeit(
      s"Sorting and counting took ",
      g.data.count)
    println(p.toString + " cells hit!")

    // Find the maximum hit
    val max = P.timeit(
      s"Finding the max ",
      g.data.map(x => x._2.size).reduce((x, y) => if (x > y) x else y))
    println("The maximum number of galaxies in one pixel is " + max.toString)

    // Select only high density region using some threshold based on the max.
    val sub = P.timeit(
      s"Filtering took ",
      g.data.filter(_._2.size > max/50.0).map(x => x._1))
    println(sub.count.toString + " filtered pixels found!")

    // Write the output
    P.timeit("Writing... took", sub.coalesce(1, true).saveAsTextFile("output/"))

    // Size of the gridded points.
    val m = P.memory_usage("Partition weights ", g)


    // Below is for pure Scala, i.e. using Points
    // println("Sample of 10 points")
    // for ((k, v) <- p.data.takeRight(10)) {
    //   println(s"nside=$resolution : pixnum=$k points=${v}")
    // }
    // println("Sample of 10 points")
    // for ((k, v) <- p.data.take(10)) {
    //   println(s"nside=$resolution : pixnum=$k points=${v}")
    // }

    // Memory profiling for several iterations
    // val pts = for (i <- 0 to 2) yield P.memory_usage(
    //   "Iteration " + i.toString + " (" +
    //     Points(npoints, mygrid)
    //     .data
    //     .toList
    //     .length
    //     .toString +
    //   s" unique cells / $npoints initial points) - size : ",
    //   Points(npoints, mygrid))
    // P.memory_usage("grille", p)

  }
}
