package com.grappiapp.grappygis.SketchController

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.grappiapp.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPointFeatureCollection
import com.grappiapp.grappygis.ClientLayerPhotoController.ClientPhotoController
import com.grappiapp.grappygis.LayerCalloutControl.FeatureLayerController
import com.grappiapp.grappygis.LegendSidebar.LegendLayerDisplayController
import com.grappiapp.grappygis.ProjectRelated.MapProperties
import com.grappiapp.grappygis.ProjectRelated.UserPoints
import com.grappiapp.grappygis.ProjectRelated.UserPolyline
import com.grappiapp.grappygis.R
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPolygonFeatureCollection
import com.grappiapp.grappygis.GeoViewController.GeoViewController
import com.grappiapp.grappygis.OfflineMode.OfflineModeController
import com.grappiapp.grappygis.ProjectRelated.UserPolygon
import kotlinx.android.synthetic.main.fragment_dialog_sketcher_save_input.*
import java.util.*

open class SketcherSaveDialogFragment(val context: Activity, val mMapView: MapView,
                                      val callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish?, val layerListener: LegendLayerDisplayController.LayerGroupsListener?, val progressDialog: ProgressDialog?, val isEditMode: Boolean): Dialog(context), View.OnClickListener {

    val shapeType = FeatureLayerController.shapeType
    val TAG = "sketcherSave"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_sketcher_save_input)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        closeSketcherSaveTV.setOnClickListener(this)
        cancelSketcherSaveTV.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (isEditMode){
            val layerID = FeatureLayerController.layerId
            when (shapeType){
                SketcherEditorTypes.POINT -> {
                    shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.points_layer)
                    shapeTypeSketcherSaveTV.setTextColor(ContextCompat.getColor(context, R.color.light_blue))
                    val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_star_blue)
                    shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)
                    val featureNum = UserPoints.userPoints!!.identifyFeatureById(layerID)
                    if (featureNum >= 0){
                        val feature = UserPoints.userPoints!!.features[featureNum]
                        val featureAttributes = feature.attributes
                        addDescriptionSketcherSaveET.setText(featureAttributes["description"].toString())
                        addCategoryToSketcherSaveET.setText(featureAttributes["category"].toString())
                        addNumberToSketcherSaveET.setText(featureAttributes["number"].toString())
                        var checked = false
                        if (featureAttributes["isUpdated"] == "yes"){
                            checked = true
                        }
                        updateToSystemSketchSaveSW.isChecked = checked
                    }
                }
                SketcherEditorTypes.POLYLINE -> {
//                    shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.polyline_layer)
//                    shapeTypeSketcherSaveTV.setTextColor(context.resources.getColor(R.color.soft_red))
//                    val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_polyline_soft_red)
//                    shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)
                    val featureNum = UserPolyline.userPolyline!!.identifyFeatureById(layerID)
                    if (featureNum >= 0){
                        val feature = UserPolyline.userPolyline!!.features[featureNum]
                        val featureAttributes = feature.attributes
                        addDescriptionSketcherSaveET.setText(featureAttributes["description"].toString())
                        addCategoryToSketcherSaveET.setText(featureAttributes["category"].toString())
                        addNumberToSketcherSaveET.setText(featureAttributes["number"].toString())
                        var checked = false
                        if (featureAttributes["isUpdated"] == "yes"){
                            checked = true
                        }
                        updateToSystemSketchSaveSW.isChecked = checked
                    }
                }
                SketcherEditorTypes.POLYGON -> {
                    shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.polygon_layer)
                    shapeTypeSketcherSaveTV.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                    val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_polygon_area_measurement)
                    shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)
                    val featureNum = UserPolygon.userPolygon!!.identifyFeatureById(layerID)
                    if (featureNum >= 0 ){
                        val feature = UserPolygon.userPolygon!!.features[featureNum]
                        val featureAttributes = feature.attributes
                        addDescriptionSketcherSaveET.setText(featureAttributes["description"].toString())
                        addCategoryToSketcherSaveET.setText(featureAttributes["category"].toString())
                        addNumberToSketcherSaveET.setText(featureAttributes["number"].toString())
                        var checked = false
                        if (featureAttributes["isUpdated"] == "yes"){
                            checked = true
                        }
                        updateToSystemSketchSaveSW.isChecked = checked
                    }
                }
            }
            return
        }
        when (SketchEditorController.sketcherEditorTypes){
            SketcherEditorTypes.POINT -> {
                shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.points_layer)
                shapeTypeSketcherSaveTV.setTextColor(ContextCompat.getColor(context, R.color.light_blue))
                val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_star_blue)
                shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)
            }
            SketcherEditorTypes.POLYLINE -> {}
            SketcherEditorTypes.POLYGON -> {
                shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.polygon_layer)
                shapeTypeSketcherSaveTV.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_polygon_area_measurement)
                shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)
            }
            SketcherEditorTypes.MULTIPOINTS,
            SketcherEditorTypes.HYDRANTS -> {
                shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.multipoint_layer)
                shapeTypeSketcherSaveTV.setTextColor(ContextCompat.getColor(context, R.color.yellow))
                val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_setting)
                shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)
            }
        }
        shapeTypeSketcherSaveTV.text
    }

    fun removeUnsupportedCharacters(string: String): String{
        return string.replace("[\\\\\\\\/:*?\\\"<>|]", "")
    }
    fun setGeometry(){
        val type = SketchEditorController.sketcherEditorTypes
        val category = removeUnsupportedCharacters(addCategoryToSketcherSaveET.text.toString())
        val number = removeUnsupportedCharacters(addNumberToSketcherSaveET.text.toString())
        var description = removeUnsupportedCharacters(addDescriptionSketcherSaveET.text.toString())
        var isUpdated = "no"
        if (updateToSystemSketchSaveSW.isChecked) isUpdated = "yes"
        val attributes = hashMapOf<String, Any>()
        attributes.put("category", category)
        attributes.put("description", description)
        attributes.put("number", number)
        attributes.put("isUpdated", isUpdated)
        attributes.put("imageURL", "")
        if (isEditMode){
            val layerId = FeatureLayerController.layerId
            val type = FeatureLayerController.shapeType
            when (type){
                SketcherEditorTypes.POINT -> {
                    UserPoints.userPoints!!.editFeatureAttributes(context, layerId, attributes)
                }
                SketcherEditorTypes.POLYLINE -> {
                    UserPolyline.userPolyline!!.editFeatureAttributes(context, layerId, attributes)
                }
                SketcherEditorTypes.POLYGON -> {
                    UserPolygon.userPolygon!!.editFeatureAttributes(context, layerId, attributes)
                }
            }

            dismiss()
            return
        }
        if (OfflineModeController.isOfflineMode){
            val geometry = SketchEditorController.getGeometry()
            geometry?.let{
                when (type){
                    SketcherEditorTypes.POINT -> {
                        if (UserPoints.userPoints == null){
                            UserPoints.userPoints = ClientPointFeatureCollection(context, context.resources.getString(R.string.my_points),UUID.randomUUID().toString(),
                                    UserPoints.grappiFields, MapProperties.spatialReference!!)
                            mMapView.map.operationalLayers.add(UserPoints.userPoints!!.layer)
                            layerListener?.successListener()
                        }
                        SketchEditorController.startSketching(SketcherEditorTypes.POINT, mMapView)
                        UserPoints.userPoints!!.layer.isVisible = true
                        UserPoints.userPoints!!.createFeature(attributes,geometry){
                            OfflineModeController.saveJSONLocally(context, UserPoints.userPoints!!.generateARCGISJSON(), type)
                        }
                    }
                    SketcherEditorTypes.POLYLINE ->{
                        if (UserPolyline.userPolyline == null){
                            UserPolyline.userPolyline = ClientFeatureCollectionLayer(context.resources.getString(R.string.my_polyline), UUID.randomUUID().toString(),
                                    UserPolyline.grappiFields, MapProperties.spatialReference!!)
                            mMapView.map.operationalLayers.add(UserPolyline.userPolyline!!.layer)
                            layerListener?.successListener()
                        } else if (UserPolyline.userPolyline!!.features.size == 0){
                            UserPolyline.userPolyline = ClientFeatureCollectionLayer(context.resources.getString(R.string.my_polyline), UUID.randomUUID().toString(),
                                    UserPolyline.grappiFields, MapProperties.spatialReference!!)
                            mMapView.map.operationalLayers.add(UserPolyline.userPolyline!!.layer)
                            layerListener?.successListener()
                        }
                        SketchEditorController.startSketching(SketcherEditorTypes.POLYLINE, mMapView)
                        UserPolyline.userPolyline!!.layer.isVisible = true
                        UserPolyline.userPolyline!!.createFeature(attributes, geometry)
                        OfflineModeController.saveJSONLocally(context, UserPolyline.userPolyline!!.generateARCGISJSON(), type)
                    }
                    SketcherEditorTypes.POLYGON -> {
                        if (UserPolygon.userPolygon == null){
                            UserPolygon.userPolygon = ClientPolygonFeatureCollection(context, context.resources.getString(R.string.my_polyline),UUID.randomUUID().toString(),
                                    UserPolygon.grappiFields, MapProperties.spatialReference!!)
                            mMapView.map.operationalLayers.add(UserPolygon.userPolygon!!.layer)
                            layerListener?.successListener()
                        }
                        SketchEditorController.startSketching(SketcherEditorTypes.POLYGON, mMapView)
                        UserPolygon.userPolygon!!.layer.isVisible = true
                        UserPolygon.userPolygon!!.createFeature(attributes, geometry){
                            OfflineModeController.saveJSONLocally(context, UserPolygon.userPolygon!!.generateARCGISJSON(), type)
                        }
                    }
                    SketcherEditorTypes.HYDRANTS, SketcherEditorTypes.MULTIPOINTS -> {
                        if (UserPoints.userPoints == null){
                            UserPoints.userPoints = ClientPointFeatureCollection(context, context.resources.getString(R.string.my_points),UUID.randomUUID().toString(),
                                    UserPoints.grappiFields, MapProperties.spatialReference!!)
                            mMapView.map.operationalLayers.add(UserPoints.userPoints!!.layer)
                            layerListener?.successListener()
                        }
                        SketchEditorController.startSketching(SketcherEditorTypes.POINT, mMapView)
                        UserPoints.userPoints!!.layer.isVisible = true
                        UserPoints.userPoints!!.addFeatureFromMultipoints(geometry, attributes){
                            OfflineModeController.saveJSONLocally(context, UserPoints.userPoints!!.generateARCGISJSON(), type)
                        }
                    }
                }
            }
            dismiss()
            return
        }
//        GeoViewController.setSavedViewPoint(mMapView)

        val geometry = SketchEditorController.getGeometry()
        if (geometry == null){
            val toast = Toast.makeText(context, "צורה ריקה", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()
            dismiss()
            return
        }

        when (type){
            SketcherEditorTypes.POLYLINE -> {
                if (UserPolyline.userPolyline == null){
                    UserPolyline.userPolyline = ClientFeatureCollectionLayer(context.resources.getString(R.string.my_polyline), UUID.randomUUID().toString(),
                            UserPolyline.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPolyline.userPolyline!!.layer)
                    layerListener?.successListener()
                } else if (UserPolyline.userPolyline!!.features.size == 0){
                    UserPolyline.userPolyline = ClientFeatureCollectionLayer(context.resources.getString(R.string.my_polyline), UUID.randomUUID().toString(),
                            UserPolyline.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPolyline.userPolyline!!.layer)
                    layerListener?.successListener()
                }
                SketchEditorController.startSketching(SketcherEditorTypes.POLYLINE, mMapView)
                UserPolyline.userPolyline!!.layer.isVisible = true
//                ClientPhotoController.showPhotoQuestionDialog(context,attributes,geometry,callback!!, progressDialog!!)
                ClientPhotoController.openBottomSheet(context,attributes,geometry,callback!!, progressDialog!!)
            }
            SketcherEditorTypes.POLYGON -> {
                if (UserPolygon.userPolygon == null){
                    UserPolygon.userPolygon = ClientPolygonFeatureCollection(context, context.resources.getString(R.string.my_polyline),UUID.randomUUID().toString(),
                            UserPolygon.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPolygon.userPolygon!!.layer)
                    layerListener?.successListener()
                }
                SketchEditorController.startSketching(SketcherEditorTypes.POLYGON, mMapView)
                UserPolygon.userPolygon!!.layer.isVisible = true
//                ClientPhotoController.showPhotoQuestionDialog(context, attributes, geometry, callback!!, progressDialog!!)
                ClientPhotoController.openBottomSheet(context,attributes,geometry,callback!!, progressDialog!!)
            }
            SketcherEditorTypes.POINT -> {
                if (UserPoints.userPoints == null){
                    UserPoints.userPoints = ClientPointFeatureCollection(context, context.resources.getString(R.string.my_points),UUID.randomUUID().toString(),
                            UserPoints.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPoints.userPoints!!.layer)
                    layerListener?.successListener()
                }
                SketchEditorController.startSketching(SketcherEditorTypes.POINT, mMapView)
                UserPoints.userPoints!!.layer.isVisible = true
//                ClientPhotoController.showPhotoQuestionDialog(context,attributes,geometry,callback!!, progressDialog!!)
                ClientPhotoController.openBottomSheet(context,attributes,geometry,callback!!, progressDialog!!)
            }
            SketcherEditorTypes.MULTIPOINTS, SketcherEditorTypes.HYDRANTS -> {
                if (UserPoints.userPoints == null){
                    UserPoints.userPoints = ClientPointFeatureCollection(context, context.resources.getString(R.string.my_points),UUID.randomUUID().toString(),
                            UserPoints.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPoints.userPoints!!.layer)
                    layerListener?.successListener()
                }
                UserPoints.userPoints!!.layer.isVisible = true
//                ClientPhotoController.showPhotoQuestionDialog(context,attributes,geometry,callback!!, progressDialog!!)
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle(context.getString(R.string.updating_layer))
                progressDialog.setCancelable(false)
                progressDialog.show()
                UserPoints.userPoints!!.addFeatureFromMultipoints(geometry!!,attributes){
                    UserPoints.userPoints!!.uploadJSON(object: ClientPointFeatureCollection.OnPointsUploaded{
                        override fun onPointsUploadFinished() {
                            SketchEditorController.clean()
                            progressDialog.dismiss()
                            dismiss()
                        }
                    })
                }
            }
        }
        dismiss()
    }

    fun validate(): Boolean{
        if (addDescriptionSketcherSaveET.text.toString().trim().isEmpty() || addDescriptionSketcherSaveET.text == null){
            addDescriptionSketcherSaveET.error = context.getString(R.string.field_mandatory)
            return false
        }
        if (addNumberToSketcherSaveET.text.toString().trim().isEmpty() || addNumberToSketcherSaveET.text == null){
//            addNumberToSketcherSaveET.error = context.getString(R.string.field_mandatory)
            addNumberToSketcherSaveET.setText("0")
        }
        if (addCategoryToSketcherSaveET.text.toString().trim().isEmpty() || addCategoryToSketcherSaveET.text == null){
            addCategoryToSketcherSaveET.error = context.getString(R.string.field_mandatory)
            return false
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.closeSketcherSaveTV ->{
                if (validate()){
                    setGeometry()
                }

            }
            R.id.cancelSketcherSaveTV->{
                dismiss()
            }
        }
    }
    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}