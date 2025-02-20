package com.example.pmm_2eval_trabajo_final

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.view.View
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

        // Botón para volver al menú principal
        val btnHome = findViewById<Button>(R.id.btnHome)
        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Botón para ir a la página de estadísticas
        val btnStatistics = findViewById<Button>(R.id.btnStatistics)
        btnStatistics.setOnClickListener {
            // Si ya estás en MainActivity, no es necesario reiniciarla
            Toast.makeText(this, "Ya estás en la página de inicio", Toast.LENGTH_SHORT).show()
        }

        // Inicializar el gráfico
        pieChart = findViewById(R.id.pieChart)

        pieChart2 = findViewById(R.id.pieChart2)

        // Configurar el gráfico
        setupPieChart(pieChart)
        setupPieChart(pieChart2)

        // Cargar los datos en el gráfico
        loadPieChartData(pieChart, entries)
        loadPieChartData(pieChart2, entries2)

        // Configurar clic en "Ingresos"
        tvIncome.setOnClickListener {
            if (!isIncomeVisible) {
                showIncomeLayout()
                tvIncome.setBackgroundResource(R.color.selected_color)
                tvOutcome.setBackgroundResource(R.color.unselected_color)
            }
        }

        // Configurar clic en "Gastos"
        tvOutcome.setOnClickListener {
            if (isIncomeVisible) {
                showOutcomeLayout()
                tvOutcome.setBackgroundResource(R.color.selected_color)
                tvIncome.setBackgroundResource(R.color.unselected_color)
            }
        }
    }

    private fun setupPieChart(pieChart : PieChart) {
        // Desactivar la descripción
        pieChart.description.isEnabled = false

        // Habilitar el centro vacío para crear un gráfico de anillo
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(Color.TRANSPARENT) // Color del centro
        pieChart.holeRadius = 50f // Radio del agujero central
        pieChart.transparentCircleRadius = 55f // Radio del círculo transparente

        // Configurar la leyenda
        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.isEnabled = true

        // Animación
        pieChart.animateY(1400) // Animación de entrada
    }

    private fun loadPieChartData(pieChart : PieChart, entries : ArrayList<PieEntry>) {

        // Crear un conjunto de datos
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() // Colores predefinidos
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        // Crear los datos del gráfico
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart)) // Mostrar porcentajes

        // Asignar los datos al gráfico
        pieChart.data = data

        // Actualizar el gráfico
        pieChart.invalidate()
    }

    private fun showIncomeLayout() {
        // Animación de traslación para mover el contenedor hacia la derecha
        val translateAnimation = TranslateAnimation(
            -container.width.toFloat() / 2, // Desde la posición actual (fuera de la pantalla)
            0f, // Hasta la posición original
            0f,
            0f
        )
        translateAnimation.duration = 300 // Duración de la animación
        translateAnimation.fillAfter = true // Mantener la posición final después de la animación

        // Aplicar la animación
        container.startAnimation(translateAnimation)

        // Actualizar el estado
        isIncomeVisible = true
    }

    private fun showOutcomeLayout() {
        // Animación de traslación para mover el contenedor hacia la izquierda
        val translateAnimation = TranslateAnimation(
            0f, // Desde la posición actual
            -container.width.toFloat() / 2, // Hasta fuera de la pantalla
            0f,
            0f
        )
        translateAnimation.duration = 300 // Duración de la animación
        translateAnimation.fillAfter = true // Mantener la posición final después de la animación

        // Aplicar la animación
        container.startAnimation(translateAnimation)

        // Actualizar el estado
        isIncomeVisible = false
    }
}