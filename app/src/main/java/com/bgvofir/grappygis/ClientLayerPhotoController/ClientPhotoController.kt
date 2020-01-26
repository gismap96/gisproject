package com.bgvofir.grappygis.ClientLayerPhotoController

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import com.bgvofir.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.bgvofir.grappygis.ClientPoint
import com.bgvofir.grappygis.ProjectRelated.ProjectId
import com.bgvofir.grappygis.ProjectRelated.UserPolyline
import com.bgvofir.grappygis.R
import com.esri.arcgisruntime.geometry.Geometry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object ClientPhotoController {
    private const val TAKE_PHOTO_FOR_LAYER = 2
    private const val EDIT_PHOTO_FOR_LAYER = 3
    lateinit var imageURI: Uri
    val storage = FirebaseStorage.getInstance()
    val reference = storage.reference
    lateinit var attributes: HashMap<String, Any>
    lateinit var geometry: Geometry


    fun editPhoto(context: Activity){
        val builder = StrictMode.VmPolicy.Builder() // for image intent
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photo = File(Environment.getExternalStorageDirectory(), "Pic.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo))
        imageURI = Uri.fromFile(photo)
        context.startActivityForResult(intent, EDIT_PHOTO_FOR_LAYER)
    }
    private fun takePhoto(context: Activity){
        val builder = StrictMode.VmPolicy.Builder() // for image intent
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photo = File(Environment.getExternalStorageDirectory(), "Pic.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo))
        imageURI = Uri.fromFile(photo)
        context.startActivityForResult(intent, TAKE_PHOTO_FOR_LAYER)
    }

    fun showPhotoQuestionDialog(activity: Activity, attributes: HashMap<String, Any>, geometry: Geometry,callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish, progressDialog: ProgressDialog){
        this.attributes = attributes
        this.geometry = geometry
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(activity)
        }
        builder.setTitle(R.string.add_photo)
                .setMessage(R.string.take_photo_prompt)
                .setPositiveButton(activity.getString(R.string.continue_to_photo)) { dialog, which -> takePhoto(activity) }
                .setNegativeButton(activity.getString(R.string.no_thank_you)) { dialog, which ->
                    progressDialog.show()
                    UserPolyline.userPolyline!!.createFeature(attributes,geometry)
                    UserPolyline.userPolyline!!.uploadJSON(callback) }
                .setIcon(R.drawable.ic_add_photo)
                .show()
    }
    fun uploadImage(uri: Uri?, context: Activity,callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish){
        uri?.let{
            uri->
            val ref = reference.child("settlements/" + ProjectId.projectId + "/images/" + UUID.randomUUID().toString())
            val compressedImage = reduceImageSize(uri)
            compressedImage?.let {
                ref.putFile(it).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri->
                        attributes["imageURL"] = uri.toString()
                        UserPolyline.userPolyline!!.createFeature(attributes, geometry)
                        UserPolyline.userPolyline!!.uploadJSON(callback)
                    }
                }.addOnFailureListener{
                    Toast.makeText(context, context.getString(R.string.uploaded), Toast.LENGTH_SHORT).show()
                    UserPolyline.userPolyline!!.createFeature(attributes, geometry)
                    UserPolyline.userPolyline!!.uploadJSON(callback)
                }
            }
        }
    }

    fun reduceImageSize(uri: Uri): Uri? {
        try {

            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 8
            // factor of downsizing the image

            val file = File(uri.path!!)

            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            val REQUIRED_SIZE = 80

            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)

            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()

            // here i override the original image file
            file.createNewFile()
            val outputStream = FileOutputStream(file)

            selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return Uri.fromFile(file)
        } catch (e: Exception) {
            return null
        }

    }
}