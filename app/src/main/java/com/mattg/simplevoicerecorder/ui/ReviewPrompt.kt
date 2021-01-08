package com.mattg.simplevoicerecorder.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import com.mattg.simplevoicerecorder.ui.util.Prefs

class ReviewPrompt(private val context: Context, private val activity: Activity) {

    private val sPrefs = Prefs(context)
    fun promptForReview() {
        val timesOpened = numberOfTimesAppOpened()
        val reviewPrompted = reviewAlreadyPrompted()

        if (!reviewPrompted && timesOpened > 2) {
            //haven't asked for review, opened more than twice, so ask
            showReviewPrompt()
        } else {
            sPrefs.addOpen()
        }
    }

    fun nukePrefs() {
        sPrefs.nukePrefs()
    }

    private fun showReviewPrompt() {
        AlertDialog.Builder(context).setTitle("App Experience")
            .setMessage("Are you enjoying the app?")
            .setNegativeButton("no") { dialog, _ ->
                dialog.dismiss()
                sPrefs.setHaveAsked()
            }
            .setPositiveButton("yes") { _, _ ->
                showReviewDialog()
                setReviewPrompted()
                sPrefs.setHaveAsked()
            }.show()
        //if yes received prompt review
    }

    private fun showReviewDialog() {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            try {
                if (request.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        //flow finished, API doesn't provide much here so back to regular flow
                        val results = it.result
                        Log.i("Review", "review completed with $results")
                    }
                } else {
                    // There was some problem, continue regardless of the result.
                    Log.e("error", "review flow error")
                }
            } catch (e: Exception) {
                Log.e("Error", "Exception $e, message ${e.message}")
            }
        }

    }

    private fun setReviewPrompted() {
        sPrefs.setHaveAsked()
    }

    private fun reviewAlreadyPrompted(): Boolean {
        return sPrefs.shouldAskForReview()
    }

    private fun numberOfTimesAppOpened(): Int {
        return sPrefs.getPrefOpens()
    }

}