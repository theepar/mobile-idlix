package com.example.watchmobile.utils

import android.content.Context
import com.example.watchmobile.domain.models.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryManager {
    private const val PREF_NAME = "watch_history"
    private const val KEY_HISTORY = "history_list"
    private val gson = Gson()

    fun saveToHistory(context: Context, movie: Movie) {
        val history = getHistory(context).toMutableList()
        // Remove existing if same slug
        history.removeAll { it.slug == movie.slug }
        // Add to top
        history.add(0, movie)
        // Keep only last 50
        val limitedHistory = history.take(50)
        
        val json = gson.toJson(limitedHistory)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY, json)
            .apply()
    }

    fun getHistory(context: Context): List<Movie> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HISTORY, null) ?: return emptyList()
        
        val type = object : TypeToken<List<Movie>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearHistory(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HISTORY)
            .apply()
    }
}
