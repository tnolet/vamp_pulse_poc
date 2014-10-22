package actors

import akka.actor.{Actor, ActorLogging}
import models.DataPoint
import play.api.Logger

/**
 * The ScoreBoardActor functions as the score board for all incoming data
 */

trait ScoreBoardMessage
case object GetTotalScoreBoard extends  ScoreBoardMessage
case class TotalScoreBoard(board: Map[String,Long])
case class GetScore(metric: String)
case class Score(value: Long)

class ScoreBoardActor extends Actor with ActorLogging {


  var board =  scala.collection.mutable.Map[String,Long]()
  var counter : Long = 0
  var timer = System.currentTimeMillis()

  def receive = {

    case d : DataPoint =>

      counter+= 1

      //filter out all data from dummy and test services, only allow object starting with "vrn-"
      if (d.name.startsWith("vrn-") || d.name.contains("dummy")) {

        // add point to the board or update it if already there

        this.board(d.name) = d.value

        if (counter % 1000 == 0 ) {
          val interval =  System.currentTimeMillis() - timer
          Logger.info(s"Refreshed a 1000 metrics on the score board in $interval milliseconds ")
          counter = 0
          timer = System.currentTimeMillis()
        }

      }

    case GetTotalScoreBoard =>

      // cast the mutable map to immutable before sending
      val immutableBoard = collection.immutable.Map(board.toSeq: _*)

      sender ! TotalScoreBoard(board = immutableBoard)

    case s: GetScore =>

      // check if the requested metric is in the score board. It might not yet be
      // due to timing issues. If it is not there, we report -1
      if (board.contains(s.metric)) {

        sender ! Score(value = this.board(s.metric))

      } else {

        sender ! Score(value = -1)

      }



  }

}

