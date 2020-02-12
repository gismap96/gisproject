package com.grappiapp.grappygis.SearchController

import com.esri.arcgisruntime.data.Feature

class SearchResult (val FID: String?, val fieldName: String, val fieldValue: String, val feature: Feature){
}