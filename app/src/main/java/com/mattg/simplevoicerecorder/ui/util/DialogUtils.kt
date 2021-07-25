package com.mattg.simplevoicerecorder.ui.util

import android.app.Application
import android.content.Context
import android.widget.Toast

object DialogUtils {


    fun toastShort(message: String, context: Context){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(message: String, context: Context){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


}