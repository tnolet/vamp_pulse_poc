package actors

import akka.actor.{Actor, ActorLogging}
import models.DataPoint
import play.api.Logger

/**
 * The ScoreBoardActor functions as the score board for all incoming data
 */

trait ScoreBoardMessage
case object GetTotalScoreBoard extends  ScoreBoardMessage
case class TotalScoreBoard(board: Map[String,Map[String,Map[String,Long]]])
case class GetScore(metric: String)
case class Score(value: Long)

class ScoreBoardActor extends Actor with ActorLogging {


  var board =  scala.collection.mutable.Map[String,Map[String,Map[String,Long]]]()
  var counter : Long = 0
  var timer = System.currentTimeMillis()

  def receive = {

    case d : DataPoint =>

      counter+= 1

      //filter out all data from dummy and test services, only allow object starting with "vrn-"
//      if (d.name.startsWith("vrn-") || d.name.contains("dummy")) {

        // split the full name on periods
        val splits = d.name.split('_')
//
//
//        if (this.board.contains(splits(0))) {
//
//          val level1Map = this.board(splits(0))
//
//          if (level1Map.contains(splits(1))) {
//
//            val level2Map = level1Map(splits(1))
//
//            val newlevel2Map : Map[String,Long] = level2Map.+(splits(2) -> d.value)
//
//            val newlevel1Map = level1Map.+(splits(1)->newlevel2Map)
//
//            this.board(splits(0)) = newlevel1Map
//
//          } else {
//
//            val newlevel1Map : Map[String,Map[String,Long]] = Map(splits(1) -> Map[String,Long](splits(2) -> d.value))
//
//            this.board(splits(0)) = level1Map.++(newlevel1Map)
//
//          }
//
//        } else {
//
//          this.board(splits(0)) = Map(splits(1) -> Map[String,Long](splits(2) -> d.value))
//
//
//        }


        if (counter % 1000 == 0 ) {
          val interval =  System.currentTimeMillis() - timer
          Logger.info(s"Refreshed a 1000 metrics on the score board in $interval milliseconds ")
          counter = 0
          timer = System.currentTimeMillis()
        }

//      }

    case GetTotalScoreBoard =>

      // cast the mutable map to immutable before sending
      val immutableBoard = collection.immutable.Map(this.board.toSeq: _*)

      sender ! TotalScoreBoard(board = immutableBoard)

  }

  def createMetricsMap(metricType : Array[String], value : Any, map: Map[String,Any], level : Int = 0) : Map[String, Any]  = {

    var newMap = Map[String, Any]()


//    if (level <= metricType.length && level != 0) {
//      val key1 = metricType(level - 1)
//      val key2 = metricType(level)
//
//      println(s"key 1: $key1. key 2: $key2")
//
//      newMap = map.++(Map(key1 -> Map[String, Any](key2 -> value)))
//
//      println(s"Map is now:  $newMap")
//      createMetricsMap(metricType, value, newMap, level + 1)
//
//    }

    if (level == metricType.length) {

      newMap = Map(metricType(level) -> value)


    } else {

      newMap = map.++(Map(metricType(level) -> createMetricsMap(metricType,metricType(level),map,level + 1)))
      println(s"Map is now:  $newMap")

    }

//    if (level == 0) {
//
//      val key = metricType(level)
//      newMap = Map(key -> value)
//
//      println(s"Map is now:  $newMap")
//
//      createMetricsMap(metricType, value, newMap, level + 1)
//
//    }
//
//    if (level <= metricType.length) {
//
//      newMap = map.++(Map[String,Any](metricType(level) -> value))
//
//    }

    newMap
  }

}

