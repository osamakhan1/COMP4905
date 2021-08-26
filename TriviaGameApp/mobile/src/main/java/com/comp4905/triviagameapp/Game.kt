package com.comp4905.triviagameapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.comp4905.triviagameapp.databinding.ActivityGameBinding
import com.comp4905.triviagameapp.databinding.ActivityMainBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class Game : AppCompatActivity(), CategoryAdapter.OnItemClickListener, TextToSpeech.OnInitListener {
    private var textInit = false
    private lateinit var gameData : GameData
    private val SPEECH_REQUEST_CODE = 0
    private var playerId = ""
    private var mode = ""
    private var gameId = ""
    val db = Firebase.firestore
    private val textToSpeech: TextToSpeech by lazy {
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                println(status)
                if (status == TextToSpeech.SUCCESS) {
                    textInit = true
                    textToSpeech.language = Locale.UK
                }
            })
    }

    private var score = 0
    private var currentTime = 0
    private lateinit var binding : ActivityGameBinding
    private lateinit var timer : CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mode = intent.getStringExtra("mode")
        playerId = intent.getStringExtra("playerId")
        gameId = intent.getStringExtra("gameId")
        generateQuestion()
    }

    private fun readQuestion(){
        textToSpeech!!.speak(gameData.questions[0].question, TextToSpeech.QUEUE_ADD, null, "")
        for (i in 0 until gameData.questions[0].options.size) {
            //textToSpeech!!.playSilentUtterance(2000, TextToSpeech.QUEUE_ADD, null);
            textToSpeech!!.speak(gameData.questions[0].options[i].text, TextToSpeech.QUEUE_ADD, null, "")
        }
    }

    private fun generateQuestion() {
        val games = db.collection("games").document(gameId)
        games.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    gameData = document.toObject(GameData::class.java)!!
                    displayQuestion()
                }
            }
    }

    private fun updateScore() {
        if (gameData.player1?.playerId.equals(playerId)) {
            db.collection("games").document(gameId).update("player1.score",score)
        } else {
            db.collection("games").document(gameId).update("player2.score",score)
        }
    }

    private fun displayQuestion() {
        if (gameData.questions.size > 0) {
            timer = object : CountDownTimer(10000, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    currentTime = (millisUntilFinished / 1000).toInt()
                    binding.timer.text = "Time left: " + currentTime
                }

                override fun onFinish() {
                    binding.timer.text ="done!"
                    if(gameData.questions.size > 0) {
                        gameData.questions.removeAt(0)
                        displayQuestion()
                    }
                }
            }.start()
            binding.gameScore.text = "Score: " + score.toString()
            binding.textView.text = gameData.questions[0].question
            binding.recyclerView.adapter = CategoryAdapter(gameData.questions[0].options, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.setHasFixedSize(true)
            readQuestion()
            if(mode == "driving") {
                displaySpeechRecognizer()
            }
        } else {
            db.collection("player").document(playerId).update("score",
                FieldValue.increment(score.toLong()))
            if (gameData.player1?.playerId.equals(playerId)) {
                val games = db.collection("games").document(gameId)
                games.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            if(document.getLong("player1.score")?.toInt()!! > document.getLong("player2.score")?.toInt()!!){
                                val intent = Intent(this, ResultPage::class.java)
                                intent.putExtra("playerId", playerId)
                                intent.putExtra("mode", mode)
                                intent.putExtra("result", "You Won")
                                startActivity(intent)
                            } else {
                                val intent = Intent(this, ResultPage::class.java)
                                db.collection("games").document(gameId).delete()
                                intent.putExtra("playerId", playerId)
                                intent.putExtra("mode", mode)
                                intent.putExtra("result", "You Lost")
                                startActivity(intent)
                            }
                        }
                    }
            } else {
                val games = db.collection("games").document(gameId)
                games.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            if(document.getLong("player2.score")?.toInt()!! > document.getLong("player1.score")?.toInt()!!){
                                val intent = Intent(this, ResultPage::class.java)
                                intent.putExtra("playerId", playerId)
                                intent.putExtra("mode", mode)
                                intent.putExtra("result", "You Won")
                                startActivity(intent)
                            } else {
                                db.collection("games").document(gameId).delete()
                                val intent = Intent(this, ResultPage::class.java)
                                intent.putExtra("playerId", playerId)
                                intent.putExtra("mode", mode)
                                intent.putExtra("result", "You Lost")
                                startActivity(intent)
                            }
                        }
                    }
            }
        }

    }

    override fun onItemClick(position: Int) {

        if(gameData.questions.size > 0) {
            textToSpeech!!.stop()
            println(gameData.questions[0].options[position].text)
            println(gameData.questions[0].answer)
            if (gameData.questions[0].options[position].text == gameData.questions[0].answer){
                score += currentTime
                updateScore()
                timer.cancel()
                textToSpeech!!.speak("correct", TextToSpeech.QUEUE_ADD, null, "")
            } else {
                timer.cancel()
                textToSpeech!!.speak("incorrect", TextToSpeech.QUEUE_ADD, null, "")
            }
            gameData.questions.removeAt(0)
            displayQuestion()
        }

    }

    private fun displaySpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 600000)
        }
        // This starts the activity and populates the intent with the speech text.
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0)
                }
            println(spokenText)
            if (spokenText != null) {
                if(spokenText.contains("answer is")){
                    var subString = spokenText.substringAfter("answer is")
                    if(subString.contains("1")||subString.contains("one")) {
                        if(gameData.questions[0].options[0].text.equals(gameData.questions[0].answer)){
                            textToSpeech!!.speak("correct", TextToSpeech.QUEUE_ADD, null, "")
                            generateQuestion()
                        }
                    } else if(subString.contains("2")||subString.contains("two")) {
                        if(gameData.questions[0].options[1].text.equals(gameData.questions[0].answer)){
                            textToSpeech!!.speak("correct", TextToSpeech.QUEUE_ADD, null, "")
                            generateQuestion()
                        }
                    } else if(subString.contains("3")||subString.contains("three")) {
                        if(gameData.questions[0].options[2].text.equals(gameData.questions[0].answer)){
                            textToSpeech!!.speak("correct", TextToSpeech.QUEUE_ADD, null, "")
                            generateQuestion()
                        }
                    } else if(subString.contains("4")||subString.contains("four")) {
                        if(gameData.questions[0].options[3].text.equals(gameData.questions[0].answer)){
                            textToSpeech!!.speak("correct", TextToSpeech.QUEUE_ADD, null, "")
                            generateQuestion()
                        }
                    } else {
                        textToSpeech!!.speak("incorrect", TextToSpeech.QUEUE_ADD, null, "")
                        generateQuestion()
                    }
                } else if(spokenText.contains("read it")){
                    readQuestion()
                    displaySpeechRecognizer()
                }
                else displaySpeechRecognizer()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS){
            val result = textToSpeech!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "Lang not supported")
            }
        } else {
            Log.e("TTS", "Init failed")
        }
    }

    override fun onPause() {
        if(textToSpeech != null) {
            textToSpeech!!.stop()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if(textToSpeech != null) {
            //textToSpeech!!.shutdown()
        }
        super.onDestroy()
    }
}