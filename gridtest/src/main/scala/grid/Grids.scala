package grid

import healpix.essentials.HealpixBase
import healpix.essentials.Pointing

// Make the class Pointing Serializable
class ExtPointing extends Pointing with java.io.Serializable

/** Abstract class to manage grids.
    Put here common methods */
abstract class Grid {
  def index(ra : Double, dec : Double) : Long
}

/** Healpix Grid
    Define a Healpix pixelisation scheme given a nside
    @param nside resolution of the grid
    @param hp Instance of HealpixBase
    @param ptg Instance of Pointing */
case class HealpixGrid(
    nside : Int, hp : HealpixBase, ptg : ExtPointing) extends Grid {

  /** Map (RA/Dec) to Healpix pixel index
      @param ra ra coordinate [Double]
      @param dec dec coordinate [Double] */
  override def index(ra : Double, dec : Double) : Long = {
    ptg.theta = ra
    ptg.phi = dec
    hp.ang2pix(ptg)
  }
}

// case class GridIndex(indexX: Int, indexY: Int){
//   override def toString: String = {s"[$indexX:$indexY]"}
// }
//
// /** Cartesian Grid
//     Define a cartesian pixelisation scheme
//     @param rangeX Number of pixels in the X direction
//     @param rangeY Number of pixels in the Y direction */
// case class CartGrid(npix_per_row : Int) extends Grid {
//
//   /** Map (RA/Dec) to cartesian pixel index
//       @param ra ra coordinate [Double]
//       @param dec dec coordinate [Double] */
//   // def index(ra : Double, dec : Double) : Option[GridIndex] = {
//   override def index(ra : Double, dec : Double) : Long = {
//     def width = Math.PI
//     def height = 2.0 * Math.PI
//
//     val dx = npix_per_row / width
//     val dy = npix_per_row / height
//
//     ((ra * dx).toInt * npix_per_row + (dec * dy).toInt).toLong
//   }
// }
