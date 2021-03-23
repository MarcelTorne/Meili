package com.github.epfl.meili

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.epfl.meili.helpers.DateAuxiliary
import com.github.epfl.meili.home.Auth
import com.github.epfl.meili.messages.ChatMessageViewModel
import com.github.epfl.meili.messages.FirebaseMessageDatabaseAdapter
import com.github.epfl.meili.models.ChatMessage
import com.github.epfl.meili.models.User
import com.google.android.gms.maps.model.PointOfInterest
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private var currentUser: User? = null
    private lateinit var groupId: String
    private var messsageSet = HashSet<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        findViewById<RecyclerView>(R.id.recycleview_chat_log).adapter = adapter

        Auth.isLoggedIn.observe(this) {
            Log.d(TAG, "value received" + it)
            verifyAndUpdateUserIsLoggedIn(it)
        }

        verifyAndUpdateUserIsLoggedIn(Auth.isLoggedIn.value!!)
    }

    fun verifyAndUpdateUserIsLoggedIn(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            currentUser = Auth.getCurrentUser()
            val poi = intent.getParcelableExtra<PointOfInterest>("POI_KEY")
            supportActionBar?.title = poi?.name

            groupId = poi?.placeId!!

            Log.d(TAG, "the poi is ${poi.name} and has id ${poi.placeId}")
            ChatMessageViewModel.setMessageDatabase(FirebaseMessageDatabaseAdapter("POI/${poi.placeId}"))

            listenForMessages()

            findViewById<Button>(R.id.button_chat_log).setOnClickListener {
                performSendMessage()
            }
        } else {
            currentUser = null
            supportActionBar?.title = "Not Signed In"
            Auth.signIn(this)
        }
    }


    private fun performSendMessage() {
        val text = findViewById<EditText>(R.id.edit_text_chat_log).text.toString()
        findViewById<EditText>(R.id.edit_text_chat_log).text.clear()

        ChatMessageViewModel.addMessage(
            text,
            currentUser!!.uid,
            groupId,
            System.currentTimeMillis() / 1000,
            currentUser!!.username
        )
    }

    private fun listenForMessages() {

        val groupMessageObserver = Observer<List<ChatMessage>?> { list ->
            var newMessages = list.minus(messsageSet)

            newMessages.forEach { message ->
                Log.d(TAG, "loading message: ${message.text}")

                adapter.add(ChatItem(message, message.fromId == currentUser!!.uid))
            }

            messsageSet.addAll(newMessages)

            //scroll down
            val lastItemPos = adapter.itemCount - 1
            findViewById<RecyclerView>(R.id.recycleview_chat_log).scrollToPosition(lastItemPos)
        }

        ChatMessageViewModel.messages.observe(this, groupMessageObserver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Auth.onActivityResult(this, requestCode, resultCode, data)
    }
}

class ChatItem(private val message: ChatMessage, private val me: Boolean) :
    Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        if (me) {
            return R.layout.chat_from_me_row
        } else {
            return R.layout.chat_from_other_row
        }
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.text_gchat_message).text = message.text
        var date = DateAuxiliary.getDateFromTimestamp(message.timestamp)
        viewHolder.itemView.findViewById<TextView>(R.id.text_chat_timestamp).text =
            DateAuxiliary.getTime(date)
        viewHolder.itemView.findViewById<TextView>(R.id.text_chat_date).text =
            DateAuxiliary.getDay(date)

        if (!me) {
            viewHolder.itemView.findViewById<TextView>(R.id.text_chat_user_other).text =
                message.fromName
        }
    }
}