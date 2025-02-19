package com.example.pmm_2eval_trabajo_final

import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class TransactionDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Obtener los datos de la transacción desde el Intent
        val transaction = intent.getParcelableExtra<Transaction>("transaction")
        // Configurar el botón para generar la factura
        val btnGenerateInvoice: Button = findViewById(R.id.btnGenerateInvoice)

        if (transaction != null) {
            findViewById<TextView>(R.id.tvTransactionType).text = "Tipo: ${transaction.type}"
            findViewById<TextView>(R.id.tvTransactionAmount).text = "Cantidad: $${transaction.amount}"
            findViewById<TextView>(R.id.tvTransactionDate).text = "Fecha: ${transaction.date}"
            findViewById<TextView>(R.id.tvTransactionRecipient).text = "Destinatario: ${transaction.to}"
            findViewById<TextView>(R.id.tvTransactionStatus).text = "Estado: ${transaction.status}"
            findViewById<TextView>(R.id.tvTransactionDescription).text = "Descripción: ${transaction.description}"

            btnGenerateInvoice.setOnClickListener {
                generatePDF(transaction)
            }
        } else {
            // Mostrar un mensaje de error si no se encontraron datos
            findViewById<TextView>(R.id.tvTransactionType).text = "Error: No se encontraron detalles de la transacción"
        }

        val goBack = findViewById<ImageView>(R.id.goBack)
        goBack.setOnClickListener{
            finish()
        }

    }

    private fun generatePDF(transaction: Transaction) {
        // Crear un nuevo documento PDF
        val document = PdfDocument()

        // Crear una página
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = document.startPage(pageInfo)

        // Dibujar contenido en la página
        val canvas = page.canvas
        val title = "Factura de Transacción"
        val type = "Tipo: ${transaction.type}"
        val amount = "Cantidad: $${transaction.amount}"
        val date = "Fecha: ${transaction.date}"
        val recipient = "Destinatario: ${transaction.to}"
        val status = "Estado: ${transaction.status}"
        val description = "Descripción: ${transaction.description}"

        val paint = android.graphics.Paint()
        paint.textSize = 12f
        paint.color = android.graphics.Color.BLACK

        canvas.drawText(title, 25f, 50f, paint)
        canvas.drawText(type, 25f, 80f, paint)
        canvas.drawText(amount, 25f, 110f, paint)
        canvas.drawText(date, 25f, 140f, paint)
        canvas.drawText(recipient, 25f, 170f, paint)
        canvas.drawText(status, 25f, 200f, paint)
        canvas.drawText(description, 25f, 230f, paint)

        document.finishPage(page)

        // Guardar el PDF en el almacenamiento externo
        val fileName = "factura_${System.currentTimeMillis()}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            Toast.makeText(this, "Factura generada y guardada en Descargas", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al generar la factura", Toast.LENGTH_SHORT).show()
        }
    }
}