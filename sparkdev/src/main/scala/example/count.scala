package example

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

object sortme extends App {
  // Create a Spark context
  val cores = 4
  val conf = new SparkConf()
  // .setMaster("local[*]").setAppName("sortme").
      // set("spark.cores.max", s"$cores").
      //set("spark.local.dir", "/mongo/log/tmp/").
      // set("spark.executor.memory", "16g")
      // set("spark.storageMemory.fraction", "0")

  val sc = new SparkContext(conf)

  // Open the file and create RDD
  val txtFile = "data/dico.txt"
  val txtData = sc.textFile(txtFile)

  // Store RDD in the cache
  txtData.cache()

  // Count the words and gather them
  val wcData = txtData.flatMap(l => l.split(" ")).map(word => (word, 1)).reduceByKey(_ + _)
  wcData.collect()//.foreach(println)
  println("done!")
}
