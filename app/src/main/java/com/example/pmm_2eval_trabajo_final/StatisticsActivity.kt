package com.example.pmm_2eval_trabajo_final

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

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

        // Configurar el gráfico
        setupPieChart()

        // Cargar los datos en el gráfico
        loadPieChartData()
    }

    private fun setupPieChart() {
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

    private fun loadPieChartData() {
        // Datos proporcionados
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(10086.50f, "Salario"))
        entries.add(PieEntry(3631.90f, "Ingresos pasivos"))
        entries.add(PieEntry(3429.71f, "Freelance"))
        entries.add(PieEntry(3025.49f, "Otros"))

        // Crear un conjunto de datos
        val dataSet = PieDataSet(entries, "Ingresos")
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
}