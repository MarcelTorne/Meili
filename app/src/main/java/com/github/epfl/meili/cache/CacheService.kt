package com.github.epfl.meili.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.github.epfl.meili.MainApplication
import com.github.epfl.meili.util.InternetConnectionService
import com.google.gson.Gson
import java.lang.reflect.Type

open class CacheService<T>(sharedPreferencesKey: String, val classType: Type) {
    // Service for fetching information
    private lateinit var fetcher: ResponseFetcher<T>

    // Object for handling saving data locally on phone
    protected var mPrefs: SharedPreferences = MainApplication.applicationContext()
            .getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
    protected var gsonObject = Gson()

    // In-Object cached values
    var lastResponse: T? = null
    var responseTimestamp: Long = 0L

    /**
     * Sets the object for handling saving data locally on phone
     */
    fun setSharedPreferences(sharedPreferences: SharedPreferences) {
        this.mPrefs = sharedPreferences
    }

    /**
     * Sets the service for fetching information
     */
    fun setResponseFetcher(fetcher: ResponseFetcher<T>) {
        this.fetcher = fetcher
    }

    /**
     * Tries to retrieve object from cache or from service if it is not in cache
     */
    open fun getResponse(arg: Any?, onSuccess: ((T) -> Unit)?, onError: ((Error) -> Unit)?) {
        if (onSuccess != null && onError != null) {
            when {
                isObjectDataValid() -> {

                    Log.d(TAG, "Getting info from in-object")
                    onSuccess(lastResponse!!)
                }
                isCacheValid(retrieveTimeOfResponse()) -> {

                    Log.d(TAG, "Getting info from shared preferences")
                    onSuccess(retrieveCachedResponse())
                }
                internetConnectionServiceProvider().isConnectedToInternet(MainApplication.applicationContext()) -> {
                    // Data saved in the object and on the phone are not valid hence we need to fetch from the API

                    Log.d(TAG, "Getting info from the API")
                    fetcher.fetchResponse(arg, onSuccessSaveResponse { onSuccess(it) }, onError)
                }
                else -> {
                    // If there is no internet connection but some data available then return it even if not valid
                    when {
                        responseTimestamp > 0L -> {
                            Log.d(TAG, "Getting old info from in-object")
                            onSuccess(lastResponse!!)
                        }
                        retrieveTimeOfResponse() > 0L -> {
                            Log.d(TAG, "Getting old info from shared preferences")
                            onSuccess(retrieveCachedResponse())
                        }
                        else -> {
                            Log.d(TAG, "Not possible to retrieve response")
                            onError(Error("No Internet Connection and no cached data"))
                        }
                    }
                }
            }
        }
    }

    private fun onSuccessSaveResponse(onSuccess: ((T) -> Unit)): (T) -> Unit {
        return { response ->
            saveResponse(response)
            onSuccess(response)
        }
    }

    /**
     * Updates the in-Object cached values
     */
    protected open fun saveResponse(response: T) {
        saveTimeOfResponse()

        val prefsEditor = mPrefs.edit()
        val jsonPoiList = gsonObject.toJson(response, classType)
        prefsEditor.putString(RESPONSE_KEY, jsonPoiList)
        prefsEditor.apply()

        lastResponse = response
    }

    private fun saveTimeOfResponse() {
        val tsLong = System.currentTimeMillis() / 1000

        val prefsEditor = mPrefs.edit()
        prefsEditor.putLong(TIMESTAMP_KEY, tsLong)
        prefsEditor.apply()

        responseTimestamp = tsLong
    }

    private fun retrieveCachedResponse(): T {
        val json = mPrefs.getString(RESPONSE_KEY, "")
        return gsonObject.fromJson(json, classType) as T
    }

    private fun retrieveTimeOfResponse(): Long {
        return mPrefs.getLong(TIMESTAMP_KEY, 0L)
    }

    /**
     * Data is considered valid if it was retrieved less than 1 hour ago
     *
     * @return whether the data saved in shared preferences is valid or not
     */
    protected open fun isCacheValid(cachedTimestamp: Long): Boolean {
        return verifyValidity(cachedTimestamp)
    }

    /**
     * Data is considered valid if it was retrieved less than 1 hour ago
     *
     * @return whether the data in the object is valid or not
     */
    protected open fun isObjectDataValid(): Boolean {
        return verifyValidity(responseTimestamp)
    }

    private fun verifyValidity(cachedTimestamp: Long): Boolean {
        val currentTimestamp = System.currentTimeMillis() / 1000

        return currentTimestamp - cachedTimestamp < CACHE_TIME_LIMIT
    }

    companion object {
        const val RESPONSE_KEY = "response"
        const val TIMESTAMP_KEY = "timestamp"
        const val CACHE_TIME_LIMIT = 60 * 60 // 1 hour in seconds
        const val TAG = "CacheService"
        var internetConnectionServiceProvider: () -> InternetConnectionService = { InternetConnectionService() }
    }
}