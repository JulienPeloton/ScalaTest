/**
  * FILE : Extensions.scala
  * PATH : astrospark.sims
  * AUTHOR : Julien Peloton <peloton@lal.in2p3.fr>
  */
package astrospark.sims

import org.apache.spark.rdd.RDD

import org.datasyslab.geospark.spatialRDD.PointRDD

import com.vividsolutions.jts.geom.Point

class ExtPoint(point: Point) extends Point(
    point.getCoordinateSequence, point.getFactory) {
  /**
    * Extend the class Point (jts) to add
    * the routine getPoint.
    *
    * @constructor Add routine to access point attribute.
    * @param point Point : (RA / Dec) coordinate of the point.
    */
  def getPoint = this.point
}

class ExtPoint3D(point: Point, redshift: Double) extends Point(
    point.getCoordinateSequence, point.getFactory) {
  /**
    * Extend the class Point (jts) to add
    * the routine getPoint.
    *
    * @constructor Add routines to access attributes
    * @param point Point : (RA / Dec) coordinate of the point
    * @param redshift Double : Redshift of the point
    *
    */
  // Include the redshift when printing out
  override def toString: String = super.toString + redshift

  // Access instance attributes
  def getPoint = this.point
  def getRedshift = this.redshift
}

class ExtPointRDD[T](r : RDD[T]) extends PointRDD(
    r.asInstanceOf[RDD[Point]]) {
  /**
    * Deprecated.
    * Better to use directly:
    * val objectRDD = new PointRDD(rdd.asInstanceOf[RDD[Point]])
    *
    * @constructor Transform a RDD of type T into a PointRDD of type T
    * @param r RDD[T] : RDD of type T
    *
    */
  analyze()
}
