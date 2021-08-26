package com.comp4905.triviagameapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ModeSelect : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode_select)
        // get reference to button
        val regularButton = findViewById<Button>(R.id.Regular)
        val drivingButton = findViewById<Button>(R.id.Driving)

        regularButton.setOnClickListener {
            createPlayer("regular")
        }
        drivingButton.setOnClickListener {
            createPlayer("driving")
        }
    }

    private fun createPlayer(mode: String){
        val db = Firebase.firestore
        val textView: EditText = findViewById<EditText>(R.id.usernameInput)
        if (textView.text.toString() != "") {
            db.collection("player").whereEqualTo("username", textView.text.toString())
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        var playerId = ""
                        val playerData = hashMapOf(
                            "username" to textView.text.toString(),
                            "score" to 0
                        )
                        db.collection("player").add(playerData)
                            .addOnSuccessListener { documentReference ->
                                playerId = documentReference.id
                                goToCatSelect(playerId, mode)
                            }

                    } else {
                        var playerId = documents.documents[0].id
                        goToCatSelect(playerId, mode)
                    }
                }
        } else {
            Toast.makeText(applicationContext,"Enter a username",Toast.LENGTH_SHORT).show()
        }

    }

    private fun goToCatSelect(playerId: String, mode: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("playerId", playerId)
        intent.putExtra("mode", mode)
        startActivity(intent)
    }

}