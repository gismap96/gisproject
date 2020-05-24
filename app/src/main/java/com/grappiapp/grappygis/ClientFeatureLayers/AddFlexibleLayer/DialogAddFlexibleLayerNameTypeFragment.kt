package com.grappiapp.grappygis.ClientFeatureLayers.AddFlexibleLayer

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import kotlinx.android.synthetic.main.fragment_dialog_add_flexible_layer_name_type.*

class DialogAddFlexibleLayerNameTypeFragment(context: Context): Dialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_dialog_add_flexible_layer_name_type)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val sketcherTypes = enumValues<SketcherEditorTypes>()
        SpinnerTypeSelectAdapter(context, R.layout.row_for_sketcher_selection_dialog,sketcherTypes).also{
            adapter-> layerTypeSelectionSpinner.adapter = adapter
        }
        val fillColors = enumValues<FillColorForSymbol>()
        SpinnerSymbolFillColor(context, fillColors).also {
            adapter-> layerSymbologyColorSelectionSpinner.adapter = adapter
        }

    }

}