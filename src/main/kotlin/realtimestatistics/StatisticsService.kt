package realtimestatistics

import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import java.util.*
import java.util.concurrent.TimeUnit

enum class StatisticResult {
    OK, OLD
}

class StatisticsService(private val influxDB: InfluxDB,
                        private val dbName: String = "statistics") {

    private val timeFrameInMillis = 60000

    private val aggregateQuery = """
        SELECT  count(s_count) as count,
                sum(s_count) as sum,
                min(s_count) as min,
                max(s_count) as max
        FROM uploads
        where time > now() - 60s
        """

    init {
        influxDB.createDatabase(dbName)
    }

    fun create(statistic: Statistic): StatisticResult {
        val now = Date().time
        if ((statistic.timestamp + timeFrameInMillis) >= now) {
            influxDB.write(dbName, "", Point.measurement("uploads")
                    .time(statistic.timestamp, TimeUnit.MILLISECONDS)
                    .addField("s_count", statistic.count)
                    .addField("s_timestamp", statistic.timestamp)
                    .build())
            return StatisticResult.OK
        }
        return StatisticResult.OLD
    }

    fun aggregated(): Total {
        val query = Query(
                aggregateQuery,
                dbName
        )
        val results = influxDB.query(query)
                .results
        if (results.first().series == null) {
            return Total(0.0, 0.0, 0.0, 0.0)
        }
        return results.first().series.first().values
                .map { mutableList ->
                    Total(mutableList[1].toString().toDouble(),
                            mutableList[2].toString().toDouble(),
                            mutableList[3].toString().toDouble(),
                            mutableList[4].toString().toDouble()
                    )
                }[0]
    }

}