package com.grappiapp.grappygis.SearchController

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import android.view.Window
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_layer_details.*

class SearchResultsDialogFragment(context: Context): Dialog(context){
    val searchResults = FeatureSearchController.searchResults
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_search_results_display)
        var layoutManager = LinearLayoutManager(context)
        var recycler = dialogLayerDetailsRecyclerview
        recycler.layoutManager = layoutManager
        val adapter = SearchResultsAdapter(context, searchResults)
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}