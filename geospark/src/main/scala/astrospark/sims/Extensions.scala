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
    * @constructor Add routine getPoint to the class Point
    * @param point Point : coordinate of the point
    */
  def getPoint = this.point
}

class ExtPointRDD[T](r:RDD[T]) extends PointRDD(
    r.asInstanceOf[RDD[Point]]) {
  /**
    * Not really sure why and how... Fix me!
    *
    * @constructor Extend the PointRDD class by....
    * @param r RDD[T] : Element of a RDD?
    *
    */
  analyze()
}
