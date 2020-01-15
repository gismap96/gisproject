package com.bgvofir.grappygis.ProjectRelated

import com.bgvofir.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.bgvofir.grappygis.ClientFeatureLayers.GrappiField

object UserPolyline{
    var userPolyline: ClientFeatureCollectionLayer? = null
    var grappiFields = mutableListOf<GrappiField>()

    fun initFields(){
        grappiFields.clear()
        grappiFields.add(GrappiField("category", "esriFieldTypeString", "סיווג", 255))
    }
}