package com.grappiapp.grappygis.SearchController

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureCollection
import com.esri.arcgisruntime.layers.FeatureCollectionLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.GeoViewController.GeoViewController
import com.grappiapp.grappygis.LegendSidebar.LegendLayerDisplayController
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_layer_details.*
import kotlinx.android.synthetic.main.fragment_dialog_layer_selection.*
import kotlinx.android.synthetic.main.fragment_dialog_search.*

class SearchDialogFragment(context: Context, val mMapView: MapView, val callback: OnMultipleSearchResults): Dialog(context), AdapterView.OnItemSelectedListener, View.OnClickListener{

    var titles = mutableListOf<String>()
    var groupNum = 0
    var layerNum = 0
    val TAG = "searchDialog"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_search)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        titles = FeatureSearchController.getGroupTitles()
        if (titles.count() > 0){
            ArrayAdapter(context, R.layout.search_category_spinner_item, titles).also {
                arrayAdapter ->  categoryForSearchSpinner.adapter = arrayAdapter

            }
            categoryForSearchSpinner.onItemSelectedListener = this
            layerSearchSpinner.onItemSelectedListener = this
            startSearchTV.setOnClickListener(this)
            closeSearchDialogTV.setOnClickListener(this)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        parent?.let{
            when (it.id){
                R.id.categoryForSearchSpinner->{
                }
                R.id.layerSearchSpinner->{
                }
                else->{}
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.let{
            when (it.id){
                R.id.categoryForSearchSpinner->{
                    val groupTitle = titles[position]
                    groupNum = position
                    val layerNames = FeatureSearchController.getLayerTitlesForCategory(groupTitle)
                    ArrayAdapter(context, R.layout.search_category_spinner_item, layerNames).also {
                        arrayAdapter -> layerSearchSpinner.adapter = arrayAdapter
                    }

                }
                R.id.layerSearchSpinner->{
                    layerNum = position
                }
                else -> {}
            }
        }
    }

    fun validate(): Boolean{
        if (searchAttributeInLayerET.text.toString().trim().isEmpty() || searchAttributeInLayerET.text == null){
            searchAttributeInLayerET.error = context.getString(R.string.field_mandatory)
            return false
        }
        return true
    }

    override fun onClick(v: View?) {
        v?.let{
            when (it.id){
                R.id.closeSearchDialogTV->{
                    dismiss()
                }
                R.id.startSearchTV-> {
                    if (validate()) {
                        val view = this.currentFocus
                        view?.let { v ->
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(v.windowToken, 0)
                        }
                        val searchAtt = searchAttributeInLayerET.text.toString()
                        if (searchAtt.toLowerCase() == "meow" || searchAtt.toLowerCase() == "מיאו"){
                            Toast.makeText(context, "Meow ~", Toast.LENGTH_LONG).show()
                        }
                        val legendGroups = LegendLayerDisplayController.legendGroups
                        val layer = legendGroups[groupNum].layers[layerNum]
                        if (layer is FeatureLayer) {
                            FeatureSearchController.searchInLayer(searchAtt, layer, false, context) { results ->
                                processSearchResults(results)
                            }
                        } else if (layer is FeatureCollectionLayer){
                            FeatureSearchController.searchInLayer(searchAtt, layer.layers[0], true, context){results->
                                processSearchResults(results)
                            }


                        }
                    }
                }
            }
        }
    }

    private fun processSearchResults(results: MutableList<SearchResult>) {
        if (results.count() == 0) {
            Toast.makeText(context, context.getString(R.string.search_no_results), Toast.LENGTH_LONG).show()
        } else {
            if (results.count() > 1) {
                var layoutManager = LinearLayoutManager(context)
                var recycler = searchMultiResultsRecyclerV
                recycler.layoutManager = layoutManager
                val adapter = SearchResultsAdapter(context, results)
                recycler.adapter = adapter
                recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))
            } else {
                val legendGroups = LegendLayerDisplayController.legendGroups
                val layer = legendGroups[groupNum].layers[layerNum]
                val feature = results[0].feature
                val envelope = feature.geometry.extent
                if (layer is FeatureLayer){
                    layer.selectFeature(feature)
                } else if (layer is FeatureCollectionLayer){
                    layer.layers[0].selectFeature(feature)
                }

                val PADDING = 3.0
                GeoViewController.moveToLocationByGeometry(envelope, PADDING, mMapView)
                FeatureSearchController.isFeatureSelected = true
                dismiss()
            }

            Log.d(TAG, results.toString())
        }
    }

    interface OnMultipleSearchResults{
        fun onMultipleSearchResults()
    }
}
