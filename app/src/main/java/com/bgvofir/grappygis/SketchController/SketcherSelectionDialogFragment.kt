package com.bgvofir.grappygis.SketchController

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bgvofir.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_layer_selection.*
import kotlinx.android.synthetic.main.fragment_dialog_sketch_mode_selection.*

class SketcherSelectionDialogFragment(var activity: Activity,  var adapter: RecyclerView.Adapter<*>): Dialog(activity), View.OnClickListener{


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(com.bgvofir.grappygis.R.layout.fragment_dialog_sketch_mode_selection)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var recycler = sketcherModeSelectionRecyclerView
        var layoutManager = LinearLayoutManager(activity)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter

        closeSketchSelectDialog.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.closeSketchSelectDialog ->{
                dismiss()
            }
        }
    }
}