package com.bgvofir.grappygis.SketchController

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bgvofir.grappygis.R
import kotlinx.android.synthetic.main.row_for_sketcher_selection_dialog.view.*

class SketcherSelectionDialogAdapter(var sketchSelectionClickListener: OnSketchSelectionClickListener): RecyclerView.Adapter<SketcherSelectionDialogAdapter.SketcherSelectionDialogViewHolder>(){
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

        p0.bind(value, type)
    }

    inner class SketcherSelectionDialogViewHolder(view: View, val clickListener: OnSketchSelectionClickListener): RecyclerView.ViewHolder(view){

        var headlineTV = view.sketcherNameSelectionTV
        fun bind(headline: String, type: SketcherEditorTypes){
            headlineTV.text = headline
            itemView.setOnClickListener {
                clickListener.onSketchSelectionListener(type)
            }
        }
    }

    interface OnSketchSelectionClickListener{
        fun onSketchSelectionListener(sketcher: SketcherEditorTypes)
    }
}