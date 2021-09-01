package com.example.salarycalculatorv2

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private var list= mutableListOf<Model>()
    private lateinit var customListAdapter : CustomAdapter
    private var salarySum: Double=0.0
    private var days: Int=0
    private var sumIn: Double=0.0
    private var sumOut: Double=0.0
    private var bonus=0.0
    private var tax=0.0
    private var wage=11.11
    private var currency="€"
    private var wage2=11.77
    private var breakCheck=false
    private var breakTime: Double= 0.0
    private var showDays=true
    private var hours=0.0
    private var hoursTemp=0.0
    private lateinit var db:Database

    @RequiresApi(Build.VERSION_CODES.O)
    val formatterDate: DateTimeFormatter? = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    @RequiresApi(Build.VERSION_CODES.O)
    val formatterTime: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm")
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        db=Room.databaseBuilder(this@MainActivity, Database::class.java, "TimeDB").build()

        val buttonAdd=findViewById<Button>(R.id.button_add)
        val buttonSettings=findViewById<ImageButton>(R.id.button_settings)
        val listView=findViewById<ListView>(R.id.listView)
        val layoutDays=findViewById<LinearLayout>(R.id.LayoutDays)
        val textDaysHours=findViewById<TextView>(R.id.textdays)

        customListAdapter = CustomAdapter(this@MainActivity, R.layout.custom_list_item, list)
        listView.adapter = customListAdapter

        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if(sharedPreference.contains("Bonus")){
            bonus=sharedPreference.getFloat("Bonus", 0f).toDouble()/100
            tax=sharedPreference.getFloat("Tax", 0f).toDouble()/100
            wage=sharedPreference.getFloat("Wage", 11.11f).toDouble()
            wage2=sharedPreference.getFloat("Wage2", 11.77f).toDouble()
            currency= sharedPreference.getString("Currency", "€").toString()
            breakCheck=sharedPreference.getBoolean("breakCheck", false)
            breakTime=sharedPreference.getFloat("BreakTime", 0f).toDouble()
        }

        CoroutineScope(Dispatchers.Main).launch {
            db.DB_DAO().Read().forEach {
                list.add(
                    Model(
                        it.id_column,
                        it.date_column,
                        it.checkin_time,
                        it.checkout_time,
                        it.bonus,
                        it.bonusWage
                    )
                )
        }
            customListAdapter.notifyDataSetChanged()
            sum()

        layoutDays.setOnClickListener {
            if (textDaysHours.text == "Days worked"){
                textDaysHours.text = "Hours worked"
                days_amount.text="%.2f".format(hours)
                showDays=false
            }
            else
            {
                textDaysHours.text = "Days worked"
                days_amount.text=days.toString()
                showDays=true
            }
        }

        buttonAdd.setOnClickListener {
            val viewAdd = inflater.inflate(R.layout.popup_add, null)
            val popupWindowAdd = PopupWindow(
                viewAdd,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            popupWindowAdd.isFocusable = true
            popupWindowAdd.update()
            popupWindowAdd.showAtLocation(buttonAdd, Gravity.CENTER, 0, 1)
            popupWindowAdd.dimBehind()

            val inputDate=viewAdd.findViewById<EditText>(R.id.inputDate)
            val inputCheckIn=viewAdd.findViewById<EditText>(R.id.input_check_in)
            val inputCheckOut=viewAdd.findViewById<EditText>(R.id.input_check_out)
            val buttonConfirm=viewAdd.findViewById<Button>(R.id.buttonConfirm)
            val bonusCheckBox=viewAdd.findViewById<CheckBox>(R.id.BonusCheckBox)
            val bonusWageCheckBox=viewAdd.findViewById<CheckBox>(R.id.BonusWageCheckBox)
            val buttonDate=viewAdd.findViewById<Button>(R.id.buttonDate)
            val buttonCheckIn=viewAdd.findViewById<Button>(R.id.button_check_in)
            val buttonCheckOut=viewAdd.findViewById<Button>(R.id.button_check_out)

            buttonDate.setOnClickListener {
                val current: LocalDateTime= LocalDateTime.now()
                inputDate.setText(current.format(formatterDate).toString())
            }

            buttonCheckIn.setOnClickListener {
                val current: LocalDateTime= LocalDateTime.now()
                inputCheckIn.setText(current.format(formatterTime).toString())
            }

            buttonCheckOut.setOnClickListener {
                val current: LocalDateTime= LocalDateTime.now()
                inputCheckOut.setText(current.format(formatterTime).toString())
            }

            buttonConfirm.setOnClickListener {
                val id: Int = if (list.size<=0){
                    1
                } else{
                    (list[list.lastIndex].ID)+1
                }
                list.add(
                    Model(
                        id,
                        inputDate.text.toString(),
                        inputCheckIn.text.toString(),
                        inputCheckOut.text.toString(),
                        bonusCheckBox.isChecked,
                        bonusWageCheckBox.isChecked
                    )
                )

                val ent=DBEntity()
                ent.id_column=id
                ent.date_column=inputDate.text.toString()
                ent.checkin_time=inputCheckIn.text.toString()
                ent.checkout_time=inputCheckOut.text.toString()
                ent.bonus=bonusCheckBox.isChecked
                ent.bonusWage=bonusWageCheckBox.isChecked
                CoroutineScope(Dispatchers.IO).launch{
                    db.DB_DAO().Save(ent)
                }
                sum()
                popupWindowAdd.dismiss()
            }
        }

            listView.setOnItemClickListener { _, _, position, _ ->
                val viewEdit = inflater.inflate(R.layout.popup_edit, null)
                val popupWindowEdit = PopupWindow(
                    viewEdit,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                popupWindowEdit.isFocusable = true
                popupWindowEdit.update()
                popupWindowEdit.showAtLocation(buttonAdd, Gravity.CENTER, 0, 1)
                popupWindowEdit.dimBehind()

                val inputDate=viewEdit.findViewById<EditText>(R.id.inputDate)
                val inputCheckIn=viewEdit.findViewById<EditText>(R.id.input_check_in)
                val inputCheckOut=viewEdit.findViewById<EditText>(R.id.input_check_out)

                val buttonDate=viewEdit.findViewById<Button>(R.id.buttonDate)
                val buttonCheckIn=viewEdit.findViewById<Button>(R.id.button_check_in)
                val buttonCheckOut=viewEdit.findViewById<Button>(R.id.button_check_out)
                val buttonConfirm=viewEdit.findViewById<Button>(R.id.buttonConfirm)
                val bonusCheckBox=viewEdit.findViewById<CheckBox>(R.id.BonusCheckBox)
                val bonusWageCheckBox=viewEdit.findViewById<CheckBox>(R.id.BonusWageCheckBox)
                val buttonDelete=viewEdit.findViewById<Button>(R.id.buttonDelete)

                inputDate.setText(list[position].Date)
                inputCheckIn.setText(list[position].CheckIn)
                inputCheckOut.setText(list[position].CheckOut)
                if(list[position].BonusCheck){
                    bonusCheckBox.isChecked=true
                }
                if (list[position].WageCheck){
                    bonusWageCheckBox.isChecked=true
                }

                buttonDate.setOnClickListener {
                    val current: LocalDateTime= LocalDateTime.now()
                    inputDate.setText(current.format(formatterDate).toString())
                }

                buttonCheckIn.setOnClickListener {
                    val current: LocalDateTime= LocalDateTime.now()
                    inputCheckIn.setText(current.format(formatterTime).toString())
                }

                buttonCheckOut.setOnClickListener {
                    val current: LocalDateTime= LocalDateTime.now()
                    inputCheckOut.setText(current.format(formatterTime).toString())
                }

                buttonDelete.setOnClickListener {
                    val ent=DBEntity()
                    ent.id_column=list[position].ID
                    ent.date_column=list[position].Date
                    ent.checkin_time=list[position].CheckIn
                    ent.checkout_time=list[position].CheckOut
                    ent.bonus=list[position].BonusCheck
                    ent.bonusWage=list[position].WageCheck
                    CoroutineScope(Dispatchers.IO).launch {
                        db.DB_DAO().DeleteElement(ent)
                    }
                    list.removeAt(position)
                    sum()
                    popupWindowEdit.dismiss()
                }

                buttonConfirm.setOnClickListener {
                    list.add(
                        position, Model(
                            list[position].ID,
                            inputDate.text.toString(),
                            inputCheckIn.text.toString(),
                            inputCheckOut.text.toString(),
                            bonusCheckBox.isChecked,
                            bonusWageCheckBox.isChecked
                        )
                    )
                    list.removeAt(position + 1)

                    val ent=DBEntity()
                    ent.id_column=list[position].ID
                    ent.date_column=inputDate.text.toString()
                    ent.checkin_time=inputCheckIn.text.toString()
                    ent.checkout_time=inputCheckOut.text.toString()
                    ent.bonus=bonusCheckBox.isChecked
                    ent.bonusWage=bonusWageCheckBox.isChecked
                    CoroutineScope(Dispatchers.IO).launch{
                        db.DB_DAO().Update(ent)
                    }
                    sum()
                    popupWindowEdit.dismiss()
                }
            }

        buttonSettings.setOnClickListener {

            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewSettings = inflater.inflate(R.layout.popup_settings, null)
            val popupWindow = PopupWindow(
                viewSettings,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            popupWindow.isFocusable = true
            popupWindow.update()
            popupWindow.showAtLocation(buttonSettings, Gravity.CENTER, 0, 1)

            popupWindow.dimBehind()

            val buttonSave=viewSettings.findViewById<Button>(R.id.buttonSave)
            val inputBonus=viewSettings.findViewById<EditText>(R.id.inputBonus)
            val inputTax=viewSettings.findViewById<EditText>(R.id.inputTax)
            val buttonClear=viewSettings.findViewById<Button>(R.id.buttonClear)
            val inputWage=viewSettings.findViewById<EditText>(R.id.inputWage)
            val inputCurrency=viewSettings.findViewById<EditText>(R.id.inputCurrency)
            val inputWage2=viewSettings.findViewById<EditText>(R.id.inputWage2)
            val breakCheckBox=viewSettings.findViewById<CheckBox>(R.id.breakCheck)
            val inputBreak=viewSettings.findViewById<EditText>(R.id.inputBreak)
            val textBreak=viewSettings.findViewById<TextView>(R.id.textView6)

            inputBonus.setText((bonus * 100).toString())
            inputTax.setText((tax * 100).toString())
            inputWage.setText("%.2f".format(wage))
            inputCurrency.setText(currency)
            inputWage2.setText("%.2f".format(wage2))
            breakCheckBox.isChecked=breakCheck

            if (breakCheckBox.isChecked){
                inputBreak.isVisible=true
                inputBreak.setText((breakTime*60).toString())
                textBreak.isVisible=true
            }


            breakCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked){
                    inputBreak.isVisible=true
                    inputBreak.setText((breakTime*60).toString())
                    textBreak.isVisible=true
                    breakCheck=true
                }
                else{
                    inputBreak.isVisible=false
                    textBreak.isVisible=false
                    breakCheck=false
            }
            }

            buttonClear.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    db.DB_DAO().DeleteAll()
                }
                    list.clear()
                    sum()
                    hours=0.0
                    days=0
            }

            buttonSave.setOnClickListener {
                if (inputBonus.text.isEmpty()){inputBonus.setText("0")}
                if(inputTax.text.isEmpty()){ inputTax.setText("0")
                if (inputBreak.text.isEmpty()){inputBreak.setText("0")}}

                breakTime=inputBreak.text.toString().toDouble()/60

                val sharedPreference =  getSharedPreferences(
                    "PREFERENCE_NAME",
                    Context.MODE_PRIVATE
                )
                val editor = sharedPreference.edit()
                bonus=inputBonus.text.toString().toDouble()/100
                tax=inputTax.text.toString().toDouble()/100
                currency=inputCurrency.text.toString()
                wage=inputWage.text.toString().toDouble()
                wage2=inputWage2.text.toString().toDouble()
                editor.putFloat("Bonus", inputBonus.text.toString().toFloat())
                editor.putFloat("Tax", inputTax.text.toString().toFloat())
                editor.putFloat("Wage", inputWage.text.toString().toFloat())
                editor.putFloat("Wage2", inputWage2.text.toString().toFloat())
                editor.putString("Currency", inputCurrency.text.toString())
                editor.putBoolean("breakCheck", breakCheck)
                editor.putFloat("BreakTime", inputBreak.text.toString().toFloat()/60)
                editor.apply()
                sum()
                popupWindow.dismiss()
            }
        }
        }
    }

    private fun sum(){
        customListAdapter.notifyDataSetChanged()
        CoroutineScope(Dispatchers.Main).launch {
            db.DB_DAO().Read().forEach{
            if (it.checkout_time!=""){
                sumOut = it.checkout_time.substringBeforeLast(":").toDouble()+((it.checkout_time.substringAfterLast(
                    ":"
                ).toDouble())/60)
                sumIn = it.checkin_time.substringBeforeLast(":").toDouble() + ((it.checkin_time.substringAfterLast(
                    ":"
                ).toDouble()) / 60)
                hoursTemp+=(sumOut-sumIn)
                if (breakCheck){
                    if ((sumOut-sumIn)>6){
                        sumOut-=breakTime
                        hours-=breakTime
                    }
                }

                salarySum += when(it.bonus){
                    true -> ((sumOut - sumIn) * wage) * (bonus)
                    else -> if (it.bonusWage){ (sumOut - sumIn) * wage2} else{ (sumOut - sumIn) * wage}
                }
            }
        }
            hours=hoursTemp
            hoursTemp=0.0
            execSum(salarySum)
        }
    }

    private fun execSum(sum: Double) {
        salary_amount.text="%.2f".format((sum - (tax * sum)))+currency
        days=list.distinctBy { it.Date }.size
        days_amount.text=days.toString()
        salarySum=0.0
    }
}
