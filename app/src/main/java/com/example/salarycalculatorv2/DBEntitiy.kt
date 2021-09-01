package com.example.salarycalculatorv2

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DBEntity{
    @PrimaryKey
    @ColumnInfo(name="ID")
    var id_column: Int=0
    @ColumnInfo(name="Date") var date_column: String=""
    @ColumnInfo(name="CheckIn") var checkin_time: String=""
    @ColumnInfo(name="CheckOut") var checkout_time: String=""
    @ColumnInfo(name="Bonus") var bonus: Boolean=false
    @ColumnInfo(name="BonusWage") var bonusWage: Boolean=false

}