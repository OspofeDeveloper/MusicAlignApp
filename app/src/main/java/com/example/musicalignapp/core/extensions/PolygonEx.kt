package com.example.musicalignapp.core.extensions

fun List<Int>.getBoundingBox(): List<Int> {
    val listX = this.filterIndexed { index, _ -> index % 2 == 0 }
    val listY = this.filterIndexed { index, _ -> index % 2 == 1 }

    val minX = listX.minOf { it }
    val minY = listY.minOf { it }
    val maxX = listX.maxOf { it }
    val maxY = listY.maxOf { it }

    val width = maxX - minX
    val height = maxY - minY

    return listOf(minX, minY, width, height)
}

fun List<Int>.getBoundingBoxArea(): Int {
    val height = this[3]
    val width = this[2]
    val area = height * width

    return area
}