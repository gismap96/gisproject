package com.bgvofir.grappygis.LayerCalloutDialog


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window

import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.internal.jni.id
import kotlinx.android.synthetic.main.fragment_dialog_layer_selection.*
import kotlinx.android.synthetic.main.fragment_dialog_layer_selection.view.*

class DialogLayerSelectionFragment(var activity: Activity, internal var adapter: RecyclerView.Adapter<*>) : Dialog(activity), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_layer_selection)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        var recycler = layerCalloutDialogRecyclerview
        var layoutManager = LinearLayoutManager(activity)

        recycler.layoutManager = layoutManager
        recycler.adapter = adapter



        closePopupDialog.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.closePopupDialog ->{
                dismiss()
            }
        }
//        when (v.id == )
//        dismiss()
    }
}







//override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onAttach(context: Context?) {
//        super.onAttach(context)
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//
//        val rootView = inflater.inflate(R.layout.fragment_dialog_layer_selection, container)
//        val mLayerRecyclerView = rootView.findViewById(R.id.layer_select_dialog_recyclerview) as RecyclerView
//        val dump = arrayOf("meow", "woof", "quack")
//        mLayerRecyclerView.adapter = DialogLayerAdapter(activity!!, dump)
//        return rootView
//    }
