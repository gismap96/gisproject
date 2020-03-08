package com.grappiapp.grappygis.ClientFeatureLayers.AddFlexibleLayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.spinner_item_fill_color_add_layer.view.*

class SpinnerSymbolFillColor(context: Context, items: Array<FillColorForSymbol>):ArrayAdapter<FillColorForSymbol>(context, R.layout.spinner_item_fill_color_add_layer, items){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        convertView?.let{
            return convertView
        }
        return createView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, parent)
    }

    fun createView(position: Int, parent: ViewGroup): View {
        var convertedView = LayoutInflater.from(context).inflate(R.layout.spinner_item_fill_color_add_layer, parent, false)

        val item = getItem(position)
        item?.let{
            convertedView.symbolFillingAddLayerTV.setBackgroundColor(it.getColor(context))
        }
        return convertedView
    }
}