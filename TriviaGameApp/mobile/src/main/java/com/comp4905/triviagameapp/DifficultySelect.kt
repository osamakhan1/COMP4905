package com.comp4905.triviagameapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.comp4905.triviagameapp.databinding.ActivityDifficultySelectBinding
import com.comp4905.triviagameapp.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

class DifficultySelect : AppCompatActivity(), CategoryAdapter.OnItemClickListener, TextToSpeech.OnInitListener {
    private val difficulties = ArrayList<ListItem>()
    private var category = ""
    private val SPEECH_REQUEST_CODE = 0
    private var textInit = false
    private var playerId = ""
    private var mode = ""
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

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name")
        category = intent.getStringExtra("id").toString()
        mode = intent.getStringExtra("mode")
        playerId = intent.getStringExtra("playerId")
        val actionBar = supportActionBar
        actionBar!!.title = name
        actionBar.setDefaultDisplayHomeAsUpEnabled(true)
        generateDifficulties()
        if(mode == "driving") {
            displaySpeechRecognizer()
        }
    }

    private fun generateDifficulties() {
        val url = "https://opentdb.com/api_category.php"
        difficulties.add(ListItem("Easy", ""))
        difficulties.add(ListItem("Medium",""))
        difficulties.add(ListItem("Hard",""))
        val binding = ActivityDifficultySelectBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.recyclerView.adapter = CategoryAdapter(difficulties, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
    }

    override fun onItemClick(position: Int) {
        val clickedItem = difficulties[position]
        val intent = Intent(this, WaitingRoom::class.java)
        //intent.putExtra("url","https://opentdb.com/api.php?amount=10&category=" + category + "&difficulty=" + clickedItem.text.toLowerCase())
        intent.putExtra("category", category)
        intent.putExtra("difficulty", clickedItem.text?.toLowerCase())
        intent.putExtra("playerId", playerId)
        intent.putExtra("mode", mode)
        startActivity(intent)
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

            if (spokenText != null) {
                if(spokenText.contains("read it")){
                    for (i in 0 until difficulties.size) {
                        textToSpeech!!.speak(difficulties.get(i).text, TextToSpeech.QUEUE_ADD, null, "")
                    }
                    while(textToSpeech.isSpeaking){
                    }
                    displaySpeechRecognizer()
                } else if(spokenText.contains("choose")){
                    var subString = spokenText.substringAfter("choose")
                    for (i in 0 until difficulties.size) {
                        if(difficulties[i].text?.let { subString.contains(it, true) } == true){
                            val intent = Intent(this, WaitingRoom::class.java)
                            intent.putExtra("category", category)
                            intent.putExtra("difficulty", difficulties[i].text?.toLowerCase())
                            intent.putExtra("playerId", playerId)
                            intent.putExtra("mode", mode)
                            startActivity(intent)
                        }
                    }

                } else displaySpeechRecognizer()
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