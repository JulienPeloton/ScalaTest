/**
  * FILE : ProcessSims.scala
  * PATH : astrospark.sims
  * AUTHOR : Julien Peloton <peloton@lal.in2p3.fr>
  */
package astrospark.sims

import java.io._
import scala.collection.JavaConverters._

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

import org.apache.log4j.Level
import org.apache.log4j.Logger

import org.datasyslab.geospark.spatialOperator.RangeQuery
import org.datasyslab.geospark.spatialOperator.KNNQuery

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Envelope

object makeShell {
  /**
    * Generate data up to some redshift `max_redshift`, and project it into
    * n shells `nshells` in redshift. Data consist in points (RA, Dec, z).
    *
    * Usage: makeShell <ngalaxie> <nshell> <max_redshift> <selection> <outdir>
    *
    * @constructor : Create data and search for specific galaxies.
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
    Usage: makeShell <ngalaxies> <nshells> <max_redshift> <selection> <outdir> <nparts>
    """

	def main(args: Array[String]) {
    /**
      * Main program
      *
      * @constructor : Create data and search for specific galaxies.
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
    val parts = args(5).toInt

    // Logger ??
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    // For profiling
    val filename = "profiling_" + parts.toString + ".txt"
    val T = new Timing(filename)
    var msg : String = ""

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
    msg = "DataGen"
    val points = T.timeit(msg, data.buildPoints(ngal))

    msg = "RDD"
    var rdd = T.timeit(msg, sc.parallelize(points, parts).persist)

    // Save the result in a txt file
    // Bottleneck, super slow operation!
    // msg = "WriteText"
    // T.timeit(msg, rdd.coalesce(1, true).saveAsTextFile(outDir+"/input"))

    // Have a look at 10 points
    var array = rdd.take(10)
    array.foreach(println)
    println(rdd.count)

    // Filter redshifts
    msg = "FilterRedshifts"
    val rdd_filtered = T.timeit(
      msg, rdd.filter(x => x.getRedshift > 1 && x.getRedshift < 5))
    var array_filtered = rdd_filtered.take(10)
    array_filtered.foreach(println)
    msg = "Count"
    println(T.timeit(msg, rdd_filtered.count))

    // Extent the standard PointRDD class
    val objectRDD = new ExtPointRDD(rdd_filtered)

    // Select points depending on the selection method
    // envelope : Define an envelope and count the point inside
    //  The envelope is defined as (ra_min, ra_max, dec_min, dec_max) (TBC)
    // target_and_search : target a point on the sky, and search for
    // X surrounding neighbours.
    if (selection_method == "envelope") {
      // Define your envelope. TODO: need to use curve sky coordinate!
      // I suspect that it only uses cartesian grid!
      val queryEnvelope = new Envelope(-45.0, 45.0, -40.0, 40.0)

      // Action
      msg = "FilterRADec"
      val resultSize = T.timeit(msg, RangeQuery.SpatialRangeQuery(
        objectRDD, queryEnvelope, false, false))
      println(resultSize.count() + " galaxies found in this range!")

      // Save the result in a txt file
      // Bottleneck: super slow operation!
      // resultSize.coalesce(1, true).saveAsTextFile(outDir+"/output")

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
