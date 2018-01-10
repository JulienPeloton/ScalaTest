package example

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

object countWords{
  val usage = """
    Usage: countWords <filename.txt> <interactive (true/false)>
  """
  def main(args: Array[String]) {

    if (args.length < 2) {
      System.err.println(usage)
      System.exit(1)
    }

    val nWord = countme(args(0), args(1).toBoolean)

  }
  def countme(filename: String, interactive: Boolean = false): Long = {
    /*
    Object to count the total number of word in a text file.
    Do not use this for performance benchamrk, as it computes several
    additional things... mainly for testing ;-)

    Parameters
    ----------
    filename : string
      Text file.

    Returns
    ----------
    nWord : Long
      The number of non-null word counted.

    */

    // Create a Spark context
    val cores = 4
    val conf = new SparkConf()
      .setMaster("local[*]").setAppName("sortme")
      .set("spark.cores.max", s"$cores")
      .set("spark.executor.memory", "16g")
      .set("spark.storageMemory.fraction", "0")
      .set("spark.driver.allowMultipleContexts", "true")

    val sc = new SparkContext(conf)

    // Open the file and create RDD
    val txtFile = filename
    val txtData = sc.textFile(txtFile)

    // Store RDD in the cache
    txtData.cache()

    // Count all words
    val nWordTot = txtData
      .flatMap(l => l.split(" "))
      .map(word => (word, 1))
      .countByKey()
      .values
      .sum

    // Exit if file is empty!
    if (nWordTot == 0) {
      val message = "File is empty!"
      if (interactive){
        println(message)
      }
      val nWord = 0
      return nWord
    }

    // Count all the non-null words
    val nWord = txtData
      .flatMap(l => l.split(" "))
      .filter(_.nonEmpty)
      .map(word => (word, 1))
      .countByKey()
      .values
      .sum

    // Count the word and gather them by keys
    val wcData = txtData
      .flatMap(l => l.split(" "))
      .map(word => (word, 1))
      .reduceByKey(_ + _)

    // The collect method at the end converts
    // the result from an RDD to an array.
    wcData.collect()

    // Define and count the null word
    val nullWord = wcData.lookup("")(0)

    val cond = nWordTot == nWord + nullWord
    if (!cond){
      println("Problem in counting words!")
      // println(s"Number of non-null word is : $nWord \n")
      // println(s"Total number of word is : $nWordTot \n")
      // println(s"Total number of null word is : $nullWord \n")
      System.exit(1)
    }

    val message = s"Number of non-null word is : $nWord \n"

    if (interactive){
      println(message)
    }

    return nWord
  }

  // def sum_word(rdd: RDD[String], filternull: Int): Long = {
  //   /*
  //   Routine to count the total number of word in a string RDD.
  //
  //   ** NOT WORKING **
  //
  //   Parameters
  //   ----------
  //   RDD : RDD
  //     The RDD containing strings.
  //   filternull : int
  //     If 1, do not include null word of the form "".
  //     If 0, do not include null word of the form "".
  //
  //   Returns
  //   ----------
  //   num : Long
  //     The number of words in your RDD.
  //
  //   */
  //   if (filternull > 0){
  //     val reducedrdd = rdd.flatMap(l => l.split(" ")).filter(_.nonEmpty)
  //     println("Here 1")
  //   } else {
  //     val reducedrdd = rdd.flatMap(l => l.split(" "))
  //     println("Here 2")
  //   }
  //
  //   val num = reducedrdd.map(word => (word, 1))
  //     .countByKey()
  //     .values
  //     .sum
  //
  //   return num
  // }
}
