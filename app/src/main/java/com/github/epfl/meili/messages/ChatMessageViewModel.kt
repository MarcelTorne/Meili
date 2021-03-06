package com.github.epfl.meili.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.epfl.meili.models.ChatMessage
import java.util.*

object ChatMessageViewModel : ViewModel(),
    Observer {

    private lateinit var database: MessageDatabase

    private val mutable_messages = MutableLiveData<List<ChatMessage>?>()
    val messages: LiveData<List<ChatMessage>?> = mutable_messages


    /**
     * Sets the database for messages
     */
    fun setMessageDatabase(database: MessageDatabase) {
        this.database = database
        database.addObserver(this)
    }

    /**
     * Adds a message to the database
     */
    fun addMessage(text: String, fromId: String, toId: String, timeStamp: Long) {
        val message = ChatMessage(
            text,
            fromId,
            toId,
            timeStamp
        )

        database.addMessageToDatabase(message)
    }

    override fun update(o: Observable?, arg: Any?) {
        //async
        mutable_messages.postValue(database.getMessages())
    }
}