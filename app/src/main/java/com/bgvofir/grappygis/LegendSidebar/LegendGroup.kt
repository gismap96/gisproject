package com.bgvofir.grappygis.LegendSidebar

import com.esri.arcgisruntime.layers.Layer

class LegendGroup(title: String, layers: List<Layer>) {
    val title = title
    val layers = layers
}