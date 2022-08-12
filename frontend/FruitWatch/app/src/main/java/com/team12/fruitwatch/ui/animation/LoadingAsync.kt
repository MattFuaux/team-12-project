package com.team12.fruitwatch.ui.animation

import android.os.AsyncTask


interface LoadingAnimationController {
    fun onStartLoading()
    fun onFinishedLoading()
}

class LoadingAsync(private val listener: LoadingAnimationController) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void?) : Void? {
        for (i in 0 until 10) {
            Thread.sleep(1000)
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

        listener.onFinishedLoading()
    }
}