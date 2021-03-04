package com.grappiapp.grappygis.SketchController

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPointFeatureCollection
import com.grappiapp.grappygis.OfflineMode.OfflineModeController
import com.grappiapp.grappygis.ProjectRelated.MapProperties
import com.grappiapp.grappygis.ProjectRelated.UserPoints
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_hydrants_save.*
import java.util.*
import kotlin.collections.HashMap

open class SketcherSaveHydrantsDialogFragment (val context: Activity, val mMapView: MapView): Dialog(context), View.OnClickListener{
    val TAG = "hydrantSave"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_hydrants_save)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        cancelHydrantsSaveTV.setOnClickListener(this)
        saveHydrantsHeadsTV.setOnClickListener(this)
    }

    private fun setAttributes(): HashMap<String, Any>{
        val attributes = hashMapOf<String, Any>()
        var hydrantClass = "2\""
        if (hydrantThreeZolRB.isChecked){
            hydrantClass = "3\""
        }
        hydrantClass = context.getString(R.string.type_of_hydrant) + " $hydrantClass"
        var hydrantHeads = context.getString(R.string.hydrant_heads) + " "
        hydrantHeads += if (headsOneRB.isChecked){
            "1"
        } else {
            "2"
        }
        var atmosphericPressure = saveHydrantsAtmosphericTV.text.toString()
        if (atmosphericPressure.length > 0){
            attributes["number"] = atmosphericPressure
        } else {
            attributes["number"] = " "
        }
        attributes["category"] = hydrantClass
        attributes["description"] = hydrantHeads
        attributes["isUpdated"] = "yes"
        attributes["imageURL"] = ""
        return attributes
    }

    fun addFeatures(attributes: HashMap<String, Any>){
        val geometry = SketchEditorController.getGeometry()
        if (UserPoints.userPoints == null){
            UserPoints.userPoints = ClientPointFeatureCollection(context, context.resources.getString(R.string.my_points), UUID.randomUUID().toString(),
                    UserPoints.grappiFields, MapProperties.spatialReference!!)
            mMapView.map.operationalLayers.add(UserPoints.userPoints!!.layer)
        }
        if (OfflineModeController.isOfflineMode){
            UserPoints.userPoints!!.addFeatureFromMultipoints(geometry!!,attributes){
                OfflineModeController.saveJSONLocally(context, UserPoints.userPoints!!.generateARCGISJSON(), SketcherEditorTypes.POINT)
                dismiss()
            }
        } else {

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
    override fun onClick(v: View?) {
        when (v?.id){
            R.id.cancelHydrantsSaveTV -> {
                dismiss()
            }
            R.id.saveHydrantsHeadsTV -> {
                val attributes = setAttributes()
                addFeatures(attributes)
            }
        }
    }

}