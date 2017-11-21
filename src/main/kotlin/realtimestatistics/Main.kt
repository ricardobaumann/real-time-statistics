package realtimestatistics

import io.javalin.ApiBuilder.get
import io.javalin.ApiBuilder.post
import io.javalin.Context
import io.javalin.Javalin
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory

data class Statistic(val count: Int = 0, val timestamp: Long = 0)

data class Total(val count: Double, val sum: Double, val min: Double, val max: Double)

val influxDB: InfluxDB by lazy { InfluxDBFactory.connect("http://localhost:8086", "root", "root") }

fun main(args: Array<String>) {
    val app = Javalin.start(7000)
    val statisticService = StatisticsService(influxDB)
    val controller = Controller(statisticService)

    app.routes {
        get("/statistics", { ctx ->
            controller.get(ctx)
        })
        post("/upload", { ctx ->
            controller.post(ctx)
        })
    }

}

class Controller(private val statisticService: StatisticsService) {
    private val asStatusCode = fun StatisticResult.(): Int {
        return if (this == StatisticResult.OK) {
            201
        } else {
            204
        }
    }

    fun post(ctx: Context) {
        val statistic = ctx.bodyAsClass(Statistic::class.java)
        val result = statisticService.create(statistic)
        ctx.status(result.asStatusCode())
    }

    fun get(ctx: Context) {
        ctx.json(statisticService.aggregated())
    }
}