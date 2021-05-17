package com.github.epfl.meili.forum

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.epfl.meili.R
import com.github.epfl.meili.models.Post
import com.github.epfl.meili.models.User
import com.github.epfl.meili.util.ClickListener
import com.github.epfl.meili.util.MeiliRecyclerAdapter
import com.github.epfl.meili.util.MeiliWithUserRecyclerViewHolder
import de.hdodenhof.circleimageview.CircleImageView

class ForumRecyclerAdapter(private val forumViewModel: ForumViewModel, private val listener: ClickListener) :
        MeiliRecyclerAdapter<Pair<Post, User>>() {
    private var userId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            PostViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false),
                    forumViewModel, listener
            )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            (holder as PostViewHolder).bind(items[position].second.second, items[position].second.first, userId)

    fun submitUserInfo(uid: String) {
        userId = uid
    }

    class PostViewHolder(itemView: View, private val forumViewModel: ForumViewModel, listener: ClickListener) :
            MeiliWithUserRecyclerViewHolder<Post>(itemView, listener){

        private val title: TextView = itemView.findViewById(R.id.post_title)
        private val postId: TextView = itemView.findViewById(R.id.post_id)
        private val upvoteButton: ImageButton = itemView.findViewById(R.id.upvote_button)
        private val downvoteButton: ImageButton = itemView.findViewById(R.id.downovte_button)
        private val upvoteCount: TextView = itemView.findViewById(R.id.upvote_count)

        init {
            itemView.findViewById<TextView>(R.id.userName).setOnClickListener(this)
        }

        fun bind(user: User, post: Post, userId: String?) {
            super.bind(user,post)
            postId.text = user.uid

            title.text = post.title

            //show or hide up/downvote depending on user status
            val visibility = if (userId == null) {
                View.GONE
            } else {
                View.VISIBLE
            }
            upvoteButton.visibility = visibility
            downvoteButton.visibility = visibility
            if (userId != null) {
                setupButtons(post.upvoters, post.downvoters, userId, user.uid)
            }
            upvoteCount.text = (post.upvoters.size - post.downvoters.size).toString()
        }

        private fun setupButtons(
                upvoters: ArrayList<String>,
                downvoters: ArrayList<String>,
                userId: String,
                postId: String
        ) {
            when {
                upvoters.contains(userId) -> {
                    upvoteButton.setImageResource(R.mipmap.upvote_filled_foreground)
                    downvoteButton.setImageResource(R.mipmap.downvote_empty_foreground)
                }
                downvoters.contains(userId) -> {
                    upvoteButton.setImageResource(R.mipmap.upvote_empty_foreground)
                    downvoteButton.setImageResource(R.mipmap.downvote_filled_foreground)
                }
                else -> {
                    upvoteButton.setImageResource(R.mipmap.upvote_empty_foreground)
                    downvoteButton.setImageResource(R.mipmap.downvote_empty_foreground)
                }
            }
            upvoteButton.setOnClickListener { forumViewModel.upvote(postId, userId) }
            downvoteButton.setOnClickListener { forumViewModel.downvote(postId, userId) }
        }
    }
}