package realtimestatistics

import io.javalin.ApiBuilder.get
import io.javalin.ApiBuilder.post
import io.javalin.Javalin
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory

data class Statistic(val count: Int = 0, val timestamp: Long = 0)

data class Total(val count: Double, val sum: Double, val min: Double, val max: Double)

val influxDB: InfluxDB by lazy { InfluxDBFactory.connect("http://localhost:8086", "root", "root") }

fun main(args: Array<String>) {
    val app = Javalin.start(7000)
    val statisticService = StatisticsService(influxDB)

    val asStatusCode = fun StatisticResult.(): Int {
        return if (this == StatisticResult.OK) {
            201
        } else {
            204
        }
    }

    app.routes {
        get("/statistics", { ctx ->
            ctx.json(statisticService.aggregated())
        })
        post("/upload", { ctx ->
            val statistic = ctx.bodyAsClass(Statistic::class.java)
            val result = statisticService.create(statistic)
            ctx.status(result.asStatusCode())
        })
    }

}