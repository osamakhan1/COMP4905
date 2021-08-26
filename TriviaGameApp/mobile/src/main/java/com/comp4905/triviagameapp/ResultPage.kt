package com.comp4905.triviagameapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ResultPage : AppCompatActivity() {
    private var playerId = ""
    private var mode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_page)
        val result = findViewById<TextView>(R.id.result)
        result.setText(intent.getStringExtra("result"))
        mode = intent.getStringExtra("mode")
        playerId = intent.getStringExtra("playerId")
        println(playerId)
        val button = findViewById<Button>(R.id.backCatSelect)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("playerId", playerId)
            intent.putExtra("mode", mode)
            startActivity(intent)
        }
    }
}