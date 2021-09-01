package com.example.salarycalculatorv2

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class CustomAdapter (var mCtx: Context, var resources:Int, var items:List<Model>): ArrayAdapter<Model>(mCtx, resources, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val lf: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View =lf.inflate(resources, null)

        val date: TextView =view.findViewById(R.id.date)
        val checkIn: TextView =view.findViewById(R.id.check_in)
        val checkOut: TextView =view.findViewById(R.id.check_out)
        val bonusText: TextView = view.findViewById<TextView>(R.id.bonustext)



        var mItem:Model=items[position]
        date.text=mItem.Date
        checkIn.text=mItem.CheckIn
        checkOut.text=mItem.CheckOut
        if(mItem.BonusCheck){
            bonusText.text="+"
        }
        if (mItem.WageCheck){
          date.setTextColor(Color.parseColor("yellow"))
        }

        return view
    }
}