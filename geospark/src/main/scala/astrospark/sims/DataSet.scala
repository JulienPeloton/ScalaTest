/**
  * FILE : DataSet.scala
  * PATH : astrospark.sims
  * AUTHOR : Julien Peloton <peloton@lal.in2p3.fr>
  */
package astrospark.sims

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory

class generateData(val max_redshift: Int) extends java.io.Serializable {
  /**
    *
    * @param max_redshift int : maximum redshift for the simulation.
    *
    */
  def buildPoints = {
    /**
      * Generator (iterator) to construct Points (ra, dec, z).
      * TODO: Fix the seed!
      *
      * @return nextPoint Point : com.vividsolutions.jts.geom.Point
      *
      */
    // Initialise random number generator
    val r = scala.util.Random
    // r.setSeed(5943759)

    // Ra / Dec boundaries
    val x0 = -180.0
    val x1 = 180.0
    val y0 = -90.0
    val y1 = 90.0

    val width = x1 - x0
    val height = y1 - y0

    // Define your coordinate
    def ra = x0 + width * r.nextFloat
    def dec = y0 + height * r.nextFloat
    def redshift = max_redshift * r.nextFloat

    val fact = new GeometryFactory()
    def nextPoint = new ExtPoint3D(
      fact.createPoint(new Coordinate(ra, dec)), redshift)

    // Return the point
    nextPoint

  }
}
