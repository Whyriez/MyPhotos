package com.example.myphotos.Server.helper

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    private val PREF_NAME = "myPhotos"
    private val sharedPref : SharedPreferences
    val editor : SharedPreferences.Editor

    init{
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }
    fun put(key: String, value: String){
        editor.putString(key, value).apply()
    }

    fun putArray(key: String, value: String){
        editor.putString(key, value).apply()
    }

    fun addValueToSet(key: String, newValue: String) {
        val existingSet = sharedPref.getStringSet(key, emptySet())
        val newSet = existingSet!!.toMutableSet()
        newSet.add(newValue)

        editor.putStringSet(key, newSet)
        editor.apply()
    }

    fun getString(key: String) : String? {
        return sharedPref.getString(key, null)
    }
    fun put(key: String, value: Boolean){
        editor.putBoolean(key, value).apply()
    }
    fun getBoolean(key: String) : Boolean{
        return sharedPref.getBoolean(key, false)
    }
    fun clear(){
        editor.clear().apply()
    }
}