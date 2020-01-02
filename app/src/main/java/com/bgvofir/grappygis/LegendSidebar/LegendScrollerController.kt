package com.bgvofir.grappygis.LegendSidebar

import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.legend_sidebar_group_overlay.view.*

object LegendScrollerController {

    var opened = -1

    fun setOpenedView(position: Int, recyclerView: RecyclerView, view: ImageView){
        if (opened>=0){
            val mView = recyclerView.findViewHolderForAdapterPosition(opened) as LegendSidebarAdapter.LegendSidebarViewHolder
            mView.legendDetailsRecyclerView.visibility = View.GONE
            LegendLayerDisplayController.closeSubArrowEffect(mView.legendIconIV)
        }
        LegendLayerDisplayController.openSubArrowEffect(view)
        opened = position
    }
    fun setClosed(view: ImageView){
        LegendLayerDisplayController.closeSubArrowEffect(view)
        opened = -1
    }
}