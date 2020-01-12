package com.bgvofir.grappygis.ClientFeatureLayers

class GrappiField (name: String, type: String, alias: String){
    var name = name
    var type = type
    var alias = alias
    var length: Int? = null
    constructor(name: String, type: String, alias: String, length: Int): this(name, type, alias){
        this.length = length
    }
}