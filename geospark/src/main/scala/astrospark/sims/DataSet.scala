/**
  * FILE : DataSet.scala
  * PATH : astrospark.sims
  * AUTHOR : Julien Peloton <peloton@lal.in2p3.fr>
  */
package astrospark.sims

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory

class generateData(var ngal: Int, var max_redshift: Int) {
  /**
    *
    * @param ngal int : number of galaxies to draw.
    * @param max_redshift int : maximum redshift for the simulation.
    *
    */
  def buildPoints(n:Int) = {
    /**
      * Iterator to construct Points (x, y).
      *
      * @param n int : number of points to simulate.
      *
      * @return nextPoint Point : com.vividsolutions.jts.geom.Point
      *
      */
    // Initialise random number generator
    val r = scala.util.Random
    r.setSeed(5943759)

    // Ra / Dec boundaries
    val x0 = -180.0
    val x1 = 180.0
    val y0 = -90.0
    val y1 = 90.0

    val width = x1 - x0
    val height = y1 - y0

    def fx = x0 + width*r.nextFloat
    def fy = y0 + height*r.nextFloat
    def fv = 12 * r.nextFloat

    val fact = new GeometryFactory()
    // def nextPoint = new ExtPoint(fact.createPoint(new Coordinate(fx, fy)), fv)
    def nextPoint = new ExtPoint(fact.createPoint(new Coordinate(fx, fy)))

    for (i <- 1 to n) yield (nextPoint)
  }
}
