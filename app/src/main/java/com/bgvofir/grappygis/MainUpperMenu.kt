package com.bgvofir.grappygis

object MainUpperMenu{
    private var isItemSelected = false
    private var itemNumber = 0
    /***
     * 0 - nothing selected
     * 1 - measure line
     * 2 - trash
     */

    fun measureLine(): Boolean{
        if (checkIfSelected(1)) {
            resetMenu()
            return false
        }
        changeSelected(1)
        return true
    }
    fun trashClicked(): Boolean{
        if (checkIfSelected(2)) {
            resetMenu()
            return false
        }
        changeSelected(2)
        return true
    }

    fun changeSelected(to:Int){
        itemNumber = to
        isItemSelected = true
    }
    fun resetMenu(){
        isItemSelected = false
        itemNumber = 0
    }

    fun checkIfSelected(menuNum: Int): Boolean{
        return isItemSelected && itemNumber == menuNum
    }
}