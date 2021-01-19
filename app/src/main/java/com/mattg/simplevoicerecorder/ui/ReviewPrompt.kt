package com.mattg.simplevoicerecorder.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.mattg.simplevoicerecorder.ui.util.Prefs


class ReviewPrompt(private val context: Context, private val activity: Activity) {

    private val sPrefs = Prefs(context)

    fun promptForReview() {
        val timesOpened = numberOfTimesAppOpened()
        Log.i("REVIEW", "NUMBER OF TIMES OPENED = $timesOpened")
        val reviewPrompted = reviewAlreadyPrompted()
        if (!reviewPrompted && timesOpened > 5) {
            //haven't asked for review, opened more than twice, so ask
            AlertDialog.Builder(context).setTitle("Review")
                .setMessage("If you are enjoying this app, please leave us a review in the Play Store along with any comments or suggestions you may have!")
                .setPositiveButton("Got it"){dialog, which ->
                    dialog.dismiss()
                }.show()

        } else {
            sPrefs.addOpen()
        }
    }

    fun nukePrefs() {
        sPrefs.nukePrefs()
    }


    private fun showReviewDialog(){
        val manager = ReviewManagerFactory.create(activity)
        val request: Task<ReviewInfo> = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            try {
                if (task.isSuccessful()) {
                    // We can get the ReviewInfo object
                    val reviewInfo: ReviewInfo = task.getResult()
                    val flow: Task<Void> = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { task2 ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.

                    }
                } else {
                    // There was some problem, continue regardless of the result.

                }
            } catch (ex: java.lang.Exception) {
                Log.i("ERRORWITHREVIEW", "Error with review flow")
            }
        }
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
              //  Toast.makeText(activity, "There was a problem connecting to Google Play.  A review in the play store would be greatly appreciated!", Toast.LENGTH_SHORT).show()

                setReviewPrompted()
                sPrefs.setHaveAsked()

            }.show()
        //if yes received prompt review
    }

    private fun showPromptForReview(){

    }

//    private fun showReviewDialog() {
//        val manager = ReviewManagerFactory.create(context)
//        val request = manager.requestReviewFlow()
//        request.addOnCompleteListener { reviewRequest ->
//            try {
//                if (reviewRequest.isSuccessful) {
//                    // We got the ReviewInfo object
//                        if(request.result != null) {
//                            val reviewInfo = reviewRequest.result
//                            val flow = manager.launchReviewFlow(activity, reviewInfo)
//                            flow.addOnCompleteListener {
//                                //flow finished, API doesn't provide much here so back to regular flow
//                                val results = it.result
//                                Log.i("Review", "review completed with $results")
//                            }
//                        } else {
//                            Toast.makeText(
//                                activity,
//                                "There was a problem connecting to Google Play.  A review in the play store would be greatly appreciated!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                } else {
//                    // There was some problem, continue regardless of the result.
//                    Log.e("error", "review flow error")
//                }
//            } catch (e: Exception) {
//                Log.e("Error", "Exception $e, message ${e.message}")
//            }
//        }
//
//    }

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