package me.wcy.datepicker

enum class DateType(val id: Int) {
    YMDHM(1), YMD(2), HM(3);

    companion object {
        fun fromId(id: Int): DateType {
            val values = values()
            return values.find { it.id == id } ?: YMDHM
        }
    }
}