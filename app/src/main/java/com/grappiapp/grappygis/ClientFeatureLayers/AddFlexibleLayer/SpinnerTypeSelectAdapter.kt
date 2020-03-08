package com.grappiapp.grappygis.ClientFeatureLayers.AddFlexibleLayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes

class SpinnerTypeSelectAdapter (context:Context, resource: Int, items: Array<SketcherEditorTypes>): ArrayAdapter<SketcherEditorTypes>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        convertView?.let{
            return convertView
        }
        return createView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, parent)
    }

    fun createView(position: Int, parent: ViewGroup): View{
        var convertedView = LayoutInflater.from(context).inflate(R.layout.row_for_sketcher_selection_dialog, parent, false)

        val item = getItem(position)
        val typeIconIV = convertedView!!.findViewById<ImageView>(R.id.sketcherIconSelectionIV)
        val typeNameTV = convertedView!!.findViewById<TextView>(R.id.sketcherNameSelectionTV)
        item?.let{
            typeIconIV.setImageResource(item.getImageAddress())
            typeNameTV.text = context.getText(item.title)
        }
        return convertedView
    }
}