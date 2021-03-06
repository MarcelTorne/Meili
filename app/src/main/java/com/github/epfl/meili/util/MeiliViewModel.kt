package com.github.epfl.meili.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.epfl.meili.database.Database
import java.util.*

open class MeiliViewModel<T> : ViewModel(), Observer {

    private val mElements: MutableLiveData<Map<String, T>> = MutableLiveData()

    protected lateinit var database: Database<T>

    fun initDatabase(database: Database<T>) {
        this.database = database
        database.addObserver(this)
    }

    fun getElements(): LiveData<Map<String, T>> = mElements

    fun addElement(id: String, element: T) = database.addElement(id, element)

    fun removeElement(id: String) = database.removeElement(id)

    override fun update(o: Observable?, arg: Any?) {
        val elements: Map<String, T> = database.elements
        mElements.postValue(elements)
    }

    fun onDestroy() {
        database.onDestroy()
    }
}