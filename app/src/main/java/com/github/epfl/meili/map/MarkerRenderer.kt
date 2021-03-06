package com.github.epfl.meili.map

import android.content.Context
import com.github.epfl.meili.poi.PointOfInterestStatus
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * PoiRenderer is the class which renders POIs as Markers on the Map and also handles cluster rendering
 */
open class MarkerRenderer(
    context: Context?,
    map: GoogleMap?,
    private val clusterManager: ClusterManager<MarkerItem>
) : DefaultClusterRenderer<MarkerItem>(context, map, clusterManager) {

    private var poiStatusMap: Map<MarkerItem, PointOfInterestStatus> = HashMap()
    private var meiliLensPoi: MarkerItem? = null

    override fun onBeforeClusterItemRendered(item: MarkerItem, markerOptions: MarkerOptions) {
        markerOptions.icon(getIcon(item))
    }

    override fun onClusterItemRendered(item: MarkerItem, marker: Marker) {
        marker.setIcon(getIcon(item))
    }

    private fun getIcon(item: MarkerItem): BitmapDescriptor {
        return if (meiliLensPoi?.poi == item.poi) {
            MEILI_LENS_ICON
        } else {
            when (poiStatusMap[item]) {
                PointOfInterestStatus.REACHABLE -> REACHABLE_ICON
                PointOfInterestStatus.VISITED -> VISITED_ICON
                PointOfInterestStatus.VISIBLE -> VISIBLE_ICON
                else -> DEFAULT_ICON
            }
        }
    }

    /**
     * Function to be called when received a new map with poi items and poi status
     */
    fun renderClusterItems(poiStatusMap: Map<MarkerItem, PointOfInterestStatus>) {
        clusterManager.clearItems()

        this.poiStatusMap += poiStatusMap

        clusterManager.addItems(this.poiStatusMap.keys)

        clusterManager.cluster()
    }

    /**
     * Function to be called when there is a change in the POI being analyzed by Meili Lens
     * so that its colour updates to one different than the rest
     */
    fun renderMeiliLensPoi(poi: MarkerItem?) {
        if (poi?.poi != meiliLensPoi?.poi) { // Update only if meili lens poi has changed

            // Update value of Meili lens poi
            val prevMeiliLensPoi = meiliLensPoi
            meiliLensPoi = poi


            updateStatusOfPoi(prevMeiliLensPoi)
            updateStatusOfPoi(meiliLensPoi)
        }
    }

    /**
     * This function removes the poi passed as parameter and reinserts it again in the clusterer
     * so that its ui is updated with the new value in case there has been a change
     */
    private fun updateStatusOfPoi(poi: MarkerItem?) {
        if (poi != null) {
            clusterManager.updateItem(poi)
            clusterManager.cluster()
        }
    }

    companion object {
        val REACHABLE_ICON: BitmapDescriptor =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        val VISITED_ICON: BitmapDescriptor =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
        val VISIBLE_ICON: BitmapDescriptor =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        val DEFAULT_ICON: BitmapDescriptor =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        val MEILI_LENS_ICON: BitmapDescriptor =
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
    }
}