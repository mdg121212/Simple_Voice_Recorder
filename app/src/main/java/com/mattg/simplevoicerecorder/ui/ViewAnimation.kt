package com.mattg.simplevoicerecorder.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.BounceInterpolator

class ViewAnimation {

    fun rotateButton(v: View, rotate: Boolean) : Boolean {
        v.animate().setDuration(700)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                }
            })
            .rotation(if (rotate) 360f else 0f)
        return rotate
    }

    fun moveLeft(v: View, translation: Float){
        v.visibility = View.VISIBLE
        v.translationX = 0f
        v.alpha = 0f

         v.animate().translationXBy(-translation)
            .setDuration(350)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)

                }
            })
            .alpha(1f)
            .setInterpolator(BounceInterpolator())
            .start()


    }

    fun moveRight(v: View, translation: Float) {
        v.visibility = View.VISIBLE
       v.alpha = 0f
        v.animate().translationXBy(translation + v.width)
            .setDuration(350)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(1f)
            .setInterpolator(BounceInterpolator())
            .start()
    }

    fun moveBackFromRight(v: View, translation: Float){
        v.animate().translationXBy(-translation - v.width)
            .setDuration(350)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(0f)
            .start()
    }

    fun showIn(v: View){
        v.visibility = View.VISIBLE
        v.alpha = 0f
        v.translationY = -100f
        v.apply {
            animate()
                .translationY(0f)
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        v.visibility = View.GONE
                        super.onAnimationEnd(animation)
                    }
                })
                .alpha(1f)
                .start()
        }
    }

    fun showOut(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 1f
        v.translationY = 0f
        v.animate()
            .setDuration(200)
            .translationY(v.height.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    v.visibility = View.GONE
                    super.onAnimationEnd(animation)
                }
            }).alpha(0f)
            .start()
    }

    fun init(v: View) {
        v.visibility = View.GONE
      //  v.translationY = v.height.toFloat()
        v.alpha = 0f
    }
}