package com.bgvofir.grappygis.LayerCalloutControl

import android.util.Log
import com.bgvofir.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.bgvofir.grappygis.ClientFeatureLayers.GrappiField
import com.bgvofir.grappygis.SketchController.SketchEditorController
import com.bgvofir.grappygis.SketchController.SketcherEditorTypes
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ExecutionException
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.Geometry
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


object FeatureLayerController {
    var point: android.graphics.Point? = null
    var tolerance = 10.0
    var isUserLayer = false
    var layerId = ""
    var shapeType = SketcherEditorTypes.POLYLINE
    var clientFeatureCollection: ClientFeatureCollectionLayer? = null
    var collection2: ClientFeatureCollectionLayer? = null

    fun layerClicked(point: android.graphics.Point, mMap: MapView, onLayerClickListener: OnLayerClickListener){
        this.point = point
        identifyClickedLayerResults(point,mMap) { res ->

            if (res.size > 0) {
                var layerNames = ArrayList<String>()
                res.forEach {
                    val mLayerName = it.layerContent.name
                    layerNames.add(mLayerName)
                }
                onLayerClickListener.onLayerClickListener(layerNames, res)
            }
        }
    }

    fun setColor(){
        clientFeatureCollection?.let{
            it.setColor()
        }
    }
    private fun convertAttributeStringToMap(forString: ArrayList<String>): MutableList<Map<String, String>>{
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        var resultMapList = mutableListOf<Map<String, String>>()
        if (forString.size > 0){
            forString.forEach {
                val trimmed = it.trim()
                val mMap = Gson().fromJson<Map<String, String>>(trimmed, mapType)
                resultMapList.add(mMap)
            }
        }
        return resultMapList
    }

    fun layerDetails(forLayer: IdentifyLayerResult): ArrayList<Map<String, String>>{
        val resultGeoElements = forLayer.elements
        layerId = ""
        isUserLayer = false
        if (forLayer.layerContent.name == "Feature Collection"){
            //doesn't work here :(
            return featureCollectionDetails(forLayer)
        }
        if (forLayer.layerContent.name.contains("\$\$##")){
            isUserLayer = true
            return parseFeatureCollection(forLayer)
        }
        isUserLayer = false
        var mAttributesString = ArrayList<String>()
        var layersAttributeList = ArrayList<Map<String, String>>()
        var mAliasesMap = mutableMapOf<String, String>() // <name, alias>

        if (!resultGeoElements.isEmpty()){
            resultGeoElements.forEach {
                if (it is ArcGISFeature){
                    val mArcGISFeature = it as? ArcGISFeature
                    mAttributesString.add(mArcGISFeature?.attributes.toString())
                    var mTempMap = mutableMapOf<String, String>()
                    mArcGISFeature?.featureTable?.fields?.forEach {
                        if (!it.alias.isEmpty()){
                            mAliasesMap[it.name] = it.alias
                        }
                    }
                    mArcGISFeature?.attributes?.forEach {
                        if (!it.value.toString().isEmpty() && !it.key.toString().isEmpty()
                                && !it.key.toString().contains(".FID")){
                            val alias = mAliasesMap[it.key]
                            val attribute = it
                            if (alias == null){
                                mTempMap[it.key] = attribute.value.toString()
                            }
                            alias?.let{
                                mTempMap[alias] = attribute.value.toString()
                            }


                        }

                    }

                    layersAttributeList.add(mTempMap)
                }
            }

        }

        return layersAttributeList
    }
    private fun parseFeatureCollection(forLayer: IdentifyLayerResult): ArrayList<Map<String, String>>{
        var resultList = ArrayList<Map<String, String>>()

        forLayer.sublayerResults.forEach {

            var mTempMap = mutableMapOf<String, String>()
            it.elements.forEach {
                it.attributes.forEach {
                    mTempMap[it.key] = it.value.toString()
                }
                mTempMap["Id"]?.let { id->
                    layerId = id
                }

            }

            resultList.add(mTempMap)
        }
        val set = HashSet<Map<String, String>>(resultList)
        resultList.clear()
        resultList.addAll(set)

        return resultList
    }

    private fun featureCollectionDetails(forLayer: IdentifyLayerResult): ArrayList<Map<String, String>>{
        var resultList = ArrayList<Map<String, String>>()
        forLayer.sublayerResults.forEach {
            var mTempMap = mutableMapOf<String, String>()
            it.elements.forEach {
                it.attributes.forEach {
                    if (it.key.contains("Description")){
                        mTempMap["תאור"] = it.value.toString()
                    }
                    if (it.key.contains("Category")){
                        mTempMap["קטגוריה"] = it.value.toString()
                    }
                    if (it.key.contains("URL") && it.value != null){
                        mTempMap["תצוגה מקדימה"] = it.value.toString()
                    }
                }
            }
            resultList.add(mTempMap)
        }
        val set = HashSet<Map<String, String>>(resultList)
        resultList.clear()
        resultList.addAll(set)
        return resultList
    }

    fun identifyAttForPointDeletion(forLayers: List<IdentifyLayerResult>): Int{
        forLayers.forEach {
            if (it.layerContent.name.toLowerCase() == "feature collection") {
                it.sublayerResults[0].elements.forEach {
                    it.attributes.forEach{
                        if (it.key.contains("CustomPointHash")){
                            return it.value as Int
                        }
                    }
                }
            }
        }
        return 0
    }

    fun featureCollectionHandle(forPoint : android.graphics.Point, forMap: MapView, identifiedLayer: IdentifyLayerResult){
        val envelope = Envelope(forPoint.x - tolerance, forPoint.y - tolerance,
                forPoint.x + tolerance, forPoint.y + tolerance, forMap.spatialReference)
        var query = QueryParameters()
        query.geometry = envelope
        val collection = identifiedLayer.layerContent as FeatureCollectionLayer
        val future = collection.layers[0].featureTable.queryFeaturesAsync(query)
        future.addDoneListener{
            val result = future.get()
            var mString = mutableListOf<String>()
            result.iterator().forEach {
                mString.add(it.attributes.toString())
            }
            print(mString)
        }
    }
    /** identifies which layers were clicked
     *
     *
     */
    fun identifyClickedLayerResults(point: android.graphics.Point, mMap: MapView, callback: (MutableList<IdentifyLayerResult>) -> Unit){
        val TAG = "ClickedLayerResults"
        var identifyLayerResult: MutableList<IdentifyLayerResult>
        val identifyLayerResultsFuture = mMap
                .identifyLayersAsync(point, tolerance, false, 5)
        identifyLayerResultsFuture.addDoneListener {
            try {
                identifyLayerResult = identifyLayerResultsFuture.get()
                callback(identifyLayerResult)

            } catch (e: InterruptedException) {
                Log.e(TAG, "Error identifying results: " + e.message)
            } catch (e: ExecutionException) {
                Log.e(TAG, "Error identifying results: " + e.message)
            }
        }
    }

    interface OnFeatureCollectionListener{
        fun onFeatureCollectionListener()
    }
    interface OnLayerClickListener{
        fun onLayerClickListener(layerNames: ArrayList<String>, identifiedLayers: MutableList<IdentifyLayerResult>)
    }

    fun addNewGeometry(geometry: Geometry, mMap: MapView, category: String){
        if (collection2 == null) {
            val id = UUID.randomUUID().toString()
            val fields = mutableListOf<GrappiField>()
            fields.add(GrappiField("category", "esriFieldTypeString", "סיווג", 255))
            collection2 = ClientFeatureCollectionLayer("סתם קו לבן", id, fields, mMap.spatialReference)
            mMap.map.operationalLayers.add(collection2!!.layer)
        }
        var attributes = hashMapOf<String, Any>()
        attributes.put("category", category)
        collection2!!.createFeature(attributes, geometry)
        SketchEditorController.clean(mMap)
        collection2!!.generateARCGISJSON()
    }
    fun addGeometryToMap(geometry: Geometry, mMap: MapView, name: String, sketcherEditorTypes: SketcherEditorTypes, category:String){

        if (clientFeatureCollection == null) {
            val id = UUID.randomUUID().toString()
            val fields = mutableListOf<GrappiField>()
            fields.add(GrappiField("category", "esriFieldTypeString", "סיווג", 255))
            clientFeatureCollection = ClientFeatureCollectionLayer("קו ירוק מגניב", id, fields, mMap.spatialReference)
            mMap.map.operationalLayers.add(clientFeatureCollection!!.layer)
        }
        var attributes = hashMapOf<String, Any>()
        attributes.put("category", category)






//        if (localPolylineFeatureCollectionLayer == null || localPolylineFeaturecollection == null){
//            localPolylineFeaturecollection = FeatureCollection()
//            localPolylineFeatureCollectionLayer = FeatureCollectionLayer(localPolylineFeaturecollection)
//            localPolylineFeatureCollectionLayer?.name = "$name\$\$##"
//            mMap.map.operationalLayers.add(localPolylineFeatureCollectionLayer!!)
//        }
//        var fieldsArray = mutableListOf<Field>()
//        fieldsArray.add(Field.createString("category","סיווג",50))
//        var geometryType = GeometryType.POLYLINE
//        when (sketcherEditorTypes){
//            SketcherEditorTypes.POLYLINE -> geometryType = GeometryType.POLYLINE
//            SketcherEditorTypes.POLYGON ->  geometryType = GeometryType.POLYGON
//        }
//        var polylineTable = FeatureCollectionTable(fieldsArray, geometryType, mMap.spatialReference)
//        val lineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.WHITE, 4f)
//        val renderer = SimpleRenderer(lineSymbol)
//        polylineTable.renderer = renderer
//        localPolylineFeaturecollection!!.tables.add(polylineTable)
//        var attributes = hashMapOf<String, Any>()
//        attributes.put(fieldsArray[0].name, category)
//        val feature = polylineTable.createFeature(attributes,geometry) as Feature
//        polylineTable.addFeatureAsync(feature)



        clientFeatureCollection!!.createFeature(attributes, geometry)
        SketchEditorController.clean(mMap)
        //clientFeatureCollection!!.uploadJSON()
    }

}
//                (identifyLayerResult.get(0).layerContent as FeatureCollectionLayer).layers.get(0).featureTable
