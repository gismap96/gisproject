package com.bgvofir.grappygis.SketchController

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.bgvofir.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.bgvofir.grappygis.LayerCalloutControl.FeatureLayerController
import com.bgvofir.grappygis.LegendSidebar.LegendLayerDisplayController
import com.bgvofir.grappygis.ProjectRelated.MapProperties
import com.bgvofir.grappygis.ProjectRelated.ProjectId
import com.bgvofir.grappygis.ProjectRelated.UserPolyline
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.mapping.view.MapView
import kotlinx.android.synthetic.main.fragment_dialog_sketcher_save_input.*
import java.util.*

class SketcherSaveDialogFragment(context: Context, mMapView: MapView, isZift2: Boolean,
                                 callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish, val layerListener: LegendLayerDisplayController.LayerGroupsListener): Dialog(context), View.OnClickListener {

    val callback = callback

    var mMapView = mMapView
    val iszift2 = isZift2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_sketcher_save_input)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        closeSketcherSaveTV.setOnClickListener(this)

    }

    fun setGeometry(){
        val geometry = SketchEditorController.getGeometry()
        if (geometry == null){
            val toast = Toast.makeText(context, "צורה ריקה", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()
            dismiss()
            return
        }
        val type = SketchEditorController.sketcherEditorTypes
        val category = addCategoryToSketcherSaveET.text.toString()
        when (type){
            SketcherEditorTypes.POLYLINE -> {
                if (UserPolyline.userPolyline == null){
                    UserPolyline.initFields()
                    UserPolyline.userPolyline = ClientFeatureCollectionLayer("פוליליין ממשתמש", UUID.randomUUID().toString(),
                            UserPolyline.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPolyline.userPolyline!!.layer)
                    layerListener.successListener()
                } else if (UserPolyline.userPolyline!!.features.size == 0){
                    UserPolyline.userPolyline = ClientFeatureCollectionLayer("פוליליין ממשתמש", UUID.randomUUID().toString(),
                            UserPolyline.grappiFields, MapProperties.spatialReference!!)
                    mMapView.map.operationalLayers.add(UserPolyline.userPolyline!!.layer)
                    layerListener.successListener()
                }
                val attributes = hashMapOf<String, Any>()
                attributes.put("category", category)
                UserPolyline.userPolyline!!.createFeature(attributes, geometry)
                SketchEditorController.clean(mMapView)
                UserPolyline.userPolyline!!.uploadJSON(callback)
            }
            SketcherEditorTypes.POLYGON -> {

            }
        }
        dismiss()
    }


    override fun onClick(v: View?) {
        when (v?.id){
            R.id.closeSketcherSaveTV ->{
                setGeometry()
            }
        }
    }
}