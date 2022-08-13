package com.team12.fruitwatch.controllers


import android.os.Parcelable
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.gson.jsonBody
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.team12.fruitwatch.data.model.LoggedInUser
import com.team12.fruitwatch.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset


class NetworkRequestController {

    private val logTag = "NetworkRequestController"

    private val URL_PREFIX = "http://"
    private val URL_IP = "54.252.63.41"
    private val URL_PORT = "8080"
    private val URL_REGISTER = "$URL_PREFIX$URL_IP:$URL_PORT/register"
    private val URL_LOGIN = "$URL_PREFIX$URL_IP:$URL_PORT/authenticate"
    private val URL_LOGOUT = "$URL_PREFIX$URL_IP:$URL_PORT/logout"
    private val URL_SEARCH = "$URL_PREFIX$URL_IP:$URL_PORT/search"
    private val URL_TEXT_SEARCH = "$URL_PREFIX$URL_IP:$URL_PORT/search-text"
    private val URL_CHECK_VALID = "$URL_PREFIX$URL_IP:$URL_PORT/check-valid"



    private val TEST_JSON_DATA_NUTRITIONAL_INFO =
            "\"name\":\"Navel Orange\"," +
            "\"calories\": 73," +
            "\"fat_g\": 0.2," +
            "\"sodium_mg\": 13," +
            "\"carbohydrates_g\": 16.5," +
            "\"fiber_g\": 2.8," +
            "\"sugar_g\": 12," +
            "\"protein_g\": 1.3," +
            "\"vitamin_c_mg\": 82.7," +
            "\"potassium_mg\": 232," +
            "\"calcium_mg\": 60.2"

    private val TEST_JSON_DATA_SUPERMARKET_PRICES = "{" +
            "\"store\": \"ALDI\"," +
            "\"price\": 2.32," +
            "\"date\": 2022-06-25," +
            "\"quantity\": \"kg\"" +
            "}," +
            "{" +
            "\"store\": \"Woolworths\"," +
            "\"price\": 2.27," +
            "\"date\": 2022-06-25," +
            "\"quantity\": \"kg\"" +
            "}," +
            "{" +
            "\"store\": \"Coles\"," +
            "\"price\": 2.40," +
            "\"date\": 2022-06-25," +
            "\"quantity\": \"kg\"" +
            "}"


    private val TEST_JSON_DATA_RESULTS = "{\"prices\":[" +
            TEST_JSON_DATA_SUPERMARKET_PRICES +
            "]," +
//            "\"nutrition\":{" +
            TEST_JSON_DATA_NUTRITIONAL_INFO +
//            "}" +
            "}"

    data class ResponseData(
        val statusCode:Int,
        val data: Any?,
        val error: FuelError?
        )

    data class RegistrationDetails(
        @SerializedName("firstName")
        val firstame:String,
        @SerializedName("lastName")
        val surname:String,
        @SerializedName("email")
        val email:String,
        @SerializedName("password")
        val password: String
        )

    data class LoginDetails(
        @SerializedName("email")
        val email:String,
        @SerializedName("password")
        val password:String
        )

    data class TextSearchDetails(
        @SerializedName("fruitName")
        val fruitName:String
    )

    @Parcelize
    data class StorePrice(
        val store:String,
        val price:String,
        val quantity:String?,
        val date:String
        ) : Parcelable {
        override fun toString(): String {
            return "\n ----------------\n store: $store\n price: $price\n quantity: $quantity\n date: $date\n ----------------".trimIndent()
        }
        }

    data class ItemNutrition(
        val name:String?,
        val calories:String?,
        val carbohydrates_total_g:String?,
        val cholesterol_mg:String?,
        val fat_saturated_g:String?,
        val fat_total_g:String?,
        val fiber_g:String?,
        val potassium_mg:String?,
        val protein_g:String?,
        val serving_size_g:String?,
        val sodium_mg:String?,
        val sugar_g:String?
        )
    @Parcelize
    data class SearchResults(
        @SerializedName("prices")
        val prices: List<StorePrice>?,
//        @SerializedName("nutrition")
//        val nutrition: ItemNutrition?
        val name:String,
        val calories:String?,
        val carbohydrates_total_g:String?,
        val cholesterol_mg:String?,
        val fat_saturated_g:String?,
        val fat_total_g:String?,
        val fiber_g:String?,
        val potassium_mg:String?,
        val protein_g:String?,
        val serving_size_g:String?,
        val sodium_mg:String?,
        val sugar_g:String?
        ) : Parcelable {
        override fun toString(): String {
            return "name: $name\nList of Prices:\n $prices\n ======================\n calories: $calories\n carbohydrates_total_g: $carbohydrates_total_g\n cholesterol_mg: $cholesterol_mg\n fat_saturated_g: $fat_saturated_g\n fat_total_g: " +
                    "$fat_total_g\n fiber_g: $fiber_g\n potassium_mg: $potassium_mg\n protein_g: $protein_g\n serving_size_g: $serving_size_g\n sodium_mg: $sodium_mg\n sugar_g: `$sugar_g)".trimIndent()
        }
        }

    fun startSearchWithItemName(loggedInUser: LoggedInUser, itemNameToSearch: String): SearchResults {
        val resultObject : SearchResults
        if(!MainActivity.IN_DEVELOPMENT) {
            // Make network/server call here
            //val parameters = listOf<Pair<String,Any?>>(Pair("itemName",itemNameToSearch))
            val response = Fuel.post(URL_TEXT_SEARCH)
                .header(Headers.COOKIE to loggedInUser.jwt).jsonBody(TextSearchDetails(itemNameToSearch)).response()
            val searchResultsJSON = String(response.second.data, Charset.defaultCharset())
            resultObject = Gson().fromJson(searchResultsJSON,SearchResults::class.java)
        }else{
            val searchResultsJSON = TEST_JSON_DATA_RESULTS // Test dummy data is used here, uncomment line above to actually send a request to the server
            resultObject = Gson().fromJson(searchResultsJSON,SearchResults::class.java)
        }
        return resultObject
    }

    fun startSearchWithImage(loggedInUser: LoggedInUser, imageToPredict: File): SearchResults {
        val resultObject : SearchResults
        if(!MainActivity.IN_DEVELOPMENT) {
            // Make network/server call here
            val dataPart: DataPart = FileDataPart(imageToPredict)
            val response = Fuel.upload(URL_SEARCH, Method.POST).add(dataPart)
                .header(Headers.COOKIE to loggedInUser.jwt).response()
            val searchResultsJSON = String(response.second.data, Charset.defaultCharset())
            resultObject = Gson().fromJson(searchResultsJSON,SearchResults::class.java)
        }else{
            val searchResultsJSON = TEST_JSON_DATA_RESULTS // Test dummy data is used here, uncomment line above to actually send a request to the server
            resultObject = Gson().fromJson(searchResultsJSON,SearchResults::class.java)
        }
        return resultObject
    }

    fun registerUser(firstname:String, surname: String, email:String, password: String): ResponseData {
        // Make network/server call here
        val response = Fuel.post(URL_REGISTER).jsonBody(RegistrationDetails(firstname,surname,email,password)).response()
        return ResponseData(response.second.statusCode,String(response.second.data, Charset.defaultCharset()),response.third.component2())
    }

    fun loginUser(email:String, password: String): ResponseData {
        // Make network/server call here
        val response = Fuel.post(URL_LOGIN).jsonBody(LoginDetails(email,password)).response()
        if(response.second.statusCode == 200 && response.second.header("Set-Cookie").isNotEmpty()){
            val cookieHeader = (response.second.headers["Set-Cookie"] as List<String>)[0]
            val cookie = cookieHeader.split(";")[0]
            val jsonBody = String(response.second.data, Charset.defaultCharset())
            val jsonResponse = JSONObject(jsonBody)
            return ResponseData(response.second.statusCode,LoggedInUser(jsonResponse.get("userID").toString(),jsonResponse.get("firstName").toString()+" "+jsonResponse.get("lastName").toString() ,cookie),response.third.component2())
        }

        return ResponseData(response.second.statusCode,String(response.second.data, Charset.defaultCharset()),response.third.component2())
    }

    fun logoutUser(jwt: String): ResponseData {
        // Make network/server call here
        val response = Fuel.post(URL_LOGOUT).response()
        return ResponseData(response.second.statusCode,String(response.second.data, Charset.defaultCharset()),response.third.component2())
    }

    fun checkIfValid(jwt: String): ResponseData {
        // Make network/server call here
        val json: JsonObject = JsonObject()
        json.addProperty("token",jwt)
        val response = Fuel.post(URL_CHECK_VALID).jsonBody(json).response()
        return ResponseData(response.second.statusCode,String(response.second.data, Charset.defaultCharset()),response.third.component2())
    }
}
