package com.example.todofirebase.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun dateToString(date:LocalDate?):String? {
        return date?.toString()
    }

    @TypeConverter
    fun stringToDate(date:String?):LocalDate? {
        return if(date == null) {
            null
        } else {
            LocalDate.parse(date)
        }
    }

    @TypeConverter
    fun timeToString(time: LocalTime?):String? {
        return time?.toString()
    }

    @TypeConverter
    fun stringToTime(time:String?):LocalTime? {
        return if(time == null) {
            null
        } else {
            LocalTime.parse(time)
        }
    }
}