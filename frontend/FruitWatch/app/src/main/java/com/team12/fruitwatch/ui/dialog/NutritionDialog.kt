package com.team12.fruitwatch.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Window
import android.widget.*
import com.team12.fruitwatch.R
import org.json.JSONArray
import java.text.DecimalFormat
import java.util.*

class NutritionDialog(
    context: Context,
    private var itemImg: Bitmap,
    private var resultNutrition: JSONArray
) : Dialog(context) {

    private lateinit var itemIV: ImageView
    private lateinit var infoTbl: TableLayout
    private lateinit var titleTV: TextView
    private lateinit var closeBtn: Button

    class NutritionData {
        var nutrition: String = String()
        var value: Double = 0.00
        var unit: String = String()
    }

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.nutrition_dialog)
        itemIV = findViewById(R.id.diag_nutrition_item_img)
        itemIV.setImageBitmap(itemImg)
        closeBtn = findViewById(R.id.diag_nutrition_close_btn)
        closeBtn.setOnClickListener { dismiss() }
        infoTbl = findViewById(R.id.diag_nutrition_info_tbl)
        titleTV = findViewById(R.id.diag_nutrition_title)
        loadNutritionData(resultNutrition)
    }

    private fun loadNutritionData(resultNutrition: JSONArray) {
        val horizontalRowMargin = context.resources.getDimension(R.dimen.margin_sm).toInt()
        val verticalRowPadding = context.resources.getDimension(R.dimen.padding_sm).toInt()
        val horizontalRowPadding = context.resources.getDimension(R.dimen.padding_lg).toInt()
        val horizontalEndNutritionPadding =
            context.resources.getDimension(R.dimen.padding_2xl).toInt()
        val horizontalStartValuePadding = context.resources.getDimension(R.dimen.padding_lg).toInt()
        val nutritionTextSize = context.resources.getDimension(R.dimen.font_size_lg).toInt()
        val valueTextSize = context.resources.getDimension(R.dimen.font_size_md).toInt()
        val data: Array<NutritionData?> = processNutrition(resultNutrition)
        val decimalFormat = DecimalFormat("0.0")
        val rows = data.size

        infoTbl.removeAllViews()

        for (i in 1 until rows) {
            val row: NutritionData? = data[i]
            // data columns
            val nutritionTitle = TextView(context)
            nutritionTitle.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            nutritionTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, nutritionTextSize.toFloat())
            nutritionTitle.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            nutritionTitle.gravity = Gravity.CENTER
            nutritionTitle.setPadding(
                horizontalRowPadding,
                verticalRowPadding,
                horizontalEndNutritionPadding,
                verticalRowPadding
            )

            nutritionTitle.setBackgroundColor(context.resources.getColor(R.color.teal_100, null))
            nutritionTitle.setTextColor(context.resources.getColor(R.color.black, null))
            nutritionTitle.text = row!!.nutrition
            val nutritionValue = TextView(context)
            nutritionValue.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            nutritionValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, valueTextSize.toFloat())
            nutritionValue.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT
            )
            nutritionValue.gravity = Gravity.CENTER
            nutritionValue.setPadding(
                horizontalStartValuePadding,
                verticalRowPadding,
                horizontalRowPadding,
                verticalRowPadding
            )
            nutritionValue.setTextColor(context.resources.getColor(R.color.white, null))
            nutritionValue.setBackgroundColor(context.resources.getColor(R.color.teal_600, null))
            nutritionValue.text = String.format(
                Locale.getDefault(),
                "%s%s",
                decimalFormat.format(row.value),
                row.unit
            )

            // add table row
            val tblRow = TableRow(context)
            tblRow.gravity = Gravity.CENTER
            tblRow.id = i + 1
            val trParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            trParams.setMargins(
                horizontalRowMargin, 0, horizontalRowMargin,
                0
            )
            tblRow.setPadding(
                horizontalRowPadding,
                verticalRowPadding,
                horizontalRowPadding,
                verticalRowPadding
            )
            tblRow.layoutParams = trParams
            tblRow.addView(nutritionTitle)
            tblRow.addView(nutritionValue)
            infoTbl.addView(tblRow, trParams)
        }
    }


    private fun processNutrition(resultNutrition: JSONArray): Array<NutritionData?> {

        val jsonObject = resultNutrition.getJSONObject(0)
        val jsonIterator: Iterator<String> = jsonObject.keys()
        val data: Array<NutritionData?> = arrayOfNulls(jsonObject.length() - 1)
        var rowCount = 0
        while (jsonIterator.hasNext()) {
            val key = jsonIterator.next()
            if (key == "name") {
                titleTV.text = String.format(
                    Locale.getDefault(),
                    "Nutritional Information for %s",
                    jsonObject.getString("name")
                )
            } else {
                val row = NutritionData()
                if (key.contains("_mg") || key.contains("_g")) {
                    row.nutrition = key.substring(0, key.lastIndexOf('_')).replace("_", " ")
                        .split(" ").joinToString(" ") { it ->
                            it.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }.trimEnd()
                    row.value = jsonObject.getDouble(key)
                    row.unit = key.substring(key.lastIndexOf('_') + 1)
                } else {
                    row.nutrition = key.replace("_", " ")
                        .split(" ").joinToString(" ") { it ->
                            it.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }.trimEnd()
                    row.value = jsonObject.getDouble(key)
                    row.unit = ""
                }

                data[rowCount] = row
                rowCount++
            }
        }
        return data
    }
}
