package com.ricardotejo.openpose

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputBinding
import android.widget.Button
import android.widget.ImageButton

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val squat_start = findViewById<ImageButton>(R.id.btn_squat_start)
        squat_start.setOnClickListener { view : View ->
            val intent = Intent(this, MocapActivity::class.java)
//            startActivity(nextIntent)
        }



    }
}