package com.team12.fruitwatch.ui.main.fragments.home

import android.R.attr.bitmap
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Bundle
import android.provider.MediaStore
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
import com.team12.fruitwatch.databinding.FragmentHomeBinding
import com.team12.fruitwatch.ui.dialog.NutritionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.DecimalFormat
import java.util.*


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val REQUEST_IMAGE_CAPTURE = 2242
    private lateinit var pricesTblLay: TableLayout
    lateinit var itemImg: ImageView
    lateinit var resultLayout: LinearLayout
    lateinit var noResultsTV: TextView
    lateinit var predFruitNameTV: TextView
    lateinit var viewNutritionBtn: Button
    private var nutritionDialog: NutritionDialog? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    val uiScope = CoroutineScope(Dispatchers.Main)

    class PriceData {
        var storeName: String = String()
        var price: Double = 0.00
        var unit: String = String()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val textView: TextView = binding.
        homeViewModel.text.observe(viewLifecycleOwner) {
            //textView.text = it
        }

        val startSearchBtn = root.findViewById<Button>(R.id.frag_home_take_pic_btn)
        startSearchBtn.setOnClickListener { dispatchTakePictureIntent() }
        itemImg = root.findViewById(R.id.frag_home_item_img)
        resultLayout = root.findViewById(R.id.frag_home_search_result_linlay)
        noResultsTV = root.findViewById(R.id.frag_home_no_results_tv)
        predFruitNameTV = root.findViewById(R.id.frag_home_pred_fruit_name)

        pricesTblLay = root.findViewById(R.id.frag_home_item_prices_tbl)
        viewNutritionBtn = root.findViewById(R.id.frag_home_view_nutrition_btn)
        viewNutritionBtn.setOnClickListener { nutritionDialog?.show() }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            itemImg.setImageBitmap(imageBitmap)
            val imgPath = saveToInternalStorage(imageBitmap, "searchImage.png")
            uiScope.launch {
                makeNetworkCall(imageBitmap,imgPath!!)
            }
        }
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap, imageName: String): String? {
        val cw = ContextWrapper(context)
        // path to /data/data/yourapp/app_data/imageDir
        val directory: File = cw.getDir("search_images", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, imageName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    private fun makeNetworkCall(imageBitmap: Bitmap, imgPath:String) {

        GlobalScope.launch(Dispatchers.IO) {

            val bos = ByteArrayOutputStream()
            imageBitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
            val bitmapdata: ByteArray = bos.toByteArray()
            val bs = ByteArrayInputStream(bitmapdata)
            //val result: JSONObject = NetworkRequestController().makeServerRequest(bs,imgPath)
            val result = NetworkRequestController().makeServerRequest(bs,imgPath)

            GlobalScope.launch(Dispatchers.Main) {
//                val resultPrices: JSONArray =
//                    result.getJSONObject("prices").optJSONArray("stores")!!
//                val resultNutrition: JSONArray =
//                    result.getJSONObject("nutrition").optJSONArray("items")!!
                val fruitName = result.trimEnd()
                updateSearchResultFields(fruitName)

//                if (resultPrices.length() > 0) {
//                    noResultsTV.visibility = View.GONE
//                    pricesTblLay.visibility = View.VISIBLE
//                    resultLayout.visibility = View.VISIBLE
//                    loadPricesData(resultPrices)
//                } else {
//                    resultLayout.visibility = View.GONE
//                    pricesTblLay.visibility = View.GONE
//                    noResultsTV.visibility = View.VISIBLE
//                }
//
//                if (resultNutrition.length() > 0) {
//
//                    viewNutritionBtn.visibility = View.VISIBLE
//                    nutritionDialog =
//                        NutritionDialog(requireContext(), imageBitmap, resultNutrition)
//                } else {
//                    viewNutritionBtn.visibility = View.GONE
//                }
            }
        }
    }

    fun updateSearchResultFields(fruitName: String) {
        predFruitNameTV.visibility = View.VISIBLE
        predFruitNameTV.text = fruitName
    }

    private fun processPrices(resultPrices: JSONArray): Array<PriceData?> {
        val data: Array<PriceData?> = arrayOfNulls(resultPrices.length())
        for (i in 0 until resultPrices.length()) {
            val jsonObject = resultPrices.getJSONObject(i)
            val row = PriceData()
            row.storeName = jsonObject.getString("name")
            row.price = jsonObject.getDouble("price")
            row.unit = jsonObject.getString("unit")
            data[i] = row
        }
        return data
    }

    private fun loadPricesData(resultPrices: JSONArray) {
        val horizontalRowMargin = resources.getDimension(R.dimen.margin_sm).toInt()
        val verticalRowPadding = resources.getDimension(R.dimen.padding_sm).toInt()
        val horizontalRowPadding = resources.getDimension(R.dimen.padding_lg).toInt()
        val horizontalEndStorePadding = resources.getDimension(R.dimen.padding_2xl).toInt()

        val storeTextSize = resources.getDimension(R.dimen.font_size_xl).toInt()
        val priceTextSize = resources.getDimension(R.dimen.font_size_lg).toInt()

        val data: Array<PriceData?> = processPrices(resultPrices)
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
                "%s p/%s",
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
