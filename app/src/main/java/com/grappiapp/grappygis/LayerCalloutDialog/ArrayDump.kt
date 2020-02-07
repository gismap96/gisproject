package com.grappiapp.grappygis.LayerCalloutDialog

object ArrayDump{
    fun getItem(): MutableMap<String, String> {
        val myMap = mutableMapOf<String, String>()
        myMap.put("דור","גלי")
        myMap.put("רן", "טל")
        myMap.put("אופיר", "?")
        myMap.put("אורן", "יד ימין")
        return myMap
    }
}