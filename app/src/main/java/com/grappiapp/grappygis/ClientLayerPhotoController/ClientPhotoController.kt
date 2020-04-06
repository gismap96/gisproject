package com.grappiapp.grappygis.ClientLayerPhotoController

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.grappiapp.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPointFeatureCollection
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.ProjectRelated.UserPoints
import com.grappiapp.grappygis.ProjectRelated.UserPolyline
import com.grappiapp.grappygis.R
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.mapping.view.GeoView
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPolygonFeatureCollection
import com.grappiapp.grappygis.GeoViewController.GeoViewController
import com.grappiapp.grappygis.ProjectRelated.UserPolygon
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_image_source_selection_bottom_sheet.view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object ClientPhotoController {
    val TAG = "PhotoCTRL"
    private const val TAKE_PHOTO_FOR_LAYER = 2
    private const val EDIT_PHOTO_FOR_LAYER = 3
    private const val TAKE_PHOTO_FROM_GALLERY = 4
    lateinit var imageURI: Uri
    val storage = FirebaseStorage.getInstance()
    val reference = storage.reference
    lateinit var attributes: HashMap<String, Any>
    lateinit var geometry: Geometry
    lateinit var type: GeometryType

    fun openBottomSheet(activity: Activity, attributes: HashMap<String, Any>, geometry: Geometry,callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish, progressDialog: ProgressDialog){
        this.attributes = attributes
        this.geometry = geometry
        this.type = geometry.geometryType
        val bottomSheet = BottomSheetDialog(activity)
        bottomSheet.setCancelable(false)
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_image_source_selection_bottom_sheet, null)
        bottomSheet.setContentView(view)
        bottomSheet.show()
        view.cameraImageResourceTV.setOnClickListener {
            takePhoto(activity)
            bottomSheet.dismiss()
        }
        view.galleryImageResourceTV.setOnClickListener {
            pullImageFromGallery(activity)
            bottomSheet.dismiss()
        }
        view.noImageSourceTV.setOnClickListener {
            when (type){
                GeometryType.POINT -> {
                    val progressDialog = ProgressDialog(activity)
                    progressDialog.setTitle(activity.getString(R.string.updating_layer))
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    UserPoints.userPoints!!.createFeature(attributes, geometry){
                        UserPoints.userPoints!!.uploadJSON(object: ClientPointFeatureCollection.OnPointsUploaded{
                            override fun onPointsUploadFinished() {
                                progressDialog.dismiss()
                                Toast.makeText(activity, activity.resources.getString(R.string.point_saved),Toast.LENGTH_SHORT).show()
                            }

                        })
                    }
                }
                GeometryType.ENVELOPE -> {}
                GeometryType.POLYLINE -> {
                    progressDialog.show()
                    UserPolyline.userPolyline!!.createFeature(attributes,geometry)
                    UserPolyline.userPolyline!!.uploadJSON(callback)
                }
                GeometryType.POLYGON -> {
                    val progressDialog = ProgressDialog(activity)
                    progressDialog.setTitle(activity.getString(R.string.updating_layer))
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    UserPolygon.userPolygon!!.createFeature(attributes,geometry){
                        UserPolygon.userPolygon!!.uploadJSON {
                            progressDialog.hide()
                            Log.d(TAG, geometry.toJson())
                            Toast.makeText(activity, activity.resources.getString(R.string.polygon_saved),Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                GeometryType.MULTIPOINT -> {}
                GeometryType.UNKNOWN -> {}
            }
            bottomSheet.dismiss()
        }

    }
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

    private fun pullImageFromGallery(context: Activity){
        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        intent.type = "image/*"
        context.startActivityForResult(intent, TAKE_PHOTO_FROM_GALLERY)
    }

    fun showPhotoQuestionDialog(activity: Activity, attributes: HashMap<String, Any>, geometry: Geometry,callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish, progressDialog: ProgressDialog){
        this.attributes = attributes
        this.geometry = geometry
        this.type = geometry.geometryType
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
                    when (type){
                        GeometryType.POINT -> {
                            val progressDialog = ProgressDialog(activity)
                            progressDialog.setTitle(activity.getString(R.string.updating_layer))
                            progressDialog.setCancelable(false)
                            progressDialog.show()
                            UserPoints.userPoints!!.createFeature(attributes, geometry){
                                UserPoints.userPoints!!.uploadJSON(object: ClientPointFeatureCollection.OnPointsUploaded{
                                    override fun onPointsUploadFinished() {
                                        progressDialog.dismiss()
                                        Toast.makeText(activity, activity.resources.getString(R.string.point_saved),Toast.LENGTH_SHORT).show()
                                    }

                                })
                            }
                        }
                        GeometryType.ENVELOPE -> {}
                        GeometryType.POLYLINE -> {
                            progressDialog.show()
                            UserPolyline.userPolyline!!.createFeature(attributes,geometry)
                            UserPolyline.userPolyline!!.uploadJSON(callback)
                        }
                        GeometryType.POLYGON -> {
                            val progressDialog = ProgressDialog(activity)
                            progressDialog.setTitle(activity.getString(R.string.updating_layer))
                            progressDialog.setCancelable(false)
                            progressDialog.show()
                            UserPolygon.userPolygon!!.createFeature(attributes,geometry){
                                UserPolygon.userPolygon!!.uploadJSON {
                                    progressDialog.hide()
                                    Log.d(TAG, geometry.toJson())
                                    Toast.makeText(activity, activity.resources.getString(R.string.polygon_saved),Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        GeometryType.MULTIPOINT -> {}
                        GeometryType.UNKNOWN -> {}
                    }

                }
                .setIcon(R.drawable.ic_add_photo)
                .show()
    }


    fun uploadImage(uri: Uri?, context: Activity,callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish, callbackPoint: ClientPointFeatureCollection.OnPointsUploaded, callbackPolygon: ClientPolygonFeatureCollection.OnClientPolygonUploadFinished,
                    mapView: MapView){
        uri?.let{
            uri->
            val ref = reference.child("settlements/" + ProjectId.projectId + "/images/" + UUID.randomUUID().toString())
            val compressedImage = reduceImageSize(uri)
            compressedImage?.let {
                uploadImageToServer(ref, it, mapView, callbackPoint, callback, callbackPolygon, context)
            }
            if (compressedImage == null){
                uploadImageToServer(ref, uri, mapView, callbackPoint, callback, callbackPolygon, context)
            }
        }
    }

    private fun uploadImageToServer(ref: StorageReference, it: Uri, mapView: MapView, callbackPoint: ClientPointFeatureCollection.OnPointsUploaded, callback: ClientFeatureCollectionLayer.OnPolylineUploadFinish, callbackPolygon: ClientPolygonFeatureCollection.OnClientPolygonUploadFinished, context: Activity): StorageTask<UploadTask.TaskSnapshot> {
        return ref.putFile(it).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                attributes["imageURL"] = uri.toString()
                when (type) {
                    GeometryType.POINT -> {
                        UserPoints.userPoints!!.createFeature(attributes, geometry) {
                            GeoViewController.setCurrentViewPointForMap(mapView)
                            UserPoints.userPoints!!.uploadJSON(callbackPoint)
                        }
                    }
                    GeometryType.ENVELOPE -> {
                    }
                    GeometryType.POLYLINE -> {
                        UserPolyline.userPolyline!!.createFeature(attributes, geometry)
                        GeoViewController.setCurrentViewPointForMap(mapView)
                        UserPolyline.userPolyline!!.uploadJSON(callback)
                    }
                    GeometryType.POLYGON -> {
                        UserPolygon.userPolygon!!.createFeature(attributes, geometry) {
                            GeoViewController.setCurrentViewPointForMap(mapView)
                            UserPolygon.userPolygon!!.uploadJSON(callbackPolygon)
                        }
                    }
                    GeometryType.MULTIPOINT -> {
                    }
                    GeometryType.UNKNOWN -> {
                    }
                }

            }
        }.addOnFailureListener {
            Toast.makeText(context, context.getString(R.string.uploaded), Toast.LENGTH_SHORT).show()
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