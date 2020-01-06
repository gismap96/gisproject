package com.bgvofir.grappygis.FormatGeometry

import android.net.Uri
import android.util.Log
import com.bgvofir.grappygis.LegendSidebar.LegendLayerDisplayController.localFile
import com.esri.arcgisruntime.data.FeatureCollection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

object FormatJSONCollectionFeature {
    val TAG = "formatJSONCollection"
    val firebaseStorage = FirebaseStorage.getInstance()

    fun pointToJson(feature: FeatureCollection, mProjectId: String){
        val json = feature.toJson().toByteArray()
        val storageRef = firebaseStorage.reference
        val username = FirebaseAuth.getInstance().uid
        val childRef = storageRef.child("settlements/$mProjectId/JSON/$username.json")

        childRef.putBytes(json).addOnSuccessListener {
            Log.d(TAG, "file sent")
        }

    }
}