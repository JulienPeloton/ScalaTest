package grid

import healpix.essentials.HealpixBase
import healpix.essentials.Pointing
import healpix.essentials.Scheme.RING

import scala.concurrent._
import ExecutionContext.Implicits.global

object MakeGrid {
  val usage =
    """
    Usage: MakeGrid <gridtype> <npoints> <nside>

    """

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

  case class Points(n : Int, grid : Grid) {
    // Use (for).par to parallelize
    val data = (for(i <- 1 to n) yield Point(fra, fdec, fz))
      .groupBy(x => grid.index(x.ra, x.dec))

    // Equivalent
    // val data = (1 to n).map(x => Point(fra, fdec, fz))
    //   .groupBy(x => grid.index(x.ra, x.dec))
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
    var ptg = new Pointing

    // Initialise HealpixBase functionalities
    val hp = new HealpixBase(resolution, RING)

    val mygrid = HealpixGrid(resolution, hp, ptg)

    // Put your code below //

    // Just record the memory usage object case class vs direct tuple
    val po = Point(fra, fdec, fz)
    P.memory_usage("Point", po)
    P.memory_usage("Point tuple", (fra, fdec, fz))

    // Just le timing pour 1 passage
    val p = P.timeit(
      s"Processing for $npoints initial points took ", Points(npoints, mygrid))

    println("Sample of 10 points")
    for ((k, v) <- p.data.takeRight(10)) {
      println(s"nside=$resolution : pixnum=$k points=${v}")
    }

    // Memory profiling for several iterations
    val pts = for (i <- 0 to 2) yield P.memory_usage(
      "Iteration " + i.toString + " (" +
        Points(npoints, mygrid)
        .data
        .toList
        .length
        .toString +
      s" unique cells / $npoints initial points) - size : ",
      Points(npoints, mygrid))
    // P.memory_usage("grille", p)

  }
}
