import scala.collection.immutable._

// about 32.0 seconds!
object TestListSet extends App {
  var set = new ListSet[Int]
  for(i <- 0 to 100000) {
    set += i
  }
}

// about 0.25 seconds!
object TestHashSet extends App {
  var set = new HashSet[Int]
  for(i <- 0 to 100000) {
    set += i
  }
}
