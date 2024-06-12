package capjoy.command.list.utils

import kotlin.math.max

fun showTable(
    headers: List<String>,
    rows: List<List<String>>,
) {
    val colWidths = headers.mapIndexed { index, header ->
        max(header.length, rows.maxOfOrNull { it[index].length } ?: 0)
    }

    fun padString(
        value: String,
        length: Int,
    ): String {
        return value.padEnd(length, ' ')
    }

    val formattedHeaders = headers.mapIndexed { index, header ->
        padString(header, colWidths[index])
    }.joinToString(" | ")

    println(formattedHeaders)
    println(colWidths.joinToString("-|-") { "-".repeat(it) })

    rows.forEach { row ->
        val formattedRow = row.mapIndexed { index, column ->
            padString(column, colWidths[index])
        }.joinToString(" | ")
        println(formattedRow)
    }
}

fun filterTableCols(
    filterHeaders: List<String>,
    headers: List<String>,
    rows: List<List<String>>,
): Pair<List<String>, List<List<String>>> {
    val indices = filterHeaders.map { headers.indexOf(it) }.filter { it != -1 }
    val filteredHeaders = indices.map { headers[it] }
    val filteredRows = rows.map { row -> indices.map { row[it] } }
    return Pair(filteredHeaders, filteredRows)
}
