package com.bgvofir.grappygis.LegendSidebar

import android.animation.Animator
import android.animation.ObjectAnimator
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.bgvofir.grappygis.LegendSidebar.LegendLayerDisplayController.groupNames
import com.bgvofir.grappygis.LegendSidebar.LegendLayerDisplayController.legendTitles
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

    fun makeLayersInvisible(mMap: MapView){
        val layers = mMap.map.operationalLayers
        layers.forEach {
            it.isVisible = false
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

    fun animateOpen(layout: RecyclerView){
        val width = layout.width.toFloat()
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

    fun animateClose(layout: RecyclerView){
        val width = layout.width.toFloat()
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
        legendGroupMap["אחר"] = mutableListOf()
        groupNames.forEach {
            legendGroupMap[it] = mutableListOf()
        }

        Log.d(TAG, legendTitles.toString())
        layers.forEach {
            val layerName = it.name
            if (legendTitles.containsKey(layerName)){
                val layerGroupName = legendTitles[layerName]
                legendGroupMap[layerGroupName]?.add(it)
            }
            else if (layerName.trim().isNotEmpty()){
                legendGroupMap["אחר"]?.add(it)
            }
        }
        var legendGroupList = mutableListOf<LegendGroup>()
        legendGroupMap.forEach{
            legendGroupList.add(LegendGroup(it.key, it.value))
        }
        legendGroupList.reverse()
        legendGroupList.forEach {
            it.layers = it.layers.reversed()
        }

        return legendGroupList
    }

    fun openSubArrowEffect(view: ImageView){
        ObjectAnimator.ofFloat(view, View.ROTATION, 0.0f, -90.0f).apply {
            duration=300
            start()
        }.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                ObjectAnimator.ofFloat(view, View.ROTATION, 0.0f, 0.0f).start()
                view.setImageResource(com.bgvofir.grappygis.R.drawable.ic_full_black_legend_arrow_down)
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
                view.setImageResource(com.bgvofir.grappygis.R.drawable.ic_hollow_black_legend_arrow_close)
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
class LayerTitle(title: String){
    var title = title
}

class OperationalLayer(title: String, layerType: String, layers: Array<LayerTitle>?){

    var layers = layers
    var title = title
    var layerType = layerType
}