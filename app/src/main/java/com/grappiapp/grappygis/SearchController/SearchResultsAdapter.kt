package com.grappiapp.grappygis.SearchController

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.geometry.Envelope
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.row_for_search_dialog_result.view.*

class SearchResultsAdapter(val context: Context, var results: MutableList<SearchResult>, var searchClickedListener: OnSearchResultClicked, val callback: ()->Unit): RecyclerView.Adapter<SearchResultsAdapter.SearchResultsAdapterViewHolder>() {



    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SearchResultsAdapterViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.row_for_search_dialog_result, p0, false)
        return SearchResultsAdapterViewHolder(v)
    }

    override fun getItemCount(): Int {
        return results.count()
    }

    override fun onBindViewHolder(p0: SearchResultsAdapterViewHolder, p1: Int) {
        p0.bind(results[p1], searchClickedListener, callback)
    }
    class SearchResultsAdapterViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val fidVal = view.searchFidValueResultTV
        private val fieldNameTV = view.searchFieldNameTV
        private val fieldValueTV = view.searchResultValueTV

        fun bind(result: SearchResult, searchListener: OnSearchResultClicked, callback: () -> Unit){
            fidVal.text = result.FID
            fieldNameTV.text = result.fieldName
            fieldValueTV.text = result.fieldValue
            itemView.setOnClickListener {
                val envelope = result.feature.geometry.extent
                val layer = FeatureSearchController.featureLayerResult
                layer?.let{
                    layer.selectFeature(result.feature)
                    layer.isVisible = true
                    searchListener.jumpToSearchResultFeature(envelope)
                    FeatureSearchController.isFeatureSelected = true
                    callback()
                }

            }
        }
    }

    interface OnSearchResultClicked{
        fun jumpToSearchResultFeature(envelope: Envelope)
    }
}