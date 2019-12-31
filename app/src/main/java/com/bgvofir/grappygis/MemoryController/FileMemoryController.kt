package com.bgvofir.grappygis.MemoryController

import android.content.Context
import android.util.Log
import java.io.File
import java.nio.file.Files

object FileMemoryController{

    var path = ""
    val TAG = "memoryController"

    fun deleteMMPKFile(file: File){
        var del = File(file.toURI().path)
        del.canonicalFile.delete()
        if (del.exists()){
            if(del.delete()){
                Log.d(TAG, "deleted")
            } else {
                Log.d(TAG, "didnt delete")
            }
        }else {
            Log.d(TAG, "file was deleted from cannon")
        }


    }

}