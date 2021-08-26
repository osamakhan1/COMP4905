package com.comp4905.triviagameapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject
import androidx.recyclerview.widget.LinearLayoutManager
import com.comp4905.triviagameapp.databinding.ActivityMainBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), CategoryAdapter.OnItemClickListener {
    private val categoriesList = ArrayList<ListItem>()
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
    

    private val SPEECH_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = intent.getStringExtra("mode")
        playerId = intent.getStringExtra("playerId")
        println(playerId)
        generateCategories()
        //myRef.setValue("Hello, World!")
        if(mode == "driving") {
            displaySpeechRecognizer()
        }

    }

    //override fun onNewIntent(intent: Intent) {
    //    super.onNewIntent(intent)
    //    handleIntent(intent)
   // }

    private fun generateCategories() {
        val url = "https://opentdb.com/api_category.php"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val categories = response.getJSONArray("trivia_categories")
                for (i in 0 until categories.length()){
                    val item = ListItem(categories.getJSONObject(i).getString("name"), categories.getJSONObject(i).getString("id"))
                    categoriesList.add(item)
                }
                val binding = ActivityMainBinding.inflate(layoutInflater)
                val view = binding.root
                setContentView(view)
                val db = Firebase.firestore
                val docRef = db.collection("player").document(playerId)
                docRef.get().addOnSuccessListener { documentSnapshot ->
                    println(documentSnapshot.getString("username"))
                    binding.usernameDisplay.text = documentSnapshot.getString("username")
                    binding.totalScore.text = documentSnapshot.get("score").toString()
                }
                binding.recyclerView.adapter = CategoryAdapter(categoriesList, this)
                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.setHasFixedSize(true)
            },
            { error ->
                // TODO: Handle error
            }
        )

        ApiRequests.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    override fun onItemClick(position: Int) {
        val clickedItem = categoriesList[position]
        val intent = Intent(this, DifficultySelect::class.java)
        intent.putExtra("name", clickedItem.text)
        intent.putExtra("id", clickedItem.id)
        intent.putExtra("playerId", playerId)
        intent.putExtra("mode", mode)
        startActivity(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkData: Uri? = intent.data
        if (appLinkData != null) {
            if (appLinkData.lastPathSegment.equals("read")) {
                for (i in 0 until categoriesList.size) {
                    textToSpeech.speak(categoriesList.get(i).text, TextToSpeech.QUEUE_FLUSH, null, "")
                }
            }
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
                if(spokenText.contains("read it")){
                    for (i in 0 until categoriesList.size) {
                        textToSpeech.speak(categoriesList.get(i).text, TextToSpeech.QUEUE_ADD, null, "")
                    }
                    while(textToSpeech.isSpeaking){
                    }
                    displaySpeechRecognizer()
                } else if(spokenText.contains("choose")){
                    var subString = spokenText.substringAfter("choose")
                    for (i in 0 until categoriesList.size) {
                        if(categoriesList[i].text?.let { subString.contains(it, true) } == true){
                            val intent = Intent(this, DifficultySelect::class.java)
                            intent.putExtra("name", categoriesList[i].text)
                            intent.putExtra("id", categoriesList[i].id)
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

    override fun onPause() {
        textToSpeech.stop()
        super.onPause()
    }

    override fun onDestroy() {
        //textToSpeech.shutdown()
        super.onDestroy()
    }
}