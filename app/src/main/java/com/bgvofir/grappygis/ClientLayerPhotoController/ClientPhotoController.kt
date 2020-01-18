package com.bgvofir.grappygis.ClientLayerPhotoController

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import java.io.File

object ClientPhotoController {
    private val TAKE_PHOTO_FOR_LAYER = 2
    lateinit var imageURI: Uri
    fun takePhoto(context: Activity){
        val builder = StrictMode.VmPolicy.Builder() // for image intent
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photo = File(Environment.getExternalStorageDirectory(), "Pic.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo))
        imageURI = Uri.fromFile(photo)
        context.startActivityForResult(intent, TAKE_PHOTO_FOR_LAYER)
    }
}