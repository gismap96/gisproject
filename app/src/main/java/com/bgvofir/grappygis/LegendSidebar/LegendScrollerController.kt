package com.bgvofir.grappygis.LegendSidebar

import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

object LegendScrollerController {

    var opened = -1

    fun setOpenedView(position: Int, recyclerView: RecyclerView){
        if (opened>=0){
            val view = recyclerView.findViewHolderForAdapterPosition(opened) as LegendSidebarAdapter.LegendSidebarViewHolder
            view.legendDetailsRecyclerView.visibility = View.GONE
        }
        opened = position
    }
}