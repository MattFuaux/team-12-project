package com.team12.fruitwatch.ui.animation

import android.app.Activity
import android.view.View
import android.widget.RelativeLayout
import com.airbnb.lottie.LottieAnimationView
import com.team12.fruitwatch.R


// This class managers the View that the loading animation requires, by providing simple start and stop methods for the animation
class LoadingAnimation(private val context: Activity, animationName: String) {

    private var lottieAnimationView : LottieAnimationView =   context.findViewById<LottieAnimationView>(R.id.load_snip_loading_anim) as LottieAnimationView
    private var loadingAnimationLayout : View =   context.findViewById<LottieAnimationView>(R.id.main_activity_loading_anim) as View

    init {
        lottieAnimationView.setAnimation(animationName)
    }

    fun playAnimation() {
        lottieAnimationView.playAnimation()
        loadingAnimationLayout.visibility = View.VISIBLE
    }

    fun stopAnimation() {
        loadingAnimationLayout.visibility = View.INVISIBLE
        lottieAnimationView.cancelAnimation()
    }
}

interface LoadingAnimationController {
    fun onStartLoading()
    fun onFinishedLoading()
}