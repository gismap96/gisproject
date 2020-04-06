package com.grappiapp.grappygis.ClientLayerPhotoController

import com.grappiapp.grappygis.R

enum class ImageSourcesEnum (val title: Int) {
    CAMERA(R.string.camera),
    GALLERY(R.string.image_gallery),
    NO_IMAGE(R.string.no_image);
    fun getImageAddress(): Int{
        return when (this){
            CAMERA -> R.drawable.ic_add_photo
            GALLERY -> R.drawable.ic_add_photo
            NO_IMAGE -> R.drawable.ic_add_photo
        }
    }
}