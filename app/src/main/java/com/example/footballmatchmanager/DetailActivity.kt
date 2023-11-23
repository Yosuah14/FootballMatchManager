package com.example.footballmatchmanager

// DetailActivity.kt
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.databindingexample.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var dbHelper: DbHelper

    private var toques = Toques()
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DbHelper(this)
        cargarDatos()

        binding.imageButtonBalon.setOnClickListener {
            cambiarImagenTemporal()
        }

        binding.btnGuardar.setOnClickListener {
            guardarDatos()
        }

        binding.btnBorrar.setOnClickListener {
            borrarDatos()
        }
    }

    private fun cambiarImagenTemporal() {
        binding.imageButtonBalon.setImageResource(R.drawable.balonnormal)
        toques.contador++
        toques.puntos++
        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"

        handler.postDelayed({
            binding.imageButtonBalon.setImageResource(R.drawable.balonblancoynegro)
        }, 3000)
    }

    private fun guardarDatos() {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DbHelper.COLUMN_PUNTOS, toques.puntos)
            put(DbHelper.COLUMN_CONTADOR, toques.contador)
            put(DbHelper.COLUMN_ADJETIVO, toques.adjetivo)
        }

        val id = db.insert(DbHelper.TABLE_TOQUES, null, values)
        if (id != -1L) {
            Toast.makeText(this, "Datos guardados con ID: $id", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al guardar datos", Toast.LENGTH_SHORT).show()
        }

        db.close()
    }

    private fun borrarDatos() {
        val db = dbHelper.writableDatabase
        db.delete(DbHelper.TABLE_TOQUES, null, null)
        Toast.makeText(this, "Datos borrados", Toast.LENGTH_SHORT).show()

        // Reinicia los valores
        toques = toques()
        bindin.text = "Puntos: ${toques.puntos}"
        binding.

        db.close()
    }

    private fun cargarDatos() {
        val db = dbHelper.readableDatabase
        val columns = arrayOf(DbHelper.COLUMN_PUNTOS, DbHelper.COLUMN_CONTADOR, DbHelper.COLUMN_ADJETIVO)
        val cursor = db.query(DbHelper.TABLE_TOQUES, columns, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            toques = Toques(
                puntos = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_PUNTOS)),
                contador = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_CONTADOR)),
                adjetivo = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ADJETIVO))
            )

            binding.textViewPuntos.text = "Puntos: ${toques.puntos}"
            // También puedes mostrar el adjetivo en tu TextView correspondiente
        }

        cursor.close()
        db.close()
    }
}
