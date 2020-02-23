package com.grappiapp.grappygis.LegendSidebar

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

object LegendScrollerController {

    var opened = -1

    fun setOpenedView(position: Int, recyclerView: RecyclerView, view: ImageView): Boolean{
        if (opened>=0){
            if (recyclerView.findViewHolderForAdapterPosition(opened) is LegendSidebarAdapter.LegendSidebarViewHolder){
                val mView = recyclerView.findViewHolderForAdapterPosition(opened) as LegendSidebarAdapter.LegendSidebarViewHolder
                mView.legendDetailsRecyclerView.visibility = View.GONE
                LegendLayerDisplayController.closeSubArrowEffect(mView.legendIconIV)
            }else {
                opened = -1
                return false
            }
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