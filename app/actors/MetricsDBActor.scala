package actors

import akka.actor.{ActorLogging, Actor}
import models.{KairosDB, DataPoint}
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

class MetricsDBActor extends Actor with ActorLogging {

  var counter : Long = 0
  var timer = System.currentTimeMillis()

  def receive = {

    case d: DataPoint =>

      counter+= 1

      // Add tags to the datapoint object for Kairos
      val names = d.name.split("\\.")

      val source = "loadbalancer"
      val proxy = names(0)
      val proxyType = names(1)
      val metric = names(2)

      // KairosDB wants epoch timestamps in milliseconds
      val timestamp = d.timestamp * 1000

      val tags = Map("source" -> source, "proxy" -> proxy, "proxyType" -> proxyType, "metric" -> metric)
      val dataPointWithTags = d.copy(tags = Some(tags), timestamp = timestamp)

      KairosDB.setDataPoint(dataPointWithTags).map {
        case true =>
          log.debug("Succesfully committed metric to Kairos")

          if (counter % 1000 == 0 ) {
            val interval = System.currentTimeMillis() - timer
            Logger.info(s"Submitted 1000 metrics to KairosDB in $timer milliseconds ")
            counter = 0
            timer = System.currentTimeMillis()
          }


        case false =>
          log.error("Error writing metric to Kairos")
      }
  }
}



