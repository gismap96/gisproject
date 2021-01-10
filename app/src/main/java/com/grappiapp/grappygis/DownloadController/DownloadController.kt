package com.grappiapp.grappygis.DownloadController

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.grappiapp.grappygis.DescriptionDialog
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.Utils
import java.io.File

object DownloadController {
    fun downloadMultiple(context: Context, storageReference: StorageReference, rasterFolderPath: String,
                         mProjectId: String, progressDialog: ProgressDialog, iteration: Int, callback: OnFinishedDownloadListener){
        val rasterRef: StorageReference = storageReference.child("settlements/$mProjectId/raster/raster_data$iteration.zip")
        val rasterFolderFile = File(rasterFolderPath + File.separator + "raster_data$iteration.zip")
        rasterRef.getFile(rasterFolderFile).addOnSuccessListener(OnSuccessListener<FileDownloadTask.TaskSnapshot?> {

            try {
                val zipFile = File(rasterFolderPath + File.separator + "raster_data"+ iteration.toString() +".zip")
                if (!zipFile.exists()){
                    callback.onFinishedDownloadListener(progressDialog)
                    return@OnSuccessListener
                }
                Utils.unzip(zipFile, File(rasterFolderPath))
                val newIter = iteration + 1
                downloadMultiple(context, storageReference, rasterFolderPath, mProjectId, progressDialog, newIter, callback)
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onFinishedDownloadListener(progressDialog)
            }
        }).addOnFailureListener(OnFailureListener { e ->
            Log.e("MainActivity", "DownloadRaster failed: " + e.message)
            callback.onFinishedDownloadListener(progressDialog)
        })
                    .addOnProgressListener(OnProgressListener<FileDownloadTask.TaskSnapshot> { taskSnapshot ->
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                    .totalByteCount
            if (progress >= 0 && progress <= 97) {
                val msg1: String = context.getString(R.string.please_wait_file_number)+ iteration+ "\n"
                progressDialog.setMessage(msg1 + progress.toInt() + "%")
            } else if (progress >= 98) {
                val msg: String = context.getString(R.string.finalising_download)
                progressDialog.setMessage(msg)
            }
        })
    }
    interface OnFinishedDownloadListener{
        fun onFinishedDownloadListener(progressDialog: ProgressDialog)
    }
}
