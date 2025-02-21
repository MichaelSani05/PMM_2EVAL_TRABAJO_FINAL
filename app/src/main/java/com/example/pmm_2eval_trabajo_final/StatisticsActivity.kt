package com.example.pmm_2eval_trabajo_final

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class StatisticsActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var pieChart2: PieChart
    private var isIncomeVisible = true
    private lateinit var incomeLayout : LinearLayout
    private lateinit var outcomeLayout : LinearLayout
    private lateinit var container : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val tvIncome = findViewById<TextView>(R.id.tvIncome)
        val tvOutcome = findViewById<TextView>(R.id.tvOutcome)
        incomeLayout = findViewById(R.id.income)
        outcomeLayout = findViewById(R.id.outcome)
        container = findViewById(R.id.container)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(10086.50f, "Salario"))
        entries.add(PieEntry(3631.90f, "Ingresos pasivos"))
        entries.add(PieEntry(3429.71f, "Freelance"))
        entries.add(PieEntry(3025.49f, "Otros"))

        val entries2 = ArrayList<PieEntry>()
        entries2.add(PieEntry(567.85f, "Hogar"))
        entries2.add(PieEntry(631.90f, "Transporte/Viajes"))
        entries2.add(PieEntry(239.71f, "Comida"))
        entries2.add(PieEntry(125.74f, "Otros"))

        val btnHome = findViewById<Button>(R.id.btnHome)
        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val btnStatistics = findViewById<Button>(R.id.btnStatistics)
        btnStatistics.setOnClickListener {
            Toast.makeText(this, "Ya estás en la página de stats", Toast.LENGTH_SHORT).show()
        }

        val btnPagos = findViewById<Button>(R.id.btnPagos)
        btnPagos.setOnClickListener {
            startActivity(Intent(this, ScheduledPaymentsActivity::class.java))
        }


        pieChart = findViewById(R.id.pieChart)

        pieChart2 = findViewById(R.id.pieChart2)

        setupPieChart(pieChart)
        setupPieChart(pieChart2)

        loadPieChartData(pieChart, entries)
        loadPieChartData(pieChart2, entries2)

        tvIncome.setOnClickListener {
            if (!isIncomeVisible) {
                showIncomeLayout()
                tvIncome.setBackgroundResource(R.color.selected_color)
                tvOutcome.setBackgroundResource(R.color.unselected_color)
            }
        }

        tvOutcome.setOnClickListener {
            if (isIncomeVisible) {
                showOutcomeLayout()
                tvOutcome.setBackgroundResource(R.color.selected_color)
                tvIncome.setBackgroundResource(R.color.unselected_color)
            }
        }
    }

    private fun setupPieChart(pieChart : PieChart) {
        pieChart.description.isEnabled = false

        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.holeRadius = 50f
        pieChart.transparentCircleRadius = 55f

        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.isEnabled = true

        pieChart.animateY(1400)
    }

    private fun loadPieChartData(pieChart : PieChart, entries : ArrayList<PieEntry>) {

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = data

        pieChart.invalidate()
    }

    private fun showIncomeLayout() {
        val translateAnimation = TranslateAnimation(
            -container.width.toFloat() / 2,
            0f,
            0f,
            0f
        )
        translateAnimation.duration = 300
        translateAnimation.fillAfter = true

        container.startAnimation(translateAnimation)

        isIncomeVisible = true
    }

    private fun showOutcomeLayout() {
        val translateAnimation = TranslateAnimation(
            0f,
            -container.width.toFloat() / 2,
            0f,
            0f
        )
        translateAnimation.duration = 300
        translateAnimation.fillAfter = true

        container.startAnimation(translateAnimation)

        isIncomeVisible = false
    }
}