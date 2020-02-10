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
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
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
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
                    shapeTypeSketcherSaveTV.setTextColor(context.resources.getColor(R.color.light_blue))
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

                }
            }
            return
        }
        when (SketchEditorController.sketcherEditorTypes){
            SketcherEditorTypes.POINT -> {
                shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.points_layer)
                shapeTypeSketcherSaveTV.setTextColor(context.resources.getColor(R.color.light_blue))
                val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_star_blue)
                shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)
            }
            SketcherEditorTypes.POLYLINE -> {}
            SketcherEditorTypes.POLYGON -> {}
        }
        shapeTypeSketcherSaveTV.text
    }

    fun setGeometry(){
        val type = SketchEditorController.sketcherEditorTypes
        val category = addCategoryToSketcherSaveET.text.toString()
        val number = addNumberToSketcherSaveET.text.toString()
        val description = addDescriptionSketcherSaveET.text.toString()
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
                SketcherEditorTypes.POLYGON -> { }
            }

            dismiss()
            return
        }
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
                ClientPhotoController.showPhotoQuestionDialog(context,attributes,geometry,callback!!, progressDialog!!)
            }
            SketcherEditorTypes.POLYGON -> {

            }
            SketcherEditorTypes.POINT -> {
                if (UserPoints.userPoints == null){
                    UserPoints.userPoints = ClientPointFeatureCollection(context, context.resources.getString(R.string.my_points),UUID.randomUUID().toString(),
                            UserPoints.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPoints.userPoints!!.layer)
                    layerListener?.successListener()
                }
                SketchEditorController.startSketching(SketcherEditorTypes.POINT, mMapView)
                ClientPhotoController.showPhotoQuestionDialog(context,attributes,geometry,callback!!, progressDialog!!)
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