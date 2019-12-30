package com.bgvofir.grappygis.MemoryController

import android.util.Log
import java.io.File
import java.nio.file.Files

object FileMemoryController{

    var path = ""
    val TAG = "memoryController"

    fun deleteMMPKFolder(){
        var file = File(path)
        if (file.delete()){
            Log.d("memoryController", "folder deleted")
        } else {
            Log.d("memoryController", "failed to delete folder")
        }

    }

}