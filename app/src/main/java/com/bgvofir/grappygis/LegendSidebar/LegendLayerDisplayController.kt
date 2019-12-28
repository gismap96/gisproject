package com.bgvofir.grappygis.LegendSidebar

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.File
import java.io.FileInputStream

object LegendLayerDisplayController{

    val storage = FirebaseStorage.getInstance()
    var localFile = File.createTempFile("mmap", "json")
    //key = layer name
    //value = group name
    var legendTitles = mutableMapOf<String, String>()

    fun fetchMMap(projectID: String){
        val storageReference = storage.reference
        val storageRef = storageReference.child("settlements/$projectID/mmap/data.json")
        storageRef.getFile(localFile).addOnSuccessListener {
            print("we showed zift! horray")
            val json = generateJson()
            parseJson(json)
        }.addOnFailureListener{
            print("failed to download file")

        }
    }

    fun generateJson(): String{
        val length = localFile.length().toInt()

        val bytes = ByteArray(length)

        val `in` = FileInputStream(localFile)
        try {
            `in`.read(bytes)
        } finally {
            `in`.close()
        }

        return String(bytes)
    }

    fun parseJson(json: String){
        legendTitles.clear()
        var parser = JsonParser()
        var element = parser.parse(json)
        val mapElement = element.asJsonObject.get("map")
        val operationalLayersElement = mapElement.asJsonObject.get("operationalLayers")
        val gson = Gson()
        val operationalLayers = gson.fromJson(operationalLayersElement.asJsonArray, Array<OperationalLayer>::class.java).toList()
        operationalLayers.forEach {
            if (it.layerType == "GroupLayer"){
                val groupTitle = it.title
                it.layers?.forEach {
                    legendTitles[it.title] = groupTitle
                }
            }
        }
    }
}
class LayerTitle(title: String){
    var title = title
}

class OperationalLayer(title: String, layerType: String, layers: Array<LayerTitle>?){

    var layers = layers
    var title = title
    var layerType = layerType
}