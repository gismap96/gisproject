package com.bgvofir.grappygis.LegendSidebar

import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.legend_sidebar_group_overlay.view.*

object LegendScrollerController {

    var opened = -1

    fun setOpenedView(position: Int, recyclerView: RecyclerView, view: ImageView): Boolean{
        if (opened>=0){
            if (recyclerView.findViewHolderForAdapterPosition(opened) is LegendSidebarAdapter.LegendSidebarViewHolder){
                val mView = recyclerView.findViewHolderForAdapterPosition(opened) as LegendSidebarAdapter.LegendSidebarViewHolder
                mView.legendDetailsRecyclerView.visibility = View.GONE
                LegendLayerDisplayController.closeSubArrowEffect(mView.legendIconIV)
            }
        } else {
            return false
        }
        LegendLayerDisplayController.openSubArrowEffect(view)
        opened = position
        return true
    }
    fun setClosed(view: ImageView){
        LegendLayerDisplayController.closeSubArrowEffect(view)
        opened = -1
    }
}