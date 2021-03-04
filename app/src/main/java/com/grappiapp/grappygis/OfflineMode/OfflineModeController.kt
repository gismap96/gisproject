package com.grappiapp.grappygis.OfflineMode

import android.app.ProgressDialog
import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.grappiapp.grappygis.Consts
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.R
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

object OfflineModeController{
    val TAG = "OfflineMode"
    val JSON_EXTENSION = ".txt"
    var isOfflineMode = false

    fun saveJSONLocally(context: Context,jsonObject: JSONObject, type: SketcherEditorTypes){
        val fileName = when (type){
            SketcherEditorTypes.POINT -> "points"
            SketcherEditorTypes.POLYLINE -> "polyline"
            SketcherEditorTypes.POLYGON -> "polygon"
            SketcherEditorTypes.MULTIPOINTS -> "points"
            SketcherEditorTypes.HYDRANTS -> "points"
        }
        val path = jsonPath(fileName)
        var jsonFile = File(path)
        var success = true
        if (!jsonFile.exists()){
            success = jsonFile.mkdirs()
            jsonFile.createNewFile()
        }
        if (success){
            val dest = File(jsonFile.absolutePath, fileName + JSON_EXTENSION)
            try{
                dest.writeText(jsonObject.toString())
            } catch (e: Exception){
                e.printStackTrace()
            }
            var reference = type.getOfflineReference()
            val editor = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE).edit()
            editor.putBoolean(Consts.DOES_OFFLINE_DATA_EXIST, true)
            editor.putBoolean(reference, true)
            editor.apply()
        }
    }

    fun startOfflineMode(context: Context){
        isOfflineMode = true
        resetSharedPreferences(context)
    }

    private fun resetSharedPreferences(context: Context) {
        val editor = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE).edit()
        editor.putBoolean(Consts.DOES_OFFLINE_POINT_DATA_EXIST, false)
        editor.putBoolean(Consts.DOES_OFFLINE_POLYGON_DATA_EXIST, false)
        editor.putBoolean(Consts.DOES_OFFLINE_POLYLINE_DATA_EXIST, false)
        editor.putBoolean(Consts.DOES_OFFLINE_DATA_EXIST, false)
        editor.apply()
    }

    fun exitOfflineMode(context: Context){
        uploadOfflineJSON(context)
        isOfflineMode = false
    }
    fun uploadOfflineJSON(context: Context){
        val sharedPrefs = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE)
        if (!sharedPrefs.getBoolean(Consts.DOES_OFFLINE_DATA_EXIST, false)){
            return
        }
        var config = mutableListOf<SketcherEditorTypes>()
        if (sharedPrefs.getBoolean(Consts.DOES_OFFLINE_POINT_DATA_EXIST, false)){
            config.add(SketcherEditorTypes.POINT)

        }
        if (sharedPrefs.getBoolean(Consts.DOES_OFFLINE_POLYLINE_DATA_EXIST, false)){
            config.add(SketcherEditorTypes.POLYLINE)
        }
        if (sharedPrefs.getBoolean(Consts.DOES_OFFLINE_POLYGON_DATA_EXIST, false)){
            config.add(SketcherEditorTypes.POLYGON)
        }

        if (config.size == 0){
            return
        }

        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle(context.getString(R.string.updating_layer))
        progressDialog.setCancelable(false)
        progressDialog.show()
        recursiveUpload(config, 0){
            resetSharedPreferences(context)
            progressDialog.dismiss()
        }

    }

    private fun recursiveUpload(config: MutableList<SketcherEditorTypes>, iterator: Int, callback: () -> Unit){
        uploadOfflineJSON(config[iterator]){
            val newIterator = iterator + 1
            if (newIterator == config.size){
                callback()
            } else {
                recursiveUpload(config, newIterator, callback)
            }
        }
    }

    fun getOfflineJSON(type: SketcherEditorTypes): JSONObject?{
        val fileName = when (type){
            SketcherEditorTypes.POINT -> "points"
            SketcherEditorTypes.POLYLINE -> "polyline"
            SketcherEditorTypes.POLYGON -> "polygon"
            else -> ""
        }
        val path = jsonPath(fileName)
        val jsonFile = File(path)
        if (jsonFile.exists()) {
            val dest = File(jsonFile.absolutePath, fileName + JSON_EXTENSION)
            val inputString = FileInputStream(dest).bufferedReader().use { it.readText() }
            return JSONObject(inputString)
        }
        return null
    }
    private fun uploadOfflineJSON(type: SketcherEditorTypes, callback: (Boolean)->Unit){
        val fileName = when (type){
            SketcherEditorTypes.POINT -> "points"
            SketcherEditorTypes.POLYLINE -> "polyline"
            SketcherEditorTypes.POLYGON -> "polygon"
            else -> ""
        }
        val path = jsonPath(fileName)
        val jsonFile = File(path)
        if (jsonFile.exists()){
            val dest = File(jsonFile.absolutePath, fileName + JSON_EXTENSION)
            val inputString = FileInputStream(dest).bufferedReader().use { it.readText() }
            if (inputString.count() > 0){
                val mProjectId = ProjectId.projectId
                val firebaseStorage = FirebaseStorage.getInstance()
                val storageRef = firebaseStorage.reference
                val username = FirebaseAuth.getInstance().uid
                val childRef = storageRef.child("settlements/$mProjectId/userLayers/$username/$fileName.json")
                val byteString = inputString.toByteArray()
                childRef.putBytes(byteString).addOnSuccessListener {
                    callback(true)
                    Log.d(TAG, "file $fileName uploaded")
                }.addOnFailureListener{
                    callback(false)
                }
            } else {
                callback(false)
            }

        } else {
            callback(false)
        }
    }

    fun deleteOfflineData() {
        deleteRecursive(File(jsonFolderPath()))
    }
    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) {
            child.delete()
            deleteRecursive(child)
        }
        fileOrDirectory.delete()
    }
    private fun jsonPath(fileName: String): String? {
        return (jsonFolderPath() + File.separator + fileName
                + JSON_EXTENSION)
    }
    private fun jsonFolderPath(): String? {
        return Environment.getExternalStorageDirectory().absolutePath + File.separator + Consts.GRAPPY_FOLDER_NAME + File.separator + ProjectId.projectId + File.separator + "offlineLayers"
    }
}