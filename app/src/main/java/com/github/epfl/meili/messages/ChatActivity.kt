package com.github.epfl.meili.messages

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.epfl.meili.R
import com.github.epfl.meili.auth.Auth
import com.github.epfl.meili.map.MapActivity
import com.github.epfl.meili.models.ChatMessage
import com.github.epfl.meili.models.PointOfInterest
import com.github.epfl.meili.models.User
import com.github.epfl.meili.profile.friends.FriendsListActivity.Companion.FRIEND_KEY
import com.github.epfl.meili.profile.friends.UserInfoService
import com.github.epfl.meili.util.DateAuxiliary
import com.github.epfl.meili.util.navigation.PoiActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


class ChatActivity : PoiActivity(R.layout.activity_chat, R.id.chat_activity) {

    companion object {
        private const val TAG: String = "ChatLogActivity"
        private const val KEYBOARD_THRESHOLD = 244
    }

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private var currentUser: User? = null
    private lateinit var chatId: String
    private var messageList: ArrayList<ChatMessage> = ArrayList()

    private var isGroupChat = false
    private var poi: PointOfInterest? = null

    private lateinit var navigationBar: BottomNavigationView
    private lateinit var chatLogView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navigationBar = findViewById(R.id.navigation)

        chatLogView = findViewById(R.id.recyclerview_chat_log)
        chatLogView.adapter = adapter

        // Hide navigation bar when keyboard is opened
        val chatLogView = findViewById<ConstraintLayout>(R.id.chat_log_view)
        chatLogView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            chatLogView.getWindowVisibleDisplayFrame(r)
            val heightDiff: Int = chatLogView.rootView
                .height - (r.bottom - r.top)
            navigationBar.isVisible = heightDiff > KEYBOARD_THRESHOLD
        }

        Auth.isLoggedIn.observe(this) {
            verifyAndUpdateUserIsLoggedIn(it)
        }

        verifyAndUpdateUserIsLoggedIn(Auth.isLoggedIn.value!!)
    }


    /**
     * Start the chat if the user is logged in
     */
    fun verifyAndUpdateUserIsLoggedIn(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            currentUser = Auth.getCurrentUser()

            val poi = intent.getParcelableExtra<PointOfInterest>(MapActivity.POI_KEY)
            val friend = intent.getParcelableExtra<User>(FRIEND_KEY)
            val databasePath: String

            if (poi != null) {
                this.poi = poi

                supportActionBar?.title = poi.name
                chatId = poi.uid

                setGroupChat(true)

                Log.d(TAG, "Starting chat group at ${poi.name} and has id ${poi.uid}")

                databasePath = "POI/${chatId}"
            } else {
                supportActionBar?.title = friend?.username
                val friendUid: String = friend?.uid!!
                val currentUid: String = currentUser!!.uid

                // The friend chat document in the database is saved under the key with value
                // of the two user ids concatenated in sorted order
                chatId =
                    if (friendUid < currentUid) "$friendUid;$currentUid" else "$currentUid;$friendUid"

                setGroupChat(false)

                Log.d(TAG, "Starting friend chat with ${friend.uid}")

                databasePath = "FriendChat/${chatId}"
            }

            ChatMessageViewModel.setMessageDatabase(FirebaseMessageDatabaseAdapter(databasePath))
            listenForMessages(chatId)

            findViewById<Button>(R.id.button_chat_log).setOnClickListener {
                performSendMessage()
            }

        } else {
            currentUser = null
            supportActionBar?.title = getString(R.string.not_signed_in)
            Auth.signInIntent(this)
        }
    }

    private fun setGroupChat(isGroupChat: Boolean) {
        this.isGroupChat = isGroupChat
        if (!isGroupChat) {
            navigationBar.isVisible = false
        }
    }

    private fun performSendMessage() {
        val text = findViewById<EditText>(R.id.edit_text_chat_log).text.toString()
        findViewById<EditText>(R.id.edit_text_chat_log).text.clear()

        ChatMessageViewModel.addMessage(
            text,
            currentUser!!.uid,
            chatId,
            System.currentTimeMillis() / 1000
        )
    }

    private fun listenForMessages(chatID: String) {

        val groupMessageObserver = Observer<List<ChatMessage>?> { list ->
            val newMessages = list.minus(messageList)
            var prevMessage: ChatMessage? = if (messageList.isEmpty()) null else messageList.last()
            newMessages.filter { message -> message.toId == chatID }.forEach { message ->
                val isDisplayingDate: Boolean = if (prevMessage != null) {
                    DateAuxiliary.getDay(DateAuxiliary.getDateFromTimestamp(message.timestamp)) != DateAuxiliary.getDay(
                        DateAuxiliary.getDateFromTimestamp(prevMessage!!.timestamp)
                    )
                } else {
                    true
                }
                adapter.add(
                    ChatItem(
                        message,
                        message.fromId == currentUser!!.uid,
                        isGroupChat,
                        isDisplayingDate
                    )
                )

                prevMessage = message
            }

            messageList.addAll(newMessages)
            //scroll down
            val lastItemPos = adapter.itemCount - 1
            chatLogView.scrollToPosition(lastItemPos)
        }

        ChatMessageViewModel.messages.observe(this, groupMessageObserver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Auth.onActivityResult(this, requestCode, resultCode, data) {}
    }
}

class ChatItem(
    private val message: ChatMessage,
    private val isChatMessageFromCurrentUser: Boolean,
    private val isGroupChat: Boolean,
    private val isDisplayingDate: Boolean,
) :
    Item<GroupieViewHolder>() {

    companion object {
        var serviceProvider: () -> UserInfoService = { UserInfoService() }
    }

    override fun getLayout(): Int {
        return if (isChatMessageFromCurrentUser && isDisplayingDate) {
            R.layout.chat_from_me_row_with_date
        } else if (isChatMessageFromCurrentUser && !isDisplayingDate) {
            R.layout.chat_from_me_row
        } else if (!isChatMessageFromCurrentUser && isDisplayingDate) {
            R.layout.chat_from_other_row_with_date
        } else {
            R.layout.chat_from_other_row
        }
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.text_gchat_message).text = message.text
        val date = DateAuxiliary.getDateFromTimestamp(message.timestamp)
        viewHolder.itemView.findViewById<TextView>(R.id.text_chat_timestamp).text =
            DateAuxiliary.getTime(date)
        if (isDisplayingDate) viewHolder.itemView.findViewById<TextView>(R.id.text_chat_date).text =
            DateAuxiliary.getDay(date)
        if (!isChatMessageFromCurrentUser) {
            val nameView = viewHolder.itemView.findViewById<TextView>(R.id.text_chat_user_other)
            if (isGroupChat) {
                serviceProvider().getUserInformation(
                    listOf(message.fromId)
                ) { getUsername(it, nameView, message.fromId) }
            } else {
                nameView.text = ""
            }
        }
    }

    private fun getUsername(map: Map<String, User>, nameView: TextView, uid: String) {
        nameView.text = map[uid]!!.username
    }
}