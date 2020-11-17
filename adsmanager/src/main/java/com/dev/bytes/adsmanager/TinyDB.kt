package com.dev.bytes.adsmanager


import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager
import android.text.TextUtils
import com.google.gson.Gson
import java.io.File
import java.util.*


class TinyDB constructor(private val appContext: Context) {

    companion object {
        @JvmStatic
        fun getInstance(context: Context) =
            TinyDB(context)
    }

    private var preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(appContext)

    // Getters

    /**
     * Get int value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
     *
     * @param key SharedPreferences key
     * @return int value at 'key' or 'defaultValue' if key not found
     */
    fun getInt(key: String): Int {
        return preferences.getInt(key, -1)
    }

    /**
     * Get parsed ArrayList of Integers from SharedPreferences at 'key'
     *
     * @param key SharedPreferences key
     * @return ArrayList of Integers
     */
    fun getListInt(key: String): ArrayList<Int> {
        val myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚")
        val arrayToList = arrayListOf(listOf(myList))
        val newList = /*ArrayList()*/arrayListOf<Int>()

        for (item in arrayToList)
            newList.add(Integer.parseInt(item.toString()))

        return newList
    }

    /**
     * Get long value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
     *
     * @param key          SharedPreferences key
     * @param defaultValue long value returned if key was not found
     * @return long value at 'key' or 'defaultValue' if key not found
     */
    fun getLong(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }

    /**
     * Get float value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
     *
     * @param key SharedPreferences key
     * @return float value at 'key' or 'defaultValue' if key not found
     */
    fun getFloat(key: String): Float {
        return preferences.getFloat(key, 0f)
    }

    /**
     * Get double value from SharedPreferences at 'key'. If exception thrown, return 'defaultValue'
     *
     * @param key          SharedPreferences key
     * @param defaultValue double value returned if exception is thrown
     * @return double value at 'key' or 'defaultValue' if exception is thrown
     */
    fun getDouble(key: String, defaultValue: Double): Double {
        val number = getString(key)

        return try {
            java.lang.Double.parseDouble(number!!)

        } catch (e: NumberFormatException) {
            defaultValue
        }

    }

    /**
     * Get parsed ArrayList of Double from SharedPreferences at 'key'
     *
     * @param key SharedPreferences key
     * @return ArrayList of Double
     */
    fun getListDouble(key: String): ArrayList<Double> {
        val myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚")
        val arrayToList = ArrayList(listOf(myList))
        val newList = ArrayList<Double>()

        for (item in arrayToList)
            newList.add(java.lang.Double.parseDouble(item.toString()))

        return newList
    }

    /**
     * Get String value from SharedPreferences at 'key'. If key not found, return ""
     *
     * @param key SharedPreferences key
     * @return String value at 'key' or "" (empty String) if key not found
     */
    fun getString(key: String, defaultValue: String = ""): String? {
        return preferences.getString(key, defaultValue)
    }

    /**
     * Get parsed ArrayList of String from SharedPreferences at 'key'
     *
     * @param key SharedPreferences key
     * @return ArrayList of String
     */
    fun getListString(key: String): MutableList<String?>? {
        return try {
            TextUtils.split(preferences.getString(key, ""), "‚‗‚")
                .toMutableList() /*as ArrayList<String>*/
        } catch (e: java.lang.Exception) {
            null
        }
    }

    /**
     * Get boolean value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
     *
     * @param key SharedPreferences key resource ID in strings.xml
     * @return boolean value at 'key' or 'defaultValue' if key not found
     */
    fun getBoolean(keyResID: Int, defaultValue: Boolean = false): Boolean {
        return getBoolean(appContext.getString(keyResID), defaultValue)
    }

    /**
     * Get boolean value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
     *
     * @param key SharedPreferences key
     * @return boolean value at 'key' or 'defaultValue' if key not found
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }


//    public ArrayList<Object> getListObject(String key, Class<?> mClass){
//    	Gson gson = new Gson();
//
//    	ArrayList<String> objStrings = getListString(key);
//    	ArrayList<Object> objects =  new ArrayList<Object>();
//
//    	for(String jObjString : objStrings){
//    		Object value  = gson.fromJson(jObjString,  mClass);
//    		objects.add(value);
//    	}
//    	return objects;
//    }

    fun <T> getListObject(key: String, mClass: Class<T>): ArrayList<T> {
        val gson = Gson()

        val objStrings = getListString(key)
        val objects = arrayListOf<T>()

        if (objStrings != null) {
            for (jObjString in objStrings) {
                val value = gson.fromJson(jObjString, mClass)
                objects.add(value)
            }
        }
        return objects
    }

    fun getObject(key: String, classOfT: Class<*>): Any {

        val json = getString(key)
        return Gson().fromJson(json, classOfT) ?: throw NullPointerException()
    }


    // Put methods

    /**
     * Put int value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value int value to be added
     */
    fun putInt(key: String, value: Int) {
        checkForNullKey(key)
        preferences.edit().putInt(key, value).apply()
    }

    /**
     * Put ArrayList of Integer into SharedPreferences with 'key' and save
     *
     * @param key     SharedPreferences key
     * @param intList ArrayList of Integer to be added
     */
    fun putListInt(key: String, intList: ArrayList<Int>) {
        checkForNullKey(key)
        val myIntList = intList.toArray(arrayOfNulls<Int>(intList.size))
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myIntList)).apply()
    }

    /**
     * Put long value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value long value to be added
     */
    fun putLong(key: String, value: Long) {
        checkForNullKey(key)
        preferences.edit().putLong(key, value).apply()
    }

    /**
     * Put float value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value float value to be added
     */
    fun putFloat(key: String, value: Float) {
        checkForNullKey(key)
        preferences.edit().putFloat(key, value).apply()
    }

    /**
     * Put double value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value double value to be added
     */
    fun putDouble(key: String, value: Double) {
        checkForNullKey(key)
        putString(key, value.toString())
    }

    /**
     * Put ArrayList of Double into SharedPreferences with 'key' and save
     *
     * @param key        SharedPreferences key
     * @param doubleList ArrayList of Double to be added
     */
    fun putListDouble(key: String, doubleList: ArrayList<Double>) {
        checkForNullKey(key)
        val myDoubleList = doubleList.toArray(arrayOfNulls<Double>(doubleList.size))
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myDoubleList)).apply()
    }

    /**
     * Put String value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value String value to be added
     */
    fun putString(key: String, value: String) {
        checkForNullKey(key)
        checkForNullValue(value)
        preferences.edit().putString(key, value).apply()
    }

    /**
     * Put ArrayList of String into SharedPreferences with 'key' and save
     *
     * @param key        SharedPreferences key
     * @param stringList ArrayList of String to be added
     */
    fun putListString(key: String, stringList: ArrayList<String>) {
        checkForNullKey(key)
        val myStringList = stringList.toArray(arrayOfNulls<String>(stringList.size))
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply()
    }

    /**
     * Put boolean value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value boolean value to be added
     */
    fun putBoolean(key: String, value: Boolean) {
        checkForNullKey(key)
        preferences.edit().putBoolean(key, value).apply()
    }


    /**
     * Put ObJect any position into SharedPrefrences with 'key' and save
     *
     * @param key SharedPreferences key
     * @param obj is the Object you want to put
     */
    fun putObject(key: String, obj: Any) {
        checkForNullKey(key)
        val gson = Gson()
        putString(key, gson.toJson(obj))
    }

//    public void putListObject(String key, ArrayList<Object> objArray) {
//        checkForNullKey(key);
//        Gson gson = new Gson();
//        ArrayList<String> objStrings = new ArrayList<String>();
//        for (Object obj : objArray) {
//            objStrings.add(gson.toJson(obj));
//        }
//        putListString(key, objStrings);
//    }

    fun <T> putListObject(key: String, objArray: ArrayList<T>) {
        checkForNullKey(key)
        val gson = Gson()
        val objStrings = ArrayList<String>()
        for (obj in objArray) {
            objStrings.add(gson.toJson(obj))
        }
        putListString(key, objStrings)
    }

    /**
     * Remove SharedPreferences item with 'key'
     *
     * @param key SharedPreferences key
     */
    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    /**
     * Delete image file at 'path'
     *
     * @param path path of image file
     * @return true if it successfully deleted, false otherwise
     */
    fun deleteImage(path: String): Boolean {
        return File(path).delete()
    }


    /**
     * Clear SharedPreferences (remove everything)
     */
    fun clear() {
        preferences.edit().clear().apply()
    }

    /**
     * Retrieve all values from SharedPreferences. Do not modify collection return by method
     *
     * @return a Map representing a list of key/value pairs from SharedPreferences
     */
    fun getAll(): Map<String, *> {
        return preferences.all
    }


    /**
     * Register SharedPreferences change listener
     *
     * @param listener listener object of OnSharedPreferenceChangeListener
     */
    fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {

        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregister SharedPreferences change listener
     *
     * @param listener listener object of OnSharedPreferenceChangeListener to be unregistered
     */
    fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {

        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }


    /**
     * Check if external storage is writable or not
     *
     * @return true if writable, false otherwise
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    /**
     * Check if external storage is readable or not
     *
     * @return true if readable, false otherwise
     */
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()

        return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     *
     * @param key pref key
     */
    private fun checkForNullKey(key: String?) {
        if (key == null) {
            throw NullPointerException()
        }
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     *
     * @param value pref key
     */
    private fun checkForNullValue(value: String?) {
        if (value == null) {
            throw NullPointerException()
        }
    }

    fun getLangInt(key: String?): Int {
        return preferences.getInt(key, -1)
    }
}