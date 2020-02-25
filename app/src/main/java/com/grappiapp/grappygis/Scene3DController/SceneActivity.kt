package com.grappiapp.grappygis.Scene3DController

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esri.arcgisruntime.mapping.view.SceneView
import com.grappiapp.grappygis.R

class SceneActivity : AppCompatActivity() {

    lateinit var sceneView: SceneView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene)
        sceneView = findViewById(R.id.mainSceneView)
        
    }
}
