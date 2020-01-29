package com.bgvofir.grappygis.SketchController

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.bgvofir.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.bgvofir.grappygis.ClientFeatureLayers.ClientPointFeatureCollection
import com.bgvofir.grappygis.ClientLayerPhotoController.ClientPhotoController
import com.bgvofir.grappygis.ClientPoint
import com.bgvofir.grappygis.LayerCalloutControl.FeatureLayerController
import com.bgvofir.grappygis.LegendSidebar.LegendLayerDisplayController
import com.bgvofir.grappygis.ProjectRelated.MapProperties
import com.bgvofir.grappygis.ProjectRelated.UserPoints
import com.bgvofir.grappygis.ProjectRelated.UserPolyline
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.mapping.view.MapView
import kotlinx.android.synthetic.main.description_dialog.*
import kotlinx.android.synthetic.main.fragment_dialog_sketcher_save_input.*
import java.util.*

class SketcherSaveDialogFragment(val context: Activity, mMapView: MapView,
                                 val callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish?, val layerListener: LegendLayerDisplayController.LayerGroupsListener?, val progressDialog: ProgressDialog?, val isEditMode: Boolean): Dialog(context), View.OnClickListener {

    val TAG = "sketcherSave"
    var mMapView = mMapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_sketcher_save_input)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        closeSketcherSaveTV.setOnClickListener(this)
        cancelSketcherSaveTV.setOnClickListener(this)

    }

    override fun onStart() {
        super.onStart()
        if (SketchEditorController.isEditMode){
            when (FeatureLayerController.shapeType){
                SketcherEditorTypes.POINT -> {
                    shapeTypeSketcherSaveTV.text = context.resources.getString(R.string.points_layer)
                    shapeTypeSketcherSaveTV.setTextColor(context.resources.getColor(R.color.light_blue))
                    val bitmap = getBitmapFromVectorDrawable(R.drawable.ic_star_blue)
                    shapeSymbolForSketcherSaveIV.setImageBitmap(bitmap)

                }
                SketcherEditorTypes.POLYLINE -> {}
                SketcherEditorTypes.POLYGON -> {}
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
        val number = addNumberToSketcherSaveET.text.toString().toDouble()
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
            UserPolyline.userPolyline!!.editFeatureAttributes(context, layerId, attributes)
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

                //UserPolyline.userPolyline!!.createFeature(attributes, geometry)
                SketchEditorController.clean(mMapView)
                ClientPhotoController.showPhotoQuestionDialog(context,attributes,geometry,callback!!, progressDialog!!)
                //UserPolyline.userPolyline!!.uploadJSON(callback)
            }
            SketcherEditorTypes.POLYGON -> {

            }
            SketcherEditorTypes.POINT -> {

                if (UserPoints.userPoints == null){
                    UserPoints.userPoints = ClientPointFeatureCollection(context, context.resources.getString(R.string.my_points),UUID.randomUUID().toString(),
                            UserPoints.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPoints.userPoints!!.layer)
                    val progressDialog = ProgressDialog(context)
                    progressDialog.setTitle(context.getString(R.string.updating_layer))
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    UserPoints.userPoints!!.createFeature(attributes, geometry){
                        if (UserPoints.userPoints!!.features.count() > 0){
                            mMapView.map.retryLoadAsync()
                            SketchEditorController.clean(mMapView)
                            UserPoints.userPoints!!.uploadJSON(object: ClientPointFeatureCollection.OnPointsUploaded{
                                override fun onPointsUploadFinished() {
                                    progressDialog.dismiss()
                                    layerListener?.successListener()
                                    Toast.makeText(context, context.resources.getString(R.string.point_saved),Toast.LENGTH_SHORT).show()
                                }

                            })

                        } else {
                            Log.d(TAG, "failed to load feature")
                        }
                    }
                    dismiss()
                    return
                }
                UserPoints.userPoints!!.createFeature(attributes,geometry, null)
                SketchEditorController.clean(mMapView)
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle(context.getString(R.string.updating_layer))
                progressDialog.setCancelable(false)
                progressDialog.show()
                UserPoints.userPoints!!.uploadJSON(object: ClientPointFeatureCollection.OnPointsUploaded{
                    override fun onPointsUploadFinished() {
                        progressDialog.dismiss()
                        Toast.makeText(context, context.resources.getString(R.string.point_saved),Toast.LENGTH_SHORT).show()
                    }

                })
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