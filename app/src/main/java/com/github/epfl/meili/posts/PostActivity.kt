package com.github.epfl.meili.posts

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.github.epfl.meili.R
import com.github.epfl.meili.database.FirebaseStorageService
import com.github.epfl.meili.database.FirestoreDatabase
import com.github.epfl.meili.home.Auth
import com.github.epfl.meili.models.Comment
import com.github.epfl.meili.models.Post
import com.github.epfl.meili.models.User
import com.github.epfl.meili.profile.UserProfileLinker
import com.github.epfl.meili.profile.friends.UserInfoService
import com.github.epfl.meili.util.ClickListener
import com.github.epfl.meili.util.ImageSetter
import com.github.epfl.meili.util.MeiliRecyclerAdapter
import com.github.epfl.meili.util.MeiliViewModel
import com.github.epfl.meili.util.RecyclerViewInitializer.initRecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostActivity : AppCompatActivity(), UserProfileLinker<Comment>, ClickListener {
    //TODO: fix comment text not getting displayed
    //TODO: fix poi activity test
    companion object {
        private const val TAG = "PostActivity"
        private val DEFAULT_URI =
                Uri.parse("https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Forum_romanum_6k_%285760x2097%29.jpg/2880px-Forum_romanum_6k_%285760x2097%29.jpg")
        const val POST_ID = "Post_ID"

        var serviceProvider: () -> UserInfoService = { UserInfoService() }
    }

    override lateinit var recyclerAdapter: MeiliRecyclerAdapter<Pair<Comment, User>>
    override lateinit var usersMap: Map<String, User>

    private lateinit var viewModel: MeiliViewModel<Comment>

    private lateinit var imageView: ImageView
    private lateinit var commentButton: Button
    private lateinit var editText: EditText
    private lateinit var addCommentButton: Button

    private lateinit var postId: String
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val post: Post = intent.getParcelableExtra(Post.TAG)!!
        postId = intent.getStringExtra(POST_ID)!!

        initViews(post)

        FirebaseStorageService.getDownloadUrl(
                "images/forum/$postId",
                { uri -> getDownloadUrlCallback(uri) },
                { exception ->
                    Log.e(TAG, "Image not found", exception)
                    getDownloadUrlCallback(DEFAULT_URI)
                }
        )

        usersMap = HashMap()

        initViewModel()
        initRecyclerAdapter()
        initLoggedInListener()
    }

    private fun getDownloadUrlCallback(uri: Uri) {
        Picasso.get().load(uri).into(imageView)
    }

    private fun initViews(post: Post) {
        this.post = post
        val titleView: TextView = findViewById(R.id.post_title)
        val textView: TextView = findViewById(R.id.post_text)
        titleView.text = post.title
        textView.text = post.text

        imageView = findViewById(R.id.post_image)
        commentButton = findViewById(R.id.comment_button)
        editText = findViewById(R.id.edit_comment)
        addCommentButton = findViewById(R.id.add_comment)
        commentButton.setOnClickListener { showEditCommentView() }
        addCommentButton.setOnClickListener { addComment() }

        val singletonList = ArrayList<String>()
        singletonList.add(post.authorUid)
        serviceProvider().getUserInformation(singletonList, { onAuthorInfoReceived(it) }) {}
    }

    private fun onAuthorInfoReceived(users: Map<String, User>) {
        val author = users[post.authorUid]
        val authorView: TextView = findViewById(R.id.userName)
        val imageAuthor: CircleImageView = findViewById(R.id.userImage)

        authorView.text = author?.username
        ImageSetter.setImageInto(author!!.uid, imageAuthor)
    }

    private fun initViewModel() {
        @Suppress("UNCHECKED_CAST")
        viewModel =
                ViewModelProvider(this).get(MeiliViewModel::class.java) as MeiliViewModel<Comment>

        viewModel.initDatabase(FirestoreDatabase("forum/$postId/comments", Comment::class.java))
        viewModel.getElements().observe(this, { map ->
            commentListener(map)
        })
    }

    private fun commentListener(commentsMap: Map<String, Comment>) {
        val newUsers = ArrayList<String>()
        for ((commentId, comment) in commentsMap) {
            newUsers.add(comment.authorUid)
        }

        serviceProvider().getUserInformation(newUsers, { onUsersInfoReceived(it, commentsMap) },
                { Log.d(TAG, "Error when fetching users information") })
    }

    override fun onUsersInfoReceived(users: Map<String, User>, commentsMap: Map<String, Comment>) {
        Log.d(TAG, "on users info received")
        Log.d(TAG, commentsMap.toString())
        Log.d(TAG, users.toString())
        usersMap = HashMap(usersMap) + users
        val commentsAndUsersMap = HashMap<String, Pair<Comment, User>>()
        for ((commentId, comment) in commentsMap) {
            val user = usersMap[comment.authorUid]
            Log.d(TAG, user.toString())
            if (user != null) {
                commentsAndUsersMap[commentId] = Pair(comment, user)
            }
        }

        recyclerAdapter.submitList(commentsAndUsersMap.toList())
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun initRecyclerAdapter() {
        recyclerAdapter = CommentsRecyclerAdapter(this)
        val recyclerView: RecyclerView = findViewById(R.id.comments_recycler_view)
        initRecyclerView(recyclerAdapter, recyclerView, this)
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
    }

    private fun initLoggedInListener() {
        Auth.isLoggedIn.observe(this, { loggedIn ->
            val layout: LinearLayout = findViewById(R.id.new_comment_layout)
            layout.visibility = if (loggedIn)
                View.VISIBLE
            else
                View.INVISIBLE
        })
    }

    private fun showEditCommentView() {
        commentButton.visibility = View.INVISIBLE
        editText.visibility = View.VISIBLE
        addCommentButton.visibility = View.VISIBLE

        editText.text.clear()
    }

    private fun hideEditCommentView() {
        commentButton.visibility = View.VISIBLE
        editText.visibility = View.INVISIBLE
        addCommentButton.visibility = View.INVISIBLE
    }

    private fun addComment() {
        if (Auth.getCurrentUser() == null) {
            error("Unconnected user is trying to add comment")
        }
        val user: User = Auth.getCurrentUser()!!
        val commentId = "${user.uid}${System.currentTimeMillis()}"
        val text = editText.text.toString()

        viewModel.addElement(commentId, Comment(user.uid, text))

        hideEditCommentView()
    }
}