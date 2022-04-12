package com.jiayou.shanghai

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

object Card {

    private const val PREF_NAME = "card"
    private const val KEY_PASSWORD = "password"

    fun setPassword(context: Context, password: String) {
        getPreferences(context).edit { putString(KEY_PASSWORD, password) }
    }

    fun getPassword(context: Context): String {
        return getPreferences(context).getString(KEY_PASSWORD, null).orEmpty()
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, AppCompatActivity.MODE_PRIVATE)
    }
}