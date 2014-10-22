package controllers

import actors.{TotalScoreBoard, GetTotalScoreBoard}
import play.api.libs.concurrent.Akka
import akka.pattern.ask
import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created by tim on 22/10/14.
 */
object ScoreBoardController extends Controller {

  import play.api.Play.current
  val scoreBoard = Akka.system.actorSelection("akka://application/user/pulseParent/scoreBoard")


  def list = Action.async {

    implicit val timeout = akka.util.Timeout(5.seconds)

    //implicit val mapWrites = Json.writes[scala.collection.mutable.Map[String,Long]]

    val futureSb = scoreBoard ? GetTotalScoreBoard
    futureSb.map {

      case t: TotalScoreBoard =>

        //cast the mutable map to immutable

        Ok(Json.toJson(t.board))
    }

  }

}
