package com.grappiapp.grappygis.EmailUpdate

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.grappiapp.grappygis.Consts
import com.grappiapp.grappygis.ProjectRelated.ProjectId
import com.grappiapp.grappygis.R


object EmailUpdateController {
    fun sendUpdateMail(context: Context){
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(Consts.SUPPORT_MAIL))
        val headline = context.getString(R.string.email_update_headline) + " ${ProjectId.projectId}"
        i.putExtra(Intent.EXTRA_SUBJECT, headline)
        var messageBody = context.getString(R.string.email_update_message) + "\n"
        val UID = FirebaseAuth.getInstance().uid
        UID?.let{
            messageBody += context.getString(R.string.serial_number) + " $it"
        }
        i.putExtra(Intent.EXTRA_TEXT,  messageBody)
        try {
            context.startActivity(Intent.createChooser(i, context.getString(R.string.email_update)))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }
}