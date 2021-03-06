package com.github.epfl.meili.models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class PointOfInterest(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var name: String = "",
    var uid: String = "",
    var icon: String = "",
    var poiTypes: List<String> = ArrayList(),
    var openNow: Boolean? = null
) : Parcelable {

    companion object {
        const val START_CHAR = '{'
        const val END_CHAR = '}'
        const val TAG = "PointOfInterest"
    }

    override fun toString(): String {
        return START_CHAR + "POI:" + "lat:" + latitude + "long:" + longitude + ",name:" + name + ",uid:" + uid + ",icon:" + icon + END_CHAR
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other::class.java == PointOfInterest::class.java) {
            val otherPoi = other as PointOfInterest
            return otherPoi.latitude == latitude && otherPoi.longitude == longitude && otherPoi.name == name
                    && otherPoi.uid == uid && otherPoi.icon == icon
        }

        return false
    }

    fun getLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + uid.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + poiTypes.hashCode()
        result = 31 * result + (openNow?.hashCode() ?: 0)
        return result
    }
}