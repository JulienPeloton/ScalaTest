import org.apache.spark.util.SizeEstimator

import healpix.essentials.HealpixBase
import healpix.essentials.Pointing
import healpix.essentials.Scheme.RING

// Small wrapper around the spark memory profiler
class Prof {
  def estimate[R](text: String, block: => AnyRef): Unit = {
    val size = SizeEstimator.estimate(block) / 1024.0
    println(text + " " + size.toString + " KB")
  }
}

import java.io.{File, FileOutputStream}

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

  case class Points(n: Int, grid: HealpixGrid) {

    val data = (for(i <- 1 to n) yield Point(fra, fdec, fz))
      .groupBy(x => grid.index(x.ra, x.dec))
  }

  def main(args: Array[String]) {
    println("TMap")
    val nside : Int = 512

    val n = 400000

    // Just le timing pour 1 passage
    val t = T.timeit(
      s"groupby for $n initial points ",
      Points(n, HealpixGrid(nside, ptg)))

    // Memory profiling for several iterations
    val p = for (i <- 0 to 10) yield P.estimate(
      "Iteration " + i.toString + " (" + Points(n, HealpixGrid(nside, ptg)).data.toList.length.toString + s" unique cells / $n initial points) - size : ",
      Points(n, HealpixGrid(nside, ptg)))
    // P.estimate("grille", p)

    // Ici la classe Points produit déjà une collection ordonnée sur la grille
    // for ((k, v) <- p.data) println(s"nside=512 : pixnum=$k points=${v}")
  }
}
