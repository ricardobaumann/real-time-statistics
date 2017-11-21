package realtimestatistics

import org.assertj.core.api.Assertions.assertThat
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.junit.After
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

class StatisticsServiceTest {

    private val influxDb: InfluxDB by lazy { InfluxDBFactory.connect("http://localhost:8086", "root", "root") }

    private val dbName = "testdb"

    private val service = StatisticsService(influxDb, dbName)

    @Test
    fun shouldCreateItemSuccessfully() {
        //Given
        val count = 200
        val timestamp = Date().time
        //When
        val result = service.create(Statistic(count, timestamp))
        //Then
        assertThat(result).isEqualTo(StatisticResult.OK)
        assertDbContent(count, timestamp)
    }

    @Test
    fun shouldSkipOldItem() {
        //Given
        val count = 200
        val timestamp = Date().time - 60001
        //When
        val result = service.create(Statistic(count, timestamp))
        //Then
        assertThat(result).isEqualTo(StatisticResult.OLD)
        assertDBIsEmpty()
    }

    @Test
    fun shouldAggregateContent() {
        //Given
        val count = 200
        val timestamp = Date().time
        influxDb.write(dbName, "", Point.measurement("uploads")
                .time(timestamp, TimeUnit.MILLISECONDS)
                .addField("s_count", count)
                .addField("s_timestamp", timestamp)
                .build())

        influxDb.write(dbName, "", Point.measurement("uploads")
                .time(timestamp + 10, TimeUnit.MILLISECONDS)
                .addField("s_count", count - 2)
                .addField("s_timestamp", timestamp + 10)
                .build())

        //When //Then
        assertThat(service.aggregated()).isEqualTo(Total(2.0, 398.0, 198.0, 200.0))
    }

    private fun assertDBIsEmpty() {
        val results = influxDb.query(Query("select s_count, s_timestamp from uploads", dbName))
                .results
        assertThat(results.isEmpty())
    }

    private fun assertDbContent(count: Int, timestamp: Long) {
        val values = influxDb.query(Query("select s_count, s_timestamp from uploads", dbName))
                .results.first()
                .series.first()
                .values
        assertThat(values[0][1].toString().toDouble().toInt()).isEqualTo(count)
        assertThat(values[0][2].toString().toDouble().toLong()).isEqualTo(timestamp)

    }

    @After
    fun after() {
        influxDb.deleteDatabase(dbName)
    }
}