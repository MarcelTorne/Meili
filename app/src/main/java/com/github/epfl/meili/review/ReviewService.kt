package com.github.epfl.meili.review

import androidx.lifecycle.MutableLiveData
import com.github.epfl.meili.models.Review

interface ReviewService {
    fun getReviews(poi_id: String): MutableLiveData<List<Review>>
}