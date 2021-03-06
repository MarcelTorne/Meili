package com.github.epfl.meili.util.navigation

import android.content.Intent
import android.os.Bundle
import com.github.epfl.meili.R
import com.github.epfl.meili.map.MapActivity
import com.github.epfl.meili.posts.feed.FeedActivity
import com.github.epfl.meili.profile.ProfileActivity

abstract class HomeActivity(layout: Int, activityId: Int): NavigableActivity(layout, activityId) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    override fun getNavigationIntent(id: Int): Intent {
        return when (id) {
            R.id.map_activity -> Intent(this, MapActivity::class.java)
            R.id.profile_activity -> Intent(this, ProfileActivity::class.java)
            R.id.feed_activity -> Intent(this, FeedActivity::class.java)
            else -> Intent(this, MapActivity::class.java)
        }
    }
}