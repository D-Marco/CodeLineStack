package com.lane.dataBeans

data class LineWithItem(val line:Line, val item: Item?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LineWithItem

        return line == other.line
    }

    override fun hashCode(): Int {
        return line.hashCode()
    }
}