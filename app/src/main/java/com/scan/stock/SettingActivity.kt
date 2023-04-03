package com.scan.stock

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText

class SettingActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val ip = findViewById<EditText>(R.id.etIP)
        val btn = findViewById<Button>(R.id.btnSave)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val sharedPref = getSharedPreferences("scanstock_pref", Context.MODE_PRIVATE)
        ip.text = sharedPref.getString("ip", "http://192.168.56.1/scanbarcode/")!!.toEditable()

        btn.setOnClickListener {
            with(sharedPref.edit()) {
                putString("ip", ip.text.toString())
                apply()
            }
            finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
}