package com.grappiapp.grappygis.SketchController

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.row_for_sketcher_selection_dialog.view.*

class SketcherSelectionDialogAdapter(var context: Context,var sketchSelectionClickListener: OnSketchSelectionClickListener): RecyclerView.Adapter<SketcherSelectionDialogAdapter.SketcherSelectionDialogViewHolder>(){
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SketcherSelectionDialogViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.row_for_sketcher_selection_dialog, p0, false)
        return SketcherSelectionDialogViewHolder(v, sketchSelectionClickListener)
    }

    override fun getItemCount(): Int {
        return SketcherEditorTypes.values().size
    }

    override fun onBindViewHolder(p0: SketcherSelectionDialogViewHolder, p1: Int) {
        val type = SketcherEditorTypes.values()[p1]
        val value = type.title

        p0.bind(value, type, context)
    }

    inner class SketcherSelectionDialogViewHolder(view: View, val clickListener: OnSketchSelectionClickListener): RecyclerView.ViewHolder(view){

        var sketcherIconSelectionIV = view.sketcherIconSelectionIV
        var headlineTV = view.sketcherNameSelectionTV
        fun bind(headline: Int, type: SketcherEditorTypes, context: Context){
            headlineTV.text = context.getString(headline)
            when (type){
                SketcherEditorTypes.POINT ->
                    sketcherIconSelectionIV.background = ContextCompat.getDrawable(context, R.drawable.ic_hollow_plus_star)
                SketcherEditorTypes.POLYLINE ->
                    sketcherIconSelectionIV.background = ContextCompat.getDrawable(context, R.drawable.ic_polyline_soft_red)
                SketcherEditorTypes.POLYGON ->
                    sketcherIconSelectionIV.background = ContextCompat.getDrawable(context, R.drawable.ic_polygon_area_measurement)
                SketcherEditorTypes.HYDRANTS -> {
                    sketcherIconSelectionIV.background = ContextCompat.getDrawable(context, R.drawable.ic_hydrant)
                }
                SketcherEditorTypes.MULTIPOINTS -> {
                    sketcherIconSelectionIV.background = ContextCompat.getDrawable(context, R.drawable.ic_setting)
                }
                SketcherEditorTypes.TRACKER->{
                    sketcherIconSelectionIV.background = ContextCompat.getDrawable(context, R.drawable.ic_location_tracker)
                }
            }
            itemView.setOnClickListener {
                clickListener.onSketchSelectionListener(type)
            }
        }
    }

    interface OnSketchSelectionClickListener{
        fun onSketchSelectionListener(sketcher: SketcherEditorTypes?)
    }
}