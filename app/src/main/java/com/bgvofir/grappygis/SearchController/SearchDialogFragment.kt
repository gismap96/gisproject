package com.bgvofir.grappygis.SearchController

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bgvofir.grappygis.LegendSidebar.LegendGroup
import com.bgvofir.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_search.*

class SearchDialogFragment(context: Context): Dialog(context), AdapterView.OnItemSelectedListener{

    var titles = mutableListOf<String>()
    var groupNum = 0
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
                    FeatureSearchController.searchInLayer("meow",groupNum, position)
                }
                else -> {}
            }
        }
    }
}
