package testpassword.services

import java.net.ConnectException
import java.sql.*
import kotlin.runCatching
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

typealias JDBC_Creds = Triple<String, String, String>

class DatabaseNotSupportedException: SQLException()

class IndexCreationError(badQuery: String): SQLException(badQuery)

class DBsSupport(val creds: JDBC_Creds) {

    companion object {
        private const val PING_TIMEOUT_SEC = 20
        val CONNECTION_URL_PATTERN = Regex("jdbc:.*://.*;.*;.*")
    }

    val creator: INSTANCES =
        try {
            INSTANCES.valueOf(creds.first.split(":")[1])
        } catch (e: IllegalArgumentException) {
            throw DatabaseNotSupportedException()
        }

    fun checkDbAvailability() {
        if (this.isDbSupported().not()) throw DatabaseNotSupportedException()
        if (this.ping().not()) throw ConnectException()
    }

    private fun getConnection(): Connection =
        DriverManager.getConnection(creds.first, creds.second, creds.third)

    fun isDbSupported(): Boolean =
        runCatching { creator; true }.getOrDefault(false)

    fun ping(): Boolean = getConnection().use { it.isValid(PING_TIMEOUT_SEC) }

    fun execute(queryFunc: () -> String): Boolean =
        getConnection().use {
            it.createStatement().use {
                it.execute(queryFunc())
            }
        }

    fun measureQuery(queryFunc: () -> String): Long =
        getConnection().use {
            it.createStatement().use {
                measureNanoTime {
                    it.execute(queryFunc())
                }
            }
        }

    fun getTableColumns(tableName: String): Set<String> =
        getConnection().use {
            it.createStatement().use {
                it.executeQuery("SELECT * FROM $tableName;")
                .metaData.let { md ->
                    generateSequence(1) { i -> i + 1 }
                        .take(md.columnCount)
                        .map { i -> md.getColumnName(i) }
                        .toSet()
                }
            }
        }
}