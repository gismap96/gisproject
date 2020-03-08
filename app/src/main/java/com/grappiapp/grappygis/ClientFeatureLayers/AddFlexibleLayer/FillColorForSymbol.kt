package com.grappiapp.grappygis.ClientFeatureLayers.AddFlexibleLayer

import android.content.Context
import androidx.core.content.ContextCompat
import com.grappiapp.grappygis.R

enum class FillColorForSymbol(val serialNum: Int, val colorValue: Int){
    WHITE(0, R.color.white),
    DARKBLUE(1, R.color.colorPrimary),
    YELLOW(2,R.color.yellow),
    RED(3, R.color.soft_red),
    GREEN(4, R.color.colorAccent);
    fun getColor(context: Context): Int{
        return ContextCompat.getColor(context, this.colorValue)
    }
}