package gitlang

object Core extends App {
  import scala.concurrent.Await
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.io.StdIn.{ readLine, readInt }
  import org.apache.spark.{ SparkContext, SparkConf }
  import java.io.{ Console => _, _ }

  val verbose = true

  // Initialise a local Spark context
  val conf = new SparkConf().setMaster("local").setAppName("vor")
  val sc = new SparkContext(conf)

  // Reads in the "dist.json" distribution data file
  val distjson = new File("src/main/resources/dist.json")
  val sqlctx = DistDataSource.readJSON(sc, distjson.getAbsolutePath())
  val dists = DistDataSource.getDistributionByLanguage(sqlctx, verbose)

  // Filter only those languages with distributions
  val dists_ = dists.filter("SIZE(coords)>0")

  // Accumulate the entire geospatial universe of the distributions
  val universe = Analysis.accumGlobalDists(sqlctx, dists_)

  // Illustrate the universe distribution
  println(Console.CYAN + s"Total geolocation spots : ${universe.keys.size}" + Console.RESET)
  for (xy <- universe) {
    println(xy)
  }

  // TAOTODO: Illustrate the distribution of the universe 

  // Group geospatial distributions data of each language
  // into [bin] so we have fixed-length numerical vectors.
  val binVectors = Analysis.geoDistToBins(sqlctx, dists_, universe)

  // Classify the bin vectors into K different patterns
  val K = 8
  Analysis.learnPatterns(sc, K, binVectors)

  // Illustrate centroids of those K different patterns
  // as we classified

}