package com.bgvofir.grappygis.LegendSidebar

import android.content.Context
import android.os.SystemClock
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bgvofir.grappygis.MapLayerAdapter
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.layers.Layer
import kotlinx.android.synthetic.main.legend_sidebar_group_overlay.view.*


class LegendSidebarAdapter(var context: Context, val layers: List<LegendGroup>, val recyclerView: RecyclerView): RecyclerView.Adapter<LegendSidebarAdapter.LegendSidebarViewHolder>(){


    var mLayers = layers.toMutableList()
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LegendSidebarViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.legend_sidebar_group_overlay, p0, false)
        return LegendSidebarViewHolder(v)
    }

    fun addLayerToLast(layer: Layer){
        val lastGroup = mLayers[mLayers.lastIndex]
        if (lastGroup.title != "שכבות משתמש"){
            var toList = mutableListOf<Layer>()
            toList.add(layer)
            val mGroup = LegendGroup("שכבות משתמש", toList)
            mLayers.add(mGroup)
        }
    }
    override fun getItemCount(): Int {
        return mLayers.count()
    }

    override fun onBindViewHolder(p0: LegendSidebarViewHolder, p1: Int) {
        p0.bind(mLayers[p1], context)
        p0.setIsRecyclable(false)
        p0.itemView.setOnClickListener {
            if (p0.legendDetailsRecyclerView.visibility == View.GONE){
                LegendScrollerController.setOpenedView(p1, recyclerView)
                p0.legendDetailsRecyclerView.visibility = View.VISIBLE
            } else {
                LegendScrollerController.setClosed()
                p0.legendDetailsRecyclerView.visibility = View.GONE
            }
        }
    }

    class LegendSidebarViewHolder(v: View): RecyclerView.ViewHolder(v){
        var legendGroupNameTV = v.legendGroupNameTV
//        var legendGroupsCheckBox = v.legendGroupsCheckBox
        var legendDetailsRecyclerView = v.legendDetailsRecyclerView

        fun bind(group: LegendGroup, context: Context){
            legendGroupNameTV.text = group.title
            val adapter = MapLayerAdapter(context)
            adapter.setLayerList(group.layers)
            val layoutManager = LinearLayoutManager(context)
            legendDetailsRecyclerView.layoutManager = layoutManager
            legendDetailsRecyclerView.adapter = adapter
            legendDetailsRecyclerView.visibility = View.GONE
//            legendGroupsCheckBox.visibility = View.GONE
//            legendGroupsCheckBox.setOnClickListener {
//                legendDetailsRecyclerView.visibility = View.GONE
//                if (legendGroupsCheckBox.isChecked){
//                    group.layers.forEach {
//                        it.isVisible = true
//                    }
//                    adapter.setLayerList(group.layers)
//                    adapter.notifyDataSetChanged()
//                } else {
//                    group.layers.forEach {
//                        it.isVisible = false
//                    }
//                    adapter.setLayerList(group.layers)
//                    adapter.notifyDataSetChanged()
//                }
//            }



        }
    }
}