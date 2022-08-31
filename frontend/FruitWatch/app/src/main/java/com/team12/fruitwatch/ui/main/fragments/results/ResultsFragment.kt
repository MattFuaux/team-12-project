package com.team12.fruitwatch.ui.main.fragments.results


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.team12.fruitwatch.R
import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.databinding.FragmentResultsBinding
import com.team12.fruitwatch.ui.camera.CameraActivity
import com.team12.fruitwatch.ui.dialog.nutrition.NutritionDialog
import com.team12.fruitwatch.ui.main.MainActivity
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.text.DecimalFormat
import java.util.*


class ResultsFragment : Fragment() {

    private val TAG = "ResultsFragment"
    private var _binding: FragmentResultsBinding? = null
    private var pastSearch: PastSearch? = null
    private lateinit var pricesTblLay: TableLayout
    lateinit var itemImgIV: ImageView
    var itemImgByteArray: ByteArray? = null
    lateinit var resultLayout: LinearLayout
    lateinit var noResultsTV: TextView
    lateinit var predFruitNameTV: TextView
    lateinit var viewNutritionBtn: Button
    private var nutritionDialog: NutritionDialog? = null
    lateinit var viewPastSearchesBtn: Button

    companion object{
        val REQUEST_IMAGE_CAPTURE = 2242
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    class PriceData {
        var storeName: String = String()
        var price: Double = 0.00
        var unit: String = String()
        var date: String = String()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val startSearchBtn = root.findViewById<Button>(R.id.frag_res_take_pic_btn)
        startSearchBtn.setOnClickListener { dispatchTakePictureIntent() }
        itemImgIV = root.findViewById(R.id.frag_res_item_img)
        noResultsTV = root.findViewById(R.id.frag_res_no_results_tv)
        predFruitNameTV = root.findViewById(R.id.frag_res_pred_fruit_name)
        pricesTblLay = root.findViewById(R.id.frag_res_item_prices_tbl)
        viewPastSearchesBtn = root.findViewById(R.id.frag_res_view_past_searches_btn)
        viewPastSearchesBtn.setOnClickListener {(requireActivity() as FragmentDataLink).openSearchFrag()}
        viewNutritionBtn = root.findViewById(R.id.frag_res_view_nutrition_btn)
        viewNutritionBtn.setOnClickListener { nutritionDialog?.show() }
        return root
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelable(MainActivity.SEARCH_RES_KEY,RecentResults.mostRecentSearchResults)
        savedInstanceState.putParcelable(MainActivity.PAST_RES_KEY,RecentResults.mostRecentPastSearch)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (RecentResults.mostRecentPastSearch != null){
            pastSearch = RecentResults.mostRecentPastSearch
            predFruitNameTV.text = pastSearch!!.itemName
            itemImgIV.setImageBitmap(BitmapFactory.decodeByteArray(pastSearch!!.itemImage,0,pastSearch!!.itemImage!!.size))
        }else{
            if (RecentResults.mostRecentSearchResults != null){
                itemImgByteArray = getSavedImageFileFromInternalStorage().readBytes()
                itemImgIV.setImageBitmap(BitmapFactory.decodeByteArray(itemImgByteArray,0,itemImgByteArray!!.size))
            }else{
                itemImgIV.setImageBitmap(null)
            }
        }
        val results = RecentResults.mostRecentSearchResults
        //Log.d(TAG,"Loading Search Results: ${results.toString()}")
        showSearchResults(results)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Starts the activity to take a picture
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(activity, CameraActivity::class.java)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Log.d("HomeFragment","Activity Not Found $e")
        }
    }

    // Creates the file to hold a users captures item
 private  fun getSavedImageFileFromInternalStorage():File{
    val directory: File = File(requireContext().filesDir, "ItemSearchImages")
    if (!directory.exists()) directory.mkdir()
    if (MainActivity.IN_DEVELOPMENT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
        return File(directory, "lime.jpg")
    }else{
        return File(directory, "image.png")
    }
}

    // Takes the search results and displays them on th UI
    private fun showSearchResults(result: NetworkRequestController.SearchResults?){
        if(result != null) {
            val fruitName = result.name
            predFruitNameTV.text = fruitName

            if (result.prices != null) {
                if (result.prices.isNotEmpty()) {
                    noResultsTV.visibility = View.GONE
                    pricesTblLay.visibility = View.VISIBLE
                    loadPricesData(result.prices)
                } else {
                    pricesTblLay.visibility = View.GONE
                    noResultsTV.visibility = View.VISIBLE
                }
            }
            if (result.calories != "") {
                if (pastSearch != null) {
                    nutritionDialog = NutritionDialog(requireContext(),isInDarkMode(), pastSearch!!.itemImage!!, result)
                } else {
                    nutritionDialog = NutritionDialog(requireContext(),isInDarkMode(),itemImgByteArray!!, result)
                }
            }
        }else{
            viewNutritionBtn.visibility = View.INVISIBLE
            noResultsTV.visibility = View.VISIBLE
            noResultsTV.gravity = Gravity.CENTER
            noResultsTV.text = "No results to show, start a 'New Search' to view the results"
        }
    }

    // Converts stores prices from search results to Price Data objects
    private fun processPrices(resultPrices: List<NetworkRequestController.StorePrice>): Array<PriceData?> {
        val data: Array<PriceData?> = arrayOfNulls(resultPrices.size)
        var count = 0
        try {
            for (storePrice in resultPrices) {
                val row = PriceData()
                row.storeName = storePrice.store
                row.price = storePrice.price.toDouble()
                row.unit = storePrice.quantity!!
                row.date = storePrice.date
                data[count] = row
                count += 1
            }
        }catch (ne : NumberFormatException){
            Log.e(TAG,"Number Format Error With Result Prices: $resultPrices")
        }catch (npe : NullPointerException){
            Log.e(TAG,"Null Pointer Error With Result Prices: $resultPrices")
        }
        return data
    }

    // Converts the search results to data rows for the result table
    private fun loadPricesData(resultPrices: List<NetworkRequestController.StorePrice>?) {
        val horizontalRowMargin = resources.getDimension(R.dimen.margin_sm).toInt()
        val verticalRowPadding = resources.getDimension(R.dimen.padding_sm).toInt()
        val horizontalRowPadding = resources.getDimension(R.dimen.padding_lg).toInt()
        val priceTextSize = resources.getDimension(R.dimen.font_size_lg).toInt()

        val data: Array<PriceData?> = processPrices(resultPrices!!)
        val decimalFormat = DecimalFormat("$0.00")
        val rows = data.size
        pricesTblLay.removeAllViews()
        for (i in 0 until rows) {
            val row: PriceData? = data[i]
            // data columns

            val tableIconCellLayoutParams = TableRow.LayoutParams(
                300,
                300
            )
            tableIconCellLayoutParams.gravity = Gravity.START + Gravity.CENTER_VERTICAL
            tableIconCellLayoutParams.setMargins(10)

            val storeIcon = ImageView(context)
            storeIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
            storeIcon.layoutParams = tableIconCellLayoutParams
            storeIcon.setPadding(horizontalRowPadding,horizontalRowPadding,horizontalRowPadding,horizontalRowPadding)
            storeIcon.setBackgroundColor(resources.getColor(R.color.off_white, null))
            when(row!!.storeName.lowercase()){
                "coles" -> {
                    storeIcon.setImageDrawable(resources.getDrawable(R.drawable.coles_logo,null))
                }
                "iga" -> {
                    storeIcon.setImageDrawable(resources.getDrawable(R.drawable.iga_logo,null))
                }
                "woolworths" -> {
                    storeIcon.setImageDrawable(resources.getDrawable(R.drawable.woolworths_logo,null))
                }
            }

            val tablePriceCellLayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            tablePriceCellLayoutParams.setMargins(
                horizontalRowMargin, 0, horizontalRowMargin,
                0
            )
            tablePriceCellLayoutParams.gravity = Gravity.CENTER

            val storePrice = TextView(context)
            storePrice.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            storePrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, priceTextSize.toFloat())
            storePrice.layoutParams = tablePriceCellLayoutParams
            storePrice.gravity = Gravity.CENTER
            storePrice.setPadding(
                horizontalRowPadding,
                verticalRowPadding,
                horizontalRowPadding,
                verticalRowPadding
            )
            storePrice.setTextColor(resources.getColor(R.color.white, null))

            storePrice.text = String.format(
                Locale.getDefault(),
                "%s %s",
                decimalFormat.format(row.price),
                row.unit
            )

            val tableBtnCellLayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            tableBtnCellLayoutParams.setMargins(
                horizontalRowMargin, 0, horizontalRowMargin,
                0
            )
            tableBtnCellLayoutParams.gravity = Gravity.END + Gravity.CENTER_VERTICAL

            val nearbyBtn = Button(context)
            nearbyBtn.setCompoundDrawablesWithIntrinsicBounds(null, ResourcesCompat.getDrawable(resources,R.drawable.ic_baseline_map_24,null),null,null)
            nearbyBtn.text = "Find\nNearby"
            nearbyBtn.gravity = Gravity.CENTER
            nearbyBtn.setTextColor(resources.getColor(R.color.lighttextcolor,null))
            nearbyBtn.layoutParams = tableBtnCellLayoutParams
            nearbyBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, priceTextSize.toFloat()*0.75f)

            nearbyBtn.setOnClickListener {
                val gmmIntentUri: Uri = Uri.parse("geo:0,0?q=${row.storeName}")
                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps")
                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent)
            }

            // add table row
            val tableRow = TableRow(context)
            tableRow.gravity = Gravity.CENTER_HORIZONTAL
            tableRow.id = i + 1
            if(tableRow.id %2 ==0){
                if(isInDarkMode()){
                    tableRow.setBackgroundColor(resources.getColor(R.color.primaryExtraDarkColor,null))
                    nearbyBtn.setBackgroundColor(resources.getColor(R.color.accentExtraDarkColor,null))
                }else{
                    tableRow.setBackgroundColor(resources.getColor(R.color.primaryColor,null))
                    nearbyBtn.setBackgroundColor(resources.getColor(R.color.accentDarkColor,null))
                }
            }else {
                if(isInDarkMode()){
                    tableRow.setBackgroundColor(resources.getColor(R.color.primaryDarkColor,null))
                    nearbyBtn.setBackgroundColor(resources.getColor(R.color.accentDarkColor,null))
                }else{
                    tableRow.setBackgroundColor(resources.getColor(R.color.primaryLightColor, null))
                    nearbyBtn.setBackgroundColor(resources.getColor(R.color.accentColor, null))
                }
            }

            val tableRowLayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            tableRowLayoutParams.gravity = Gravity.CENTER_HORIZONTAL

            tableRow.layoutParams = tableRowLayoutParams
            tableRow.addView(storeIcon)
            tableRow.addView(storePrice)
            tableRow.addView(nearbyBtn)
            pricesTblLay.addView(tableRow, tableRowLayoutParams)
        }
    }

    // Checks if the device currently has 'Dark Mode' enabled
    fun isInDarkMode(): Boolean {
        return requireActivity().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

}

