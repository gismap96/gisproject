package com.bgvofir.grappygis.LegendSidebar

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bgvofir.grappygis.MapLayerAdapter
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.layers.Layer
import kotlinx.android.synthetic.main.legend_sidebar_group_overlay.view.*


class LegendSidebarAdapter(var context: Context, val layers: List<LegendGroup>): RecyclerView.Adapter<LegendSidebarAdapter.LegendSidebarViewHolder>(){


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LegendSidebarViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.legend_sidebar_group_overlay, p0, false)
        return LegendSidebarViewHolder(v)
    }

    override fun getItemCount(): Int {
        return layers.count()
    }

    override fun onBindViewHolder(p0: LegendSidebarViewHolder, p1: Int) {
        p0.bind(layers[p1], context)
    }

    class LegendSidebarViewHolder(v: View): RecyclerView.ViewHolder(v){
        var legendGroupNameTV = v.legendGroupNameTV
        var legendGroupsCheckBox = v.legendGroupsCheckBox
        var legendDetailsRecyclerView = v.legendDetailsRecyclerView

        fun bind(group: LegendGroup, context: Context){
            legendDetailsRecyclerView.visibility = View.GONE
            legendGroupNameTV.text = group.title
            val adapter = MapLayerAdapter(context)
            adapter.setLayerList(group.layers)
            val layoutManager = LinearLayoutManager(context)
            legendDetailsRecyclerView.layoutManager = layoutManager
            legendDetailsRecyclerView.adapter = adapter
            legendGroupsCheckBox.setOnClickListener {
                legendDetailsRecyclerView.visibility = View.GONE
                if (legendGroupsCheckBox.isChecked){
                    group.layers.forEach {
                        it.isVisible = true
                    }
                    adapter.setLayerList(group.layers)
                    adapter.notifyDataSetChanged()
                } else {
                    group.layers.forEach {
                        it.isVisible = false
                    }
                    adapter.setLayerList(group.layers)
                    adapter.notifyDataSetChanged()
                }
            }

            itemView.setOnClickListener {
                if (legendDetailsRecyclerView.visibility == View.GONE){
                    legendDetailsRecyclerView.visibility = View.VISIBLE
                } else {
                    legendDetailsRecyclerView.visibility = View.GONE
                }
            }

        }
    }
}