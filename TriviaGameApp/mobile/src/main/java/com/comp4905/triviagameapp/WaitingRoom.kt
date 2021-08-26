package com.comp4905.triviagameapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class WaitingRoom : AppCompatActivity() {
    private val questions = mutableListOf<QuestionItem>()
    private var gameId = ""
    private var playerId = ""
    private var mode = ""
    val db = Firebase.firestore
    val games = db.collection("games")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)
        //val db = Firebase.firestore
        val category = intent.getStringExtra("category")
        val difficulty = intent.getStringExtra("difficulty")
        mode = intent.getStringExtra("mode")
        playerId = intent.getStringExtra("playerId")
        //val games = db.collection("games")

        var docRef = games.whereEqualTo("category", category).whereEqualTo("difficulty", difficulty).whereEqualTo("started", false)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                        createWaitingRoom()
                } else {
                    gameId = documents.documents[0].id

                    val player = hashMapOf(
                        "playerId" to playerId,
                        "score" to 0
                    )

                    games.document(gameId).update("player2", player,"started", true)
                        .addOnFailureListener { e -> Log.w("Error updating document", e) }
                    val intent = Intent(this, Game::class.java)
                    intent.putExtra("playerId", playerId)
                    intent.putExtra("mode", mode)
                    intent.putExtra("gameId", gameId)
                    startActivity(intent)
                }
            }


    }

    private fun createWaitingRoom(){
        val category = intent.getStringExtra("category")
        val difficulty = intent.getStringExtra("difficulty")

        val url = "https://opentdb.com/api.php?amount=10&category=$category&difficulty=$difficulty"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val questionJson = response.getJSONArray("results")
                for (i in 0 until questionJson.length()){
                    var question = questionJson.getJSONObject(i).getString("question")
                    var answer = questionJson.getJSONObject(i).getString("correct_answer")
                    var options = ArrayList<ListItem>()
                    options.add(ListItem(questionJson.getJSONObject(i).getString("correct_answer"), ""))
                    val wrongAnswers = questionJson.getJSONObject(i).getJSONArray("incorrect_answers")
                    for (i in 0 until wrongAnswers.length()) {
                        val item = ListItem(wrongAnswers.get(i).toString(), "")
                        options.add(item)
                    }
                    options = options.shuffled() as ArrayList<ListItem>
                    questions.add(QuestionItem(question, answer, options))

                }
                val player = hashMapOf(
                    "playerId" to playerId,
                    "score" to 0
                )
                val roomData = hashMapOf(
                    "category" to category,
                    "started"  to false,
                    "player1"  to player,
                    "player2"  to null,
                    "difficulty" to difficulty,
                    "questions" to questions
                )

                val db = Firebase.firestore
                db.collection("games").add(roomData).addOnSuccessListener { documentReference ->
                    gameId = documentReference.id
                    println(gameId)
                    val gameRef = db.collection("games").document(gameId)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w("Listen failed.", e)
                                return@addSnapshotListener
                            }

                            if (snapshot != null && snapshot.exists()) {
                                if(snapshot.getBoolean("started") == true){
                                    val intent = Intent(this, Game::class.java)
                                    intent.putExtra("playerId", playerId)
                                    intent.putExtra("mode", mode)
                                    intent.putExtra("gameId", gameId)
                                    startActivity(intent)
                                }

                            } else {
                                Log.d("data", "Current data: null")
                            }
                        }
                }.addOnFailureListener { e -> Log.w("Error writing document", e) }
            },
            { error ->

            }
        )
        ApiRequests.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }
}
