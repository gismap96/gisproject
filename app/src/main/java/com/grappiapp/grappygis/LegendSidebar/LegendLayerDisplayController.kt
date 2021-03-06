package com.grappiapp.grappygis.LegendSidebar

import android.animation.Animator
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.esri.arcgisruntime.layers.GroupLayer
import com.grappiapp.grappygis.R
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.grappiapp.grappygis.Utils
import java.io.File
import java.io.FileInputStream

object LegendLayerDisplayController{

    val TAG = "LayerDisplay"
    val storage = FirebaseStorage.getInstance()
    var localFile = File.createTempFile("mmap", "json")
    var legendIds = mutableMapOf<String, String>()
    var groupNames = mutableListOf<String>()
    var legendGroups = mutableListOf<LegendGroup>()



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

    fun makeLayersInvisible(mMap: MapView){
        val layers = mMap.map.operationalLayers
        layers.forEach {
            if (it is GroupLayer){
                it.layers.forEach { layer->
                    layer.isVisible = false
                }
            } else if (!it.name.contains(".jpg") && !it.name.contains(".tif") && !it.name.contains(".ecw")){
                it.isVisible = false
            }
        }
    }
    fun showLayersFromUser(mMap: MapView){
        val layers = mMap.map.operationalLayers
        layers.forEach {
            if (it.name == "Feature Collection")
                it.isVisible = true
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

    fun animateOpen(layout: ConstraintLayout){
        var width = layout.width.toFloat()
        var direction = -1f
        if (Utils.isRTL(layout.context)){
            direction = 1f
        }
        width *= direction
        ObjectAnimator.ofFloat(layout,"translationX", width).apply {
            duration = 0
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                ObjectAnimator.ofFloat(layout, "translationX", 0f).apply {
                    duration = 500
                    start()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
    }

    fun animateClose(layout: ConstraintLayout){
        var width = layout.width.toFloat()
        var direction = -1f
        if (Utils.isRTL(layout.context)){
            direction = 1f
        }
        width *= direction
        ObjectAnimator.ofFloat(layout,"translationX", width).apply{
            duration = 500
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                layout.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
    }

    private fun parseJson(json: String){
        legendIds.clear()
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
                    legendIds[it.id] = groupTitle
                }
            }
        }
    }
    fun makeAllGroupLayersVisible(mMapView: MapView){
        val layers = mMapView.map.operationalLayers
        layers.forEach {
            if (it is GroupLayer){
                it.isVisible = true
            }
        }
    }

    fun generateLegendSidebar(map: MapView): List<LegendGroup>{
        var orthophotoName = map.context.getString(R.string.orthophoto)
        val otherName = map.context.getString(R.string.other)
        val myLayers = map.context.getString(R.string.my_layers)
        val layers = map.map.operationalLayers
        var legendGroupMap = mutableMapOf<String, MutableList<Layer>>()
        var legendGroupList = mutableListOf<LegendGroup>()
        legendGroupMap[myLayers] = mutableListOf()
        legendGroupMap[otherName] = mutableListOf()
        legendGroupMap[orthophotoName] = mutableListOf()
        layers.forEach {
            if (it is GroupLayer){
                legendGroupMap[it.name] = it.layers
            } else {
                val layerName = it.name
                if (layerName.contains(".jpg") || layerName.contains(".tif") || layerName.contains(".ecw")){
                    legendGroupMap[orthophotoName]?.add(it)
                } else if(layerName.contains("$$##")){
                    legendGroupMap[myLayers]?.add(it)
                } else if (layerName.trim().isNotEmpty()){
                    legendGroupMap[otherName]?.add(it)
                }
            }
        }
        legendGroupMap[otherName]?.let{
            if (it.count() == 0) legendGroupMap.remove(otherName)
        }
        legendGroupMap[myLayers]?.let{
            if (it.count() == 0) legendGroupMap.remove(myLayers)
        }
        legendGroupMap.forEach{
            legendGroupList.add(LegendGroup(it.key, it.value))
        }
        legendGroupList.reverse()
        legendGroupList.forEach {
            it.layers = it.layers.reversed()
        }
        this.legendGroups = legendGroupList
        return legendGroupList
    }
//    fun generateLegendGroupList(map: MapView): List<LegendGroup>{
//        return generateGroups(map)
//        var orthophotoName = map.context.getString(R.string.orthophoto)
//        val otherName = map.context.getString(R.string.other)
//        val myLayers = map.context.getString(R.string.my_layers)
//        val layers = map.map.operationalLayers
//        var legendGroupMap = mutableMapOf<String, MutableList<Layer>>()
//        legendGroupMap[myLayers] = mutableListOf()
//        legendGroupMap[otherName] = mutableListOf()
//        legendGroupMap[orthophotoName] = mutableListOf()
//        groupNames.forEach {
//            legendGroupMap[it] = mutableListOf()
//        }
//
//        Log.d(TAG, legendIds.toString())
//        layers.forEach {
//            var layerName = it.name
//            val layerid = it.id
//            if (layerName.contains(".jpg") || layerName.contains(".tif") || layerName.contains(".ecw")){
//                legendGroupMap[orthophotoName]?.add(it)
//            }
//            else if (legendIds.containsKey(layerid)){
//                val layerGroupName = legendIds[layerid]
//                legendGroupMap[layerGroupName]?.add(it)
//            } else if(layerName.contains("$$##")){
//                legendGroupMap[myLayers]?.add(it)
//            } else if (layerName.trim().isNotEmpty()){
//                legendGroupMap[otherName]?.add(it)
//            }
//        }
//        legendGroupMap[otherName]?.let{
//            if (it.count() == 0) legendGroupMap.remove(otherName)
//        }
//        legendGroupMap[myLayers]?.let{
//            if (it.count() == 0) legendGroupMap.remove(myLayers)
//        }
//        var legendGroupList = mutableListOf<LegendGroup>()
//        legendGroupMap.forEach{
//            legendGroupList.add(LegendGroup(it.key, it.value))
//        }
//        legendGroupList.reverse()
//        legendGroupList.forEach {
//            it.layers = it.layers.reversed()
//        }
//        this.legendGroups = legendGroupList
//        return legendGroupList
//    }

    fun openSubArrowEffect(view: ImageView){
        ObjectAnimator.ofFloat(view, View.ROTATION, 0.0f, -90.0f).apply {
            duration=300
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                ObjectAnimator.ofFloat(view, View.ROTATION, 0.0f, 0.0f).start()
                view.setImageResource(com.grappiapp.grappygis.R.drawable.ic_full_black_legend_arrow_down)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

    }
    fun closeSubArrowEffect(view: ImageView){
        ObjectAnimator.ofFloat(view, View.ROTATION, 0.0f, 90.0f).apply {
            duration=300
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                ObjectAnimator.ofFloat(view, View.ROTATION, 0.0f, 0.0f).start()
                view.setImageResource(com.grappiapp.grappygis.R.drawable.ic_hollow_black_legend_arrow_close)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })
    }

    interface LayerGroupsListener{
        fun successListener()
    }
}
class LayerID(id: String){
    var id = id
}

class OperationalLayer(title: String, layerType: String, layers: Array<LayerID>?, id: String){

    var layers = layers
    var title = title
    var layerType = layerType
    var id = id
}