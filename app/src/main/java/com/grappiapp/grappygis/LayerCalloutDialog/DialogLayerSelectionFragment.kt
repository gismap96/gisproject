package com.grappiapp.grappygis.LayerCalloutDialog


import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.grappiapp.grappygis.R
import kotlinx.android.synthetic.main.fragment_dialog_layer_selection.*

class DialogLayerSelectionFragment(var activity: Activity, internal var adapter: RecyclerView.Adapter<*>) : Dialog(activity), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.fragment_dialog_layer_selection)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        var recycler = layerCalloutDialogRecyclerview
        var layoutManager = LinearLayoutManager(activity)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
        window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_LTR



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
