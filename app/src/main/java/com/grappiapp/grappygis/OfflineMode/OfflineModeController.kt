package com.grappiapp.grappygis.OfflineMode

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import com.esri.arcgisruntime.mapping.view.SceneView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.grappiapp.grappygis.Consts
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

object OfflineModeController{
    val TAG = "OfflineMode"
    val JSON_EXTENSION = ".txt"
    var isOfflineMode = false

    fun saveJSONLocally(context: Context,fileName: String ,jsonObject: JSONObject){
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
            val editor = context.getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE).edit()
            editor.putBoolean(Consts.DOES_OFFLINE_DATA_EXIST, true)
            editor.putBoolean(fileName, true)
            editor.apply()
        }
    }

    fun uploadOfflineJSON(fileName: String, callback: (Boolean)->Unit){
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