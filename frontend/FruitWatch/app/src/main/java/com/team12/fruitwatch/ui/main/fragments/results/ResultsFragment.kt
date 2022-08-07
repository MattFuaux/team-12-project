package com.team12.fruitwatch.ui.main.fragments.results

import android.app.Activity.RESULT_OK


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.team12.fruitwatch.R
import com.team12.fruitwatch.controllers.NetworkRequestController

import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.database.entitymanager.PastSearchDb
import com.team12.fruitwatch.databinding.FragmentResultsBinding
import com.team12.fruitwatch.ui.camera.CameraActivity
import com.team12.fruitwatch.ui.dialog.NutritionDialog
import com.team12.fruitwatch.ui.main.MainActivity
import com.team12.fruitwatch.ui.main.fragments.search.PastSearchItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.util.*

class ResultsFragment : Fragment() {

    private val TAG = "ResultsFragment"
    private var _binding: FragmentResultsBinding? = null

    private var pastSearch: PastSearchItemModel? = null
    private lateinit var pricesTblLay: TableLayout
    lateinit var itemImgIV: ImageView
    var itemImgByteArray: ByteArray? = null
    lateinit var resultLayout: LinearLayout
    lateinit var noResultsTV: TextView
    lateinit var predFruitNameTV: TextView
    lateinit var viewNutritionBtn: Button
    private var nutritionDialog: NutritionDialog? = null

    companion object{
        val REQUEST_IMAGE_CAPTURE = 2242
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    val uiScope = CoroutineScope(Dispatchers.Main)

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
        val resultsViewModel =
            ViewModelProvider(this).get(ResultsViewModel::class.java)

        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val startSearchBtn = root.findViewById<Button>(R.id.frag_res_take_pic_btn)
        startSearchBtn.setOnClickListener { dispatchTakePictureIntent() }
        itemImgIV = root.findViewById(R.id.frag_res_item_img)
        resultLayout = root.findViewById(R.id.frag_res_search_result_linlay)
        noResultsTV = root.findViewById(R.id.frag_res_no_results_tv)
        predFruitNameTV = root.findViewById(R.id.frag_res_pred_fruit_name)

        pricesTblLay = root.findViewById(R.id.frag_res_item_prices_tbl)
        viewNutritionBtn = root.findViewById(R.id.frag_res_view_nutrition_btn)
        viewNutritionBtn.setOnClickListener { nutritionDialog?.show() }

        if (requireArguments().containsKey("PAST_SEARCH_REQUEST")){
            pastSearch = requireArguments().getParcelable<PastSearchItemModel>("PAST_SEARCH_REQUEST")
            predFruitNameTV.text = pastSearch!!.itemName
            itemImgIV.setImageBitmap(pastSearch!!.itemImage)
        }else{
            itemImgByteArray = requireArguments().getByteArray("SEARCH_IMAGE")
            itemImgIV.setImageBitmap(BitmapFactory.decodeByteArray(itemImgByteArray,0,itemImgByteArray!!.size))
        }
        val results = requireArguments().getParcelable<NetworkRequestController.SearchResults>("SEARCH_RESULTS")
        showSearchResults(results!!)

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dispatchTakePictureIntent() {
        //val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val takePictureIntent = Intent(activity, CameraActivity::class.java)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Log.d("HomeFragment","Activity Not Found ${e.toString()}")
        }
    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode == RESULT_OK) {
//            val imageFile = getSavedImageFileFromInternalStorage()
//
//            itemImg.setImageBitmap(BitmapFactory.decodeFile(imageFile.absolutePath))
//            // TODO: Add loading wheel while waiting for network activity
//            uiScope.launch {
//                prepareSearchRequestWithImage(imageFile)
//            }
//        }
//        else if (resultCode == CameraActivity.RESULT_FAILED) {
//            Log.d("HomeFragment","No camera attached on device")
//            Toast.makeText(context,"No camera detected on device!",Toast.LENGTH_LONG).show()
//        }
//    }

    private fun getSavedImageFileFromInternalStorage(): File {
        val cw = ContextWrapper(context)
        // path to /data/data/yourapp/app_data/imageDir
        val directory: File = cw.getDir("search_images", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, "image.png")
        return mypath
    }

    private fun showSearchResults(result: NetworkRequestController.SearchResults){
        val fruitName = result.name
        updateSearchResultFields(fruitName)

        if(result.prices != null){
            if (result.prices.isNotEmpty()) {
                noResultsTV.visibility = View.GONE
                pricesTblLay.visibility = View.VISIBLE
                resultLayout.visibility = View.VISIBLE
                loadPricesData(result.prices)
            } else {
                resultLayout.visibility = View.GONE
                pricesTblLay.visibility = View.GONE
                noResultsTV.visibility = View.VISIBLE
            }
        }
        if (result.calories != "") {
            viewNutritionBtn.visibility = View.VISIBLE
            if(pastSearch != null){
                nutritionDialog = NutritionDialog(requireContext(), pastSearch!!.itemImage, result)
            }else{
                nutritionDialog = NutritionDialog(requireContext(), BitmapFactory.decodeByteArray(itemImgByteArray,0,itemImgByteArray!!.size), result)
            }

        } else {
            viewNutritionBtn.visibility = View.GONE
        }
    }

    fun updateSearchResultFields(fruitName: String) {
        predFruitNameTV.visibility = View.VISIBLE
        predFruitNameTV.text = fruitName
    }

    private fun processPrices(resultPrices: List<NetworkRequestController.StorePrice>): Array<PriceData?> {
        val data: Array<PriceData?> = arrayOfNulls(resultPrices.size)
        var count = 0
        for (storePrice in resultPrices) {
            val row = PriceData()
            row.storeName = storePrice.store
            row.price = storePrice.price.toDouble()
            row.unit = storePrice.quantity!!
            row.date = storePrice.date
            data[count] = row
            count += 1
        }
        return data
    }

    private fun loadPricesData(resultPrices: List<NetworkRequestController.StorePrice>?) {
        val horizontalRowMargin = resources.getDimension(R.dimen.margin_sm).toInt()
        val verticalRowPadding = resources.getDimension(R.dimen.padding_sm).toInt()
        val horizontalRowPadding = resources.getDimension(R.dimen.padding_lg).toInt()
        val horizontalEndStorePadding = resources.getDimension(R.dimen.padding_2xl).toInt()

        val storeTextSize = resources.getDimension(R.dimen.font_size_xl).toInt()
        val priceTextSize = resources.getDimension(R.dimen.font_size_lg).toInt()

        val data: Array<PriceData?> = processPrices(resultPrices!!)
        val decimalFormat = DecimalFormat("$0.00")
        val rows = data.size
        pricesTblLay.removeAllViews()

        for (i in 0 until rows) {
            val row: PriceData? = data[i]
            // data columns
            val storeName = TextView(context)
            storeName.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            storeName.setTextSize(TypedValue.COMPLEX_UNIT_PX, storeTextSize.toFloat())
            storeName.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            storeName.gravity = Gravity.CENTER
            storeName.setPadding(
                horizontalRowPadding,
                verticalRowPadding,
                horizontalEndStorePadding,
                verticalRowPadding
            )

            storeName.setBackgroundColor(resources.getColor(R.color.green_100, null))
            storeName.setTextColor(resources.getColor(R.color.black, null))
            storeName.text = row!!.storeName
            val storePrice = TextView(context)
            storePrice.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            storePrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, priceTextSize.toFloat())
            storePrice.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT
            )
            storePrice.gravity = Gravity.CENTER
            storePrice.setPadding(
                horizontalRowPadding,
                verticalRowPadding,
                horizontalRowPadding,
                verticalRowPadding
            )
            storePrice.setTextColor(resources.getColor(R.color.white, null))
            storePrice.setBackgroundColor(resources.getColor(R.color.green_600, null))

            storePrice.text = String.format(
                Locale.getDefault(),
                "%s %s",
                decimalFormat.format(row.price),
                row.unit
            )

            // add table row
            val tableRow = TableRow(context)
            tableRow.gravity = Gravity.CENTER
            tableRow.id = i + 1
            val tableRowParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            tableRowParams.setMargins(
                horizontalRowMargin, 0, horizontalRowMargin,
                0
            )
            tableRow.setPadding(
                horizontalRowPadding,
                verticalRowPadding,
                horizontalRowPadding,
                verticalRowPadding
            )
            tableRow.layoutParams = tableRowParams
            tableRow.addView(storeName)
            tableRow.addView(storePrice)
            pricesTblLay.addView(tableRow, tableRowParams)
        }
    }
}

