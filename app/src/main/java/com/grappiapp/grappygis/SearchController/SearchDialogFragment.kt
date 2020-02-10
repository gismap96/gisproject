package com.grappiapp.grappygis.SearchController

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.esri.arcgisruntime.mapping.view.MapView
import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_search.*

class SearchDialogFragment(context: Context, val mMapView: MapView): Dialog(context), AdapterView.OnItemSelectedListener, View.OnClickListener{

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
                R.id.startSearchTV-> {
                    if (validate()) {
                        val searchAtt = searchAttributeInLayerET.text.toString()
                        FeatureSearchController.searchInLayer(searchAtt, groupNum, layerNum){
                            features->
                            Log.d(TAG, features.toString())
                        }
                    }
                }
            }
        }
    }
}
