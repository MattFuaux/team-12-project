package com.team12.fruitwatch.controllers

import android.graphics.Bitmap
import com.team12.fruitwatch.data.model.LoggedInUser
import com.team12.fruitwatch.ui.main.MainActivity
import org.json.JSONObject

class NetworkRequestController {

    private val TEST_JSON_DATA_NUTRITIONAL_INFO = "\"items\":[" +
            "{" +
            "\"name\":\"orange\"," +
            "\"calories\": 73," +
            "\"fat_g\": 0.2," +
            "\"sodium_mg\": 13," +
            "\"carbohydrates_g\": 16.5," +
            "\"fiber_g\": 2.8," +
            "\"sugar_g\": 12," +
            "\"protein_g\": 1.3," +
            "\"vitamin_c_mg\": 82.7," +
            "\"potassium_mg\": 232," +
            "\"calcium_mg\": 60.2" +
            "}" +
            "]"

    private val TEST_JSON_DATA_SUPERMARKET_PRICES = "\"stores\":[" +
            "{" +
            "\"name\": \"ALDI\"," +
            "\"price\": 2.32," +
            "\"unit\": \"kg\"" +
            "}," +
            "{" +
            "\"name\": \"Woolworths\"," +
            "\"price\": 2.27," +
            "\"unit\": \"kg\"" +
            "}," +
            "{" +
            "\"name\": \"Coles\"," +
            "\"price\": 2.40," +
            "\"unit\": \"kg\"" +
            "}" +
            "]"

    private val TEST_JSON_DATA_RESULTS = "{\"prices\":{" +
            TEST_JSON_DATA_SUPERMARKET_PRICES +
            "}," +
            "\"nutrition\":{" +
            TEST_JSON_DATA_NUTRITIONAL_INFO +
            "}}"

    class Request(user: LoggedInUser, image: Bitmap)

    fun makeServerRequest(itemImage: Bitmap): JSONObject {
        val request = Request(MainActivity.userInfo, itemImage)
        val jsonString = getResults(request)
        return JSONObject(jsonString)
    }

    private fun getResults(request: Request): String {
        // Make network/server call here
        //val requestResults = okhttpClient.execute(request)
        val requestResults =
            TEST_JSON_DATA_RESULTS // Test dummy data is used here, uncomment line above to actually send a request to the server
        return requestResults
    }

//    override fun onPreExecute() {
//        val activity = activityReference.get()
//        if (activity == null || activity.isFinishing) return
//        activity.progressBar.visibility = View.VISIBLE
//    }
//
//    override fun doInBackground(vararg params: Int?): String? {
//        publishProgress("Sleeping Started") // Calls onProgressUpdate()
//        try {
//            val time = params[0]?.times(1000)
//            time?.toLong()?.let { Thread.sleep(it / 2) }
//            publishProgress("Half Time") // Calls onProgressUpdate()
//            time?.toLong()?.let { Thread.sleep(it / 2) }
//            publishProgress("Sleeping Over") // Calls onProgressUpdate()
//            resp = "Android was sleeping for " + params[0] + " seconds"
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//            resp = e.message
//        } catch (e: Exception) {
//            e.printStackTrace()
//            resp = e.message
//        }
//
//        return resp
//    }
//
//
//    override fun onPostExecute(result: String?) {
//
//        val activity = activityReference.get()
//        if (activity == null || activity.isFinishing) return
//        activity.progressBar.visibility = View.GONE
//        activity.textView.text = result.let { it }
//        activity.myVariable = 100
//    }
//
//    override fun onProgressUpdate(vararg text: String?) {
//
//        val activity = activityReference.get()
//        if (activity == null || activity.isFinishing) return
//
//        Toast.makeText(activity, text.firstOrNull(), Toast.LENGTH_SHORT).show()
//
//    }

}