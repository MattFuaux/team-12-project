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
import com.team12.fruitwatch.controllers.NetworkRequestController
import java.text.DecimalFormat
import java.util.*

class NutritionDialog(
    context: Context,
    private var imageBitmap: Bitmap,
    private var resultNutrition: NetworkRequestController.SearchResults
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
        itemIV.setImageBitmap(imageBitmap)
        closeBtn = findViewById(R.id.diag_nutrition_close_btn)
        closeBtn.setOnClickListener { dismiss() }
        infoTbl = findViewById(R.id.diag_nutrition_info_tbl)
        titleTV = findViewById(R.id.diag_nutrition_title)
        titleTV.text = resultNutrition.name
        loadNutritionData(resultNutrition)
    }

    private fun loadNutritionData(resultNutrition: NetworkRequestController.SearchResults) {
        val horizontalRowMargin = context.resources.getDimension(R.dimen.margin_sm).toInt()
        val verticalRowPadding = context.resources.getDimension(R.dimen.padding_sm).toInt()
        val horizontalRowPadding = context.resources.getDimension(R.dimen.padding_lg).toInt()
        val horizontalEndNutritionPadding = context.resources.getDimension(R.dimen.padding_2xl).toInt()
        val horizontalStartValuePadding = context.resources.getDimension(R.dimen.padding_lg).toInt()
        val nutritionTextSize = context.resources.getDimension(R.dimen.font_size_md).toInt()
        val valueTextSize = context.resources.getDimension(R.dimen.font_size_md).toInt()
        val data: Array<NutritionData?> = processNutrition(resultNutrition)
        val decimalFormat = DecimalFormat("0.0")
        val rows = data.size

        infoTbl.removeAllViews()

        for (i in 1 until rows) {
            val row: NutritionData? = data[i]
            if(data[i] != null) {
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

                nutritionTitle.setBackgroundColor(
                    context.resources.getColor(
                        R.color.teal_100,
                        null
                    )
                )
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
                nutritionValue.setBackgroundColor(
                    context.resources.getColor(
                        R.color.teal_600,
                        null
                    )
                )
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
    }


    private fun processNutrition(resultNutrition: NetworkRequestController.SearchResults): Array<NutritionData?> {

        val data: Array<NutritionData?> = arrayOfNulls(11)
        var rowCount = 0
        if(resultNutrition.calories != null) {
            data[rowCount++] =
                createNutritionRow("Calories", resultNutrition.calories.toDouble(), "")
        }
            if(resultNutrition.serving_size_g != null) {
                data[rowCount++] = createNutritionRow(
                    "Serving Size",
                    resultNutrition.serving_size_g.toDouble(),
                    "g"
                )
            }
            if(resultNutrition.protein_g != null) {
                data[rowCount++] =
                    createNutritionRow("Protein", resultNutrition.protein_g.toDouble(), "g")
            }
            if(resultNutrition.carbohydrates_total_g != null) {
                data[rowCount++] = createNutritionRow(
                    "Carbohydrates",
                    resultNutrition.carbohydrates_total_g.toDouble(),
                    "g"
                )
            }
            if(resultNutrition.sugar_g != null) {
                data[rowCount++] =
                    createNutritionRow("Sugar", resultNutrition.sugar_g.toDouble(), "g")
            }
            if(resultNutrition.sodium_mg != null) {
                data[rowCount++] =
                    createNutritionRow("Sodium", resultNutrition.sodium_mg.toDouble(), "mg")
            }
            if(resultNutrition.potassium_mg != null) {
                data[rowCount++] =
                    createNutritionRow("Potassium", resultNutrition.potassium_mg.toDouble(), "mg")
            }
            if(resultNutrition.fat_total_g != null) {
                data[rowCount++] =
                    createNutritionRow("Total Fat", resultNutrition.fat_total_g.toDouble(), "g")
            }
            if(resultNutrition.fat_saturated_g != null) {
                data[rowCount++] = createNutritionRow(
                    "Saturated Fat",
                    resultNutrition.fat_saturated_g.toDouble(),
                    "g"
                )

            }
            if(resultNutrition.cholesterol_mg != null){
            data[rowCount++] = createNutritionRow("Cholesterol",resultNutrition.cholesterol_mg.toDouble(),"mg")
        }
        if(resultNutrition.fiber_g != null) {
            data[rowCount] = createNutritionRow("Fibre", resultNutrition.fiber_g.toDouble(), "g")
        }
        return data
    }

    private fun createNutritionRow(title: String, value:Double, unit:String): NutritionData {
        val row = NutritionData()
        row.nutrition = title
        row.value = value
        row.unit = unit
        return row
    }
}
