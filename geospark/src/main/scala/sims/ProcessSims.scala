package sims

import java.io._
import scala.collection.JavaConverters._

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import org.apache.log4j.Level
import org.apache.log4j.Logger

import org.datasyslab.geospark.spatialRDD.PointRDD
import org.datasyslab.geospark.spatialOperator.RangeQuery
import org.datasyslab.geospark.spatialOperator.KNNQuery

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Envelope


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

object makeShell {
	/**
	  * Generate data up to some redshift `max_redshift`, and project it into
	  * n shells `nshells` in redshift. Data consist in points (RA, Dec, z).
	  *
	  * Usage: makeShell <ngalaxie> <nshell> <max_redshift> <selection> <outdir>
	  *
    * @constructor Create data and search for specific galaxies.
    * @param ngalaxies int : Number of galaxie to simulate.
    * @param nshells int : Number of shells.
    * @param max_redshift int : Maximum redshift for the data
    * @param selection string : method to select galaxie.
    *   `envelope` or `target_and_search`
    * @param outdir string : output folder. Should not exist.
		*
	  */
		val usage =
			"""
      Usage: makeShell <ngalaxies> <nshells> <max_redshift> <selection> <outdir>
      """

	def main(args: Array[String]) {
		/**
			* Main program
      *
      * @constructor Create data and search for specific galaxies.
      * @param ngalaxies int : Number of galaxie to simulate.
      * @param nshells int : Number of shells.
  	  * @param max_redshift int : Maximum redshift for the data
      * @param selection string : method to select galaxie.
      *   `envelope` or `target_and_search`
  	  * @param outdir string : output folder. Should not exist.
			*/

		// Check you call the script with the correct number of args
		if (args.length < 5) {
      System.err.println(usage)
      System.exit(1)
    }

		// Initialise Spark context
		val conf = new SparkConf()
    val sc = new SparkContext(conf)

		// Number of partitions for the data
		val parts = 4
    println("Using only 4 hardcoded partitions for the moment! Fix me...")

    // Logger ??
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    // Define input parameters
    val ngal = args(0).toInt
		val nshells = args(1).toInt
		val max_redshift = args(2).toInt
    val selection_method = args(3).toString

    // Create output directory where products
    // will be stored
    val outDir = new File(args(4).toString)
    outDir.mkdirs()

    // Generate the mock data set
		val data = new generateData(ngal, max_redshift)

		// RDDified the data set
		val points = data.buildPoints(ngal)
    var rdd = sc.parallelize(points, parts).persist

		// Save the result in a txt file
    // Bottleneck, super slow operation!
		rdd.coalesce(1, true).saveAsTextFile(outDir+"/input")

		// Have a look at 10 points
		var array = rdd.take(10)
		array.foreach(println)

		// Extent the standard PointRDD class
    val objectRDD = new ExtPointRDD(rdd)

    // Select points depending on the selection method
    // envelope : Define an envelope and count the point inside
    //  The envelope is defined as (ra_min, ra_max, dec_min, dec_max) (TBC)
    // target_and_search : target a point on the sky, and search for
    // X surrounding neighbours.
    if (selection_method == "envelope") {
      // Define your envelope. TODO: need to use curve sky coordinate!
      // I suspect that it only uses cartesian grid!
  		val queryEnvelope = new Envelope(-45.0, 45.0, -20.0, 20.0)

      // Action
  		val resultSize = RangeQuery.SpatialRangeQuery(
        objectRDD, queryEnvelope, false, false)
      println(resultSize.count() + " galaxies found in this range!")

      // Save the result in a txt file
      // Bottleneck: super slow operation!
  		resultSize.coalesce(1, true).saveAsTextFile(outDir+"/output")

    } else if (selection_method == "target_and_search") {
      // Initialise your geometry
      val geometryFactory = new GeometryFactory()

      // Number of galaxies to target + initialisation of coordinates
      val ntargets = 4
      val ra_target = scala.util.Random
      ra_target.setSeed(0)
      val dec_target = scala.util.Random
      dec_target.setSeed(54857)

      // Number of neighbours to find
      val n_neighbours = 500

      // Output file to match RDD-like style.
      val file = "output/part-00000"
      val writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(file)))
      for(i <- 1 to ntargets) {
        // Convert in degree
        val xcoord = ra_target.nextFloat * 180
        val ycoord = dec_target.nextFloat * 90

        // Action
        val kNNQueryPoint = geometryFactory.createPoint(
          new Coordinate(xcoord, ycoord))
        val resultSize = KNNQuery.SpatialKnnQuery(
          objectRDD, kNNQueryPoint, n_neighbours, false)

        // Write the result in a file
        // Super slow!
        for (x <- resultSize.asScala) {
          writer.write(x + "\n")
        }
      }
      writer.close()
    } else {
      System.err.println("Selection method not understood!\n")
      System.err.println("You need to use `envelope` or `target_and_search`\n")
      System.exit(1)
    }
	}
}

class generateData(var ngal: Int, var max_redshift: Int) {
	/**
	  * You can access using

		* {{{
		* val data = new generateData(10000, 1)
		* }}}
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
