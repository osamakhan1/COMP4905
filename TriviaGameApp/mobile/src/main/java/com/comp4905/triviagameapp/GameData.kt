package com.comp4905.triviagameapp

data class GameData(var category: String = "", var difficulty: String = "", var player1: PlayerData? = null, var player2: PlayerData? = null, var questions: ArrayList<QuestionItem> = arrayListOf<QuestionItem>(), var started: Boolean = false)
