package com.bgvofir.grappygis.LegendSidebar

import android.util.Log
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.File
import java.io.FileInputStream

object LegendLayerDisplayController{

    val TAG = "LayerDisplay"
    val storage = FirebaseStorage.getInstance()
    var localFile = File.createTempFile("mmap", "json")
    var legendTitles = mutableMapOf<String, String>()
    var groupNames = mutableListOf<String>()

    fun fetchMMap(projectID: String, layerListener: LayerGroupsListener){
        val storageReference = storage.reference
        val storageRef = storageReference.child("settlements/$projectID/mmap/data.json")
        storageRef.getFile(localFile).addOnSuccessListener {
            val json = generateJson()
            parseJson(json)
            layerListener.successListener()
        }.addOnFailureListener{
            Log.d(TAG,"failed to download file")

        }
    }

    private fun generateJson(): String{
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

    private fun parseJson(json: String){
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
                groupNames.add(groupTitle)
                it.layers?.forEach {
                    legendTitles[it.title] = groupTitle
                }
            }
        }
    }

    fun generateLegendGroupList(map: MapView): List<LegendGroup>{
        val layers = map.map.operationalLayers
        var legendGroupMap = mutableMapOf<String, MutableList<Layer>>()
        groupNames.forEach {
            legendGroupMap[it] = mutableListOf()
        }
        legendGroupMap["אחר"] = mutableListOf()
        layers.forEach {
            val layerName = it.name
            if (legendTitles.containsKey(layerName)){
                val layerGroupName = legendTitles[layerName]
                legendGroupMap[layerGroupName]?.add(it)
            } else {
                legendGroupMap["אחר"]?.add(it)
            }
        }
        var legendGroupList = mutableListOf<LegendGroup>()
        legendGroupMap.forEach{
            legendGroupList.add(LegendGroup(it.key, it.value))
        }
        return legendGroupList
    }

    interface LayerGroupsListener{
        fun successListener()
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