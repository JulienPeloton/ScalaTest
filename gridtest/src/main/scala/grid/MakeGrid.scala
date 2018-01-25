package grid

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
    Usage: MakeGrid <gridtype> <npoints> <nside>

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
    val data = (for(i <- 1 to n) yield Point(fra, fdec, fz))
      .groupBy(x => grid.index(x.ra, x.dec))

    // Equivalent
    // val data = (1 to n).map(x => Point(fra, fdec, fz))
    //   .groupBy(x => grid.index(x.ra, x.dec))
  }

  // Spark version!
  case class scPoints(n : Int, grid : Grid) {
    val data = sc.parallelize((1 to n), parts)
      .map(x => Point(fra, fdec, fz))
      .groupBy(x => grid.index(x.ra, x.dec))
      // .count
  }

  def main(args: Array[String]) {
    // Check you call the script with the correct number of args
    if (args.length < 3) {
      System.err.println(usage)
      System.exit(1)
    }

    // Define input parameters
    val gridtype = args(0).toString
    println(s"Processing $gridtype grid")

    val npoints = args(1).toInt
    val resolution = args(2).toInt

    // Initialise the Pointing object
    var ptg = new ExtPointing

    // Initialise HealpixBase functionalities
    val hp = new HealpixBase(resolution, RING)

    // Instantiate the grid
    val mygrid = HealpixGrid(resolution, hp, ptg)

    // Put your code below //
    val g = scPoints(npoints, mygrid)

    // Just record the memory usage object case class vs direct tuple
    val po = Point(fra, fdec, fz)
    val sizePoint = P.memory_usage("Point", po)
    println("Partitions = " + (npoints/parts).toString + " points")
    println("Partitions = " + (npoints/parts * sizePoint).toString + " KB")

    // Just le timing pour 1 passage sort + count
    val p = P.timeit(
      s"Sorting and counting $npoints points took ",
      g.data.count)
    println(p.toString + " cells hit!")

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
