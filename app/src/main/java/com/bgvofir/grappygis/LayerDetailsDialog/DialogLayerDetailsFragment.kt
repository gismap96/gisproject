package com.bgvofir.grappygis.LayerDetailsDialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.fragment_dialog_layer_details.*
import android.support.v7.widget.DividerItemDecoration
import android.R
import android.graphics.Color
import android.graphics.drawable.ColorDrawable


class DialogLayerDetailsFragment(var activity: Activity, internal var adapter: RecyclerView.Adapter<*>, var headline: String): Dialog(activity), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(com.bgvofir.grappygis.R.layout.fragment_dialog_layer_details)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var recycler = dialogLayerDetailsRecyclerview
        var layoutManager = LinearLayoutManager(activity)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))

        var finalHeadline = headline
        if (headline == "Feature Collection"){
            finalHeadline = "שכבה ממשתמש"
        }
        fragmentDialogLayerDetailsHeadline.text = finalHeadline
        fragmentDialogLayerDetailsClose.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            com.bgvofir.grappygis.R.id.fragmentDialogLayerDetailsClose ->{
                dismiss()
            }
        }
    }
}