package com.peter.dailypsalms

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BibleRepository(private val context: Context, private val versionCode: String) {

    fun loadPsalms(): List<ChapterData> {
        val fileName = "psalms_$versionCode.json"
        val inputStream = context.assets.open(fileName)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<ChapterData>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

    fun loadProverbs(): List<ChapterData> {
        val fileName = "proverbs_$versionCode.json"
        val inputStream = context.assets.open(fileName)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<ChapterData>>() {}.type
        return Gson().fromJson(jsonString, type)
    }
}