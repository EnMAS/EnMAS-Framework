import scala.collection.immutable._

// about 32.0 seconds!
object TestListSet extends App {
  val start = System.currentTimeMillis
  var set = new ListSet[Int]
  for(i <- 0 to 100000) {
    set += i
  }
  println((System.currentTimeMillis - start) / 1000d)
}

// about 0.25 seconds!
object TestHashSet extends App {
  val start = System.currentTimeMillis
  var set = new HashSet[Int]
  for(i <- 0 to 100000) {
    set += i
  }
  println((System.currentTimeMillis - start) / 1000d)
}
