package com.team12.fruitwatch.controllers


import android.graphics.Bitmap
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpUpload
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.team12.fruitwatch.data.model.LoggedInUser
import com.team12.fruitwatch.ui.main.MainActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


class NetworkRequestController {

    private val logTag = "NetworkRequestController"
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

    class RequestData(var user: LoggedInUser, var imageFileDetails: InputStream,var imgPath: String)

    data class FruitPredictionResponse(
        val name: String,
        val errors: ErrorsResponse?
    ) {

//        data class Fruit(
//            val name: String
//        )

        // If the server prints errors.
        data class ErrorsResponse(val message: String?)

        // Needed for awaitObjectResponse, awaitObject, etc.
        class Deserializer :
            ResponseDeserializable<NetworkRequestController.FruitPredictionResponse> {
            override fun deserialize(content: String): FruitPredictionResponse =
                Gson().fromJson(
                    content,
                    NetworkRequestController.FruitPredictionResponse::class.java
                )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun makeServerRequest(itemImageInputStream: InputStream,imgPath:String): String {
        val request = RequestData(MainActivity.userInfo, itemImageInputStream,imgPath)
        var fruit: String? = null
        var result: String = ""
        runBlocking {
            //val respose = getFruitPredictionResponse(request).data.toString(Charset.defaultCharset())
            val respose = getFruitPrediction(request).component1()!!
            result = respose.name
            Log.d("RequestResults","Results $result ")
        }
        return result
    }

    private suspend fun getFruitPrediction(request: NetworkRequestController.RequestData): Result<NetworkRequestController.FruitPredictionResponse, FuelError> =
        getResults(request).awaitResponseResult(NetworkRequestController.FruitPredictionResponse.Deserializer()).third

    private suspend fun getFruitPredictionResponse(request: NetworkRequestController.RequestData): Response =
        getResults(request).awaitResponseResult(NetworkRequestController.FruitPredictionResponse.Deserializer()).second

    private suspend fun getFruitPredictionRequest(request: NetworkRequestController.RequestData): Request =
        getResults(request).awaitResponseResult(NetworkRequestController.FruitPredictionResponse.Deserializer()).first

    private fun getResults(request: NetworkRequestController.RequestData): Request {
        // Make network/server call here
        val dataPart: DataPart = FileDataPart.from(request.imgPath+"/searchImage.png", name = "myFile")
        return Fuel.upload("http://10.1.1.103:8080/upload",Method.POST).add(dataPart)
        //val requestResults =            TEST_JSON_DATA_RESULTS // Test dummy data is used here, uncomment line above to actually send a request to the server
    }

//    fun encodeToBase64(image: Bitmap): String? {
//        val baos = ByteArrayInputStream(image.ninePatchChunk)
//        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
//        val b: ByteArray = baos.
//
//        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
//        ByteArrayInputStream(input.toByteArray(Charsets.UTF_8))
//        Log.e("LOOK", imageEncoded)
//        return imageEncoded
//    }
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
