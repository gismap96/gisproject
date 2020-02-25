package com.grappiapp.grappygis.Scene3DController

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.MobileScenePackage
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.SceneView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.grappiapp.grappygis.Consts
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.R
import java.io.File
import java.io.IOException


class SceneActivity : AppCompatActivity() {

    val SCENE_EXTENSION = ".mspk"
    lateinit var sharedPreferences: SharedPreferences
    lateinit var mSceneView: SceneView
    lateinit var extStorDir: File
    val storage = FirebaseStorage.getInstance()
    lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene)
        mSceneView = findViewById(R.id.mainSceneView)
        val scene = ArcGISScene(Basemap.createImagery())
        val surface = Surface()
        scene.baseSurface = surface
        storageReference = storage.reference
        extStorDir = Environment.getExternalStorageDirectory()
        sharedPreferences = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE)
        runMSPK()
        //downloadMSPK()
    }

    private fun runMSPK(){
        val path = createMSPKPath(ProjectId.projectId)
        var mspkFile = File(path)
        try{
            mspkFile.parentFile.mkdir()
            mspkFile.createNewFile()
        }catch (e: IOException){
            e.printStackTrace()
        }
        loadMobileScenePackage()
    }

    private fun downloadMSPK(){
        val path = createMSPKPath(ProjectId.projectId)
        var mspkFile = File(path)
        try{
            mspkFile.parentFile.mkdir()
            mspkFile.createNewFile()
        }catch(e: IOException){
            e.printStackTrace()
        }
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setTitle(getString(R.string.commencing_download))
        progressDialog.show()

        val mspkRef = storageReference.child("settlements/" + ProjectId.projectId + "/3D/HalChall.mspk")
        mspkRef.metadata.addOnSuccessListener { metadata->
            val timeModified = metadata.updatedTimeMillis
            val lastDownloadTime = sharedPreferences.getLong(Consts.DOWNLOAD_SCENE_TIME_KEY, Long.MIN_VALUE)
            if (timeModified > lastDownloadTime){
                if (lastDownloadTime != Long.MIN_VALUE){
                    deleteMSPKFolderData()
                    val path = createMSPKPath(ProjectId.projectId)
                    val mspkFile = File(path)
                    try{
                        mspkFile.parentFile.mkdir()
                        mspkFile.createNewFile()
                    }catch(e: IOException){
                        e.printStackTrace()
                    }
                }
                mspkRef.getFile(mspkFile).addOnSuccessListener {
                    val editor = sharedPreferences.edit()
                    editor.putLong(Consts.DOWNLOAD_TIME_KEY, System.currentTimeMillis())
                    editor.apply()
                    loadMobileScenePackage()
                    progressDialog.dismiss()
                }.addOnFailureListener{
                    progressDialog.dismiss()
                }.addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                            .totalByteCount
                    if (progress >= 0) {
                        val msg1 = getString(R.string.please_wait).toString() + "\n"
                        progressDialog.setMessage(msg1 + progress.toInt() + "%")
                    }
                }
            } else {
                progressDialog.dismiss()
                loadMobileScenePackage()

            }

        }
    }

    private fun loadMobileScenePackage() {
        val mobileScenePackage = MobileScenePackage(createMSPKPath(ProjectId.projectId))
        mobileScenePackage.addDoneLoadingListener {
            if (mobileScenePackage.loadStatus == LoadStatus.LOADED && !mobileScenePackage.scenes.isEmpty()) {
                mSceneView.scene = mobileScenePackage.scenes[0]
            } else {
                val error = "Failed to load mobile scene package: " + mobileScenePackage.loadError.message
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e("Scene", error)
            }
        }
        mobileScenePackage.loadAsync()
    }
    private fun deleteMSPKFolderData() {
        deleteRecursive(File(getMSPKFolderPath()))
    }
    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) {
            child.delete()
            deleteRecursive(child)
        }
        fileOrDirectory.delete()
    }
    private fun createMSPKPath(fileName: String): String? {
        return (getMSPKFolderPath() + File.separator + fileName
                + SCENE_EXTENSION)
    }
    private fun getMSPKFolderPath(): String? {
        return extStorDir.absolutePath + File.separator + Consts.GRAPPY_FOLDER_NAME + File.separator + ProjectId.projectId + File.separator + "mspk"
    }
//    private fun loadMobileScenePackage() {
//        val mobileScenePackage = MobileScenePackage()
//        mobileScenePackage.addDoneLoadingListener {
//            if (mobileScenePackage.loadStatus == LoadStatus.LOADED && !mobileScenePackage.scenes.isEmpty()) {
//                mSceneView.setScene(mobileScenePackage.scenes[0])
//            } else {
//                val error = "Failed to load mobile scene package: " + mobileScenePackage.loadError.message
//                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
//                Log.e(FragmentActivity.TAG, error)
//            }
//        }
//        mobileScenePackage.loadAsync()
//    }
}
