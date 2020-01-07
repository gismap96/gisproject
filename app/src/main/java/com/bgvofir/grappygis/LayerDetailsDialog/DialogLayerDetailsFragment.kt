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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import java.util.concurrent.ExecutionException


class DialogLayerDetailsFragment(var activity: Activity, internal var adapter: RecyclerView.Adapter<*>, var headline: String, val identifiedLayer: IdentifyLayerResult): Dialog(activity), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(com.bgvofir.grappygis.R.layout.fragment_dialog_layer_details)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        var recycler = dialogLayerDetailsRecyclerview
        var layoutManager = LinearLayoutManager(activity)
        recycler.layoutManager = layoutManager
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))

        var finalHeadline = headline
        if (headline == "Feature Collection"){
            finalHeadline = context.resources.getString(com.bgvofir.grappygis.R.string.client_point)
            layerIconForDetailsDialog.setImageBitmap(getBitmapFromVectorDrawable(com.bgvofir.grappygis.R.drawable.ic_star_blue))
        } else {
            var layerLegend = identifiedLayer.layerContent.fetchLegendInfosAsync()
            layerLegend.addDoneListener {
                try {
                    var legendInfo = layerLegend.get()
                    if (legendInfo.size > 0) {
                        val legendSymbol = legendInfo[0].symbol
                        val symbolSwatch = legendSymbol.createSwatchAsync(context, Color.TRANSPARENT)
                        val symbolBitmap = symbolSwatch.get()
                        layerIconForDetailsDialog.setImageBitmap(symbolBitmap)
                    }
                } catch (e: InterruptedException) {

                } catch (e: ExecutionException) {

                }
            }
        }
        fragmentDialogLayerDetailsHeadline.text = finalHeadline
        fragmentDialogLayerDetailsClose.setOnClickListener(this)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    override fun onClick(v: View?) {
        when(v?.id){
            com.bgvofir.grappygis.R.id.fragmentDialogLayerDetailsClose ->{
                dismiss()
            }
        }
    }
}