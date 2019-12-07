package com.bgvofir.grappygis.LayerCalloutDialog

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import com.bgvofir.grappygis.R
import kotlinx.android.synthetic.main.row_for_callout_dialog.view.*

class DialogLayerAdapter(val activity: Activity, val layers: Map<String,
        String>, val onRowClickListener: OnRowClickListener): RecyclerView.Adapter<DialogLayerAdapterViewHolder>(){

    var keyList =  arrayListOf<String>()
    var valueList =  arrayListOf<String>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        keyList.clear()
        valueList.clear()
        layers.forEach {
            keyList.add(it.key)
            valueList.add(it.value)
        }
    }
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DialogLayerAdapterViewHolder {

        val v = LayoutInflater.from(p0.context).inflate(R.layout.row_for_callout_dialog, p0, false)
        return DialogLayerAdapterViewHolder(v)

    }

    override fun getItemCount(): Int {
        return layers.size
    }

    override fun onBindViewHolder(p0: DialogLayerAdapterViewHolder, p1: Int) {
        val mIndex = keyList[p1]
        val mTitle = valueList[p1]
        p0.bind(mIndex, mTitle, onRowClickListener)
    }



}
class DialogLayerAdapterViewHolder(v: View): RecyclerView.ViewHolder(v){
    var mTextView: TextView = v.text_for_row_callout_dialog

    fun bind(layerIndex: String, layerTitle: String ,clickListener: OnRowClickListener){
        mTextView.text = layerTitle
        itemView.setOnClickListener {
            clickListener.onItemClicked(layerIndex)
        }
    }
}

interface OnRowClickListener{
    fun onItemClicked(layerIndex: String)
}