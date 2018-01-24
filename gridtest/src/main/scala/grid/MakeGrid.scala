package grid

import org.apache.spark.util.SizeEstimator

import healpix.essentials.HealpixBase
import healpix.essentials.Pointing
import healpix.essentials.Scheme.RING

import scala.concurrent._
import ExecutionContext.Implicits.global


object HealpixMap {

  // Initialise the profiler
  val P = new Prof
  val T = new Timing
  var ptg = new Pointing

  // Initialise random number generator
  // and set the seed
  val r = scala.util.Random
  r.setSeed(0)

  // Initialise the coordinates (RA/Dec/redshift)
  def fra = r.nextFloat() * Math.PI
  def fdec = r.nextFloat() * 2.0F * Math.PI
  def fz = r.nextFloat()

  // Case class for galaxy positions
  case class Point(ra: Double, dec: Double, z : Double)

  // Just record the memory usage object case class vs direct tuple
  val po = Point(fra, fdec, fz)
  P.estimate("Point", po)
  P.estimate("Point tuple", (fra, fdec, fz))

  case class HealpixGrid(nside : Int, ptg : Pointing) {
    val hp = new HealpixBase(nside, RING)

    def index(ra : Double, dec : Double): Long = {
      // var p = new Pointing(ra, dec)
      ptg.theta = ra
      ptg.phi = dec
      hp.ang2pix(ptg)
    }
  }

  case class Points(n : Int, grid: HealpixGrid) {
    // Notice the par use to parallelize
    val data = (for(i <- 1 to n) yield Point(fra, fdec, fz)).par
      .groupBy(x => grid.index(x.ra, x.dec))

    // Equivalent
    // val data = (0 to n).par.map(x => Point(fra, fdec, fz))
    //   .groupBy(x => grid.index(x.ra, x.dec))
  }

  def main(args: Array[String]) {
    println("Start!")

    val nside : Int = 512

    val n = 10000

    // Just le timing pour 1 passage
    val t = T.timeit(
      s"groupby for $n initial points ",
      Points(n, HealpixGrid(nside, ptg)))

    // Memory profiling for several iterations
    val p = for (i <- 0 to 2) yield P.estimate(
      "Iteration " + i.toString + " (" + Points(n, HealpixGrid(nside, ptg)).data.toList.length.toString + s" unique cells / $n initial points) - size : ",
      Points(n, HealpixGrid(nside, ptg)))
    // P.estimate("grille", p)

    // Ici la classe Points produit déjà une collection ordonnée sur la grille
    // for ((k, v) <- p.data) println(s"nside=512 : pixnum=$k points=${v}")
  }
}
