package com.mikirinkode.pikul.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.mikirinkode.pikul.data.model.UserAccount

class LocalPreference(
    private val context: Context
) {
    private var mSharedPreferences: SharedPreferences = context.getSharedPreferences("user_pref", 0)

    fun saveInt(key: String, value: Int) {
        val editor = mSharedPreferences?.edit()
        editor?.putInt(key, value)
        editor?.apply()
    }

    fun getInt(key: String): Int? {
        return if (isKeyExists(key)) {
            mSharedPreferences?.getInt(key, 0)
        } else 0
    }

    fun saveBoolean(key: String, value: Boolean) {
        val editor = mSharedPreferences?.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun getBoolean(key: String): Boolean? {
        return if (isKeyExists(key)) {
            mSharedPreferences?.getBoolean(key, false)
        } else {
            false
        }
    }

    fun saveFloat(key: String, value: Float) {
        val editor = mSharedPreferences?.edit()
        editor?.putFloat(key, value)
        editor?.apply()
    }

    fun getFloat(key: String): Float? {
        return if (isKeyExists(key)) {
            mSharedPreferences?.getFloat(key, 0.0f)
        } else 0.0f
    }


    fun saveLong(key: String, value: Long) {
        val editor = mSharedPreferences?.edit()
        editor?.putLong(key, value)
        editor?.apply()
    }

    fun getLong(key: String): Long? {
        return if (isKeyExists(key)) {
            mSharedPreferences?.getLong(key, 0)
        } else 0
    }

    fun saveString(key: String, value: String) {
        val editor = mSharedPreferences?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun getString(key: String): String? {
        return if (isKeyExists(key)) {
            mSharedPreferences?.getString(key, null)
        } else null
    }

    fun <T> saveObject(key: String, `object`: T) {
        val objectString = Gson().toJson(`object`)
        val editor = mSharedPreferences?.edit()
        editor?.putString(key, objectString)
        editor?.apply()
    }

    fun <T> getObject(key: String, classType: Class<T>): T? {
        if (isKeyExists(key)) {
            val objectString = mSharedPreferences?.getString(key, null)
            if (objectString != null) {
                return Gson().fromJson(objectString, classType)
            }
        }
        return null
    }

    fun <T> saveObjectsList(key: String, objectList: List<T>) {
        val objectString = Gson().toJson(objectList)
        val editor = mSharedPreferences?.edit()
        editor?.putString(key, objectString)
        editor?.apply()
    }

    fun <T> getObjectsList(key: String, classType: Class<T>): List<T>? {
        return if (isKeyExists(key)) {
            val objectString = mSharedPreferences?.getString(key, null)
            if (objectString != null) {
                Gson().fromJson(objectString, object : TypeToken<List<T>>() {
                }
                    .where(object : TypeParameter<T>() {

                    }, classType)
                    .type
                )
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun startSession(user: UserAccount) {
        saveBoolean(LocalPreferenceConstants.IS_LOGGED_IN, true)
        saveObject(LocalPreferenceConstants.USER, user)
    }

    fun clearSession() {
        val editor = mSharedPreferences?.edit()
        editor?.clear()
        editor?.apply()
    }

    fun deleteValue(key: String): Boolean {
        if (this.isKeyExists(key)) {
            val editor = mSharedPreferences?.edit()
            editor?.remove(key)
            editor?.apply()
            return true
        }

        return false
    }

    fun isKeyExists(key: String): Boolean {
        val map = mSharedPreferences?.all
        return map != null && map.containsKey(key)
    }

}