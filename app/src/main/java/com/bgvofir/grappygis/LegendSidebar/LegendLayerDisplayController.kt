package com.bgvofir.grappygis.LegendSidebar

import com.google.firebase.storage.FirebaseStorage

object LegendLayerDisplayController{
    val storage = FirebaseStorage.getInstance()

    fun fetchMMap(projectID: String){
        val storageReference = storage.reference
        val storageRef = storageReference.child("settlements/$projectID/layers/points.json")

    }
}