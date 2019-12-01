package com.bgvofir.grappygis.LayerCalloutDialog

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bgvofir.grappygis.R
import kotlinx.android.synthetic.main.row_for_callout_dialog.view.*

class DialogLayerAdapter(val activity: Activity, val layers: Map<String, String>): RecyclerView.Adapter<DialogLayerAdapterViewHolder>(){

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

        keyList.forEach {
            p0.mTextView.text = it
        }
    }

}
class DialogLayerAdapterViewHolder(v: View): RecyclerView.ViewHolder(v){
    var mTextView: TextView = v.text_for_row_callout_dialog
}