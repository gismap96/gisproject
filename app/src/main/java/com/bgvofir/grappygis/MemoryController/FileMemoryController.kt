package com.bgvofir.grappygis.MemoryController

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import com.bgvofir.grappygis.Consts
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.nio.file.Files

object FileMemoryController{

    var path = ""
    val TAG = "memoryController"
    var projectID = ""
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference


    fun deleteMMPKFile(file: File){
        Log.d(TAG, file.path.toString())
        file.canonicalFile.delete()
        if (file.exists()){
            if(file.delete()){
                Log.d(TAG, "deleted")
            } else {
                Log.d(TAG, "didnt delete")
            }
        }else {
            Log.d(TAG, "file was deleted from cannon")
        }


    }

//    fun checkupUpdate(context: Context, callback: OnUpdateResolved){
//        val mmpkRef = storageRef.child("settlements/$projectID/mmpk/data.mmpk")
//        mmpkRef.metadata.addOnSuccessListener { meta->
//            val timeModified = meta.updatedTimeMillis
//            val sharedPreferences = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE)
//            val lastDownloadTime = sharedPreferences.getLong(Consts.DOWNLOAD_TIME_KEY, java.lang.Long.MIN_VALUE)
//            if (timeModified > lastDownloadTime){
//                val extStorDir = Environment.getExternalStorageDirectory()
//                val path = extStorDir.absolutePath + File.separator + Consts.GRAPPY_FOLDER_NAME +
//                        File.separator + projectID + File.separator + "mmpk"
//                val file = File(path)
//                deleteDirectory(file)
//                callback.onUpdateResolved()
//            }
//        }.addOnFailureListener {
//            Log.d(TAG, "no file at $mmpkRef")
//            callback.onUpdateResolved()
//        }
//    }
//
//    interface OnUpdateResolved{
//        fun onUpdateResolved()
//    }

}