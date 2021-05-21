package com.github.epfl.meili.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
        var authorUid: String = "",
        var text: String = ""
) : Parcelable {
    companion object {
        const val TAG = "Comment"
    }
}