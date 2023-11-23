package com.example.footballmatchmanager

import DbHelper
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.footballmatchmanager.databinding.ActivityDetail1Binding
import com.google.firebase.auth.FirebaseAuth

class DetailActivity1 : AppCompatActivity() {

    private lateinit var binding: ActivityDetail1Binding
    private lateinit var dbHelper: DbHelper

    private var toques = Toques("", 0, 0, "")
    private lateinit var auth: FirebaseAuth // Agrega esta línea
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetail1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance() // Agrega esta línea
        dbHelper = DbHelper(this)

        if (!dbHelper.databaseExists()) {
            dbHelper.writableDatabase.close()
        }

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

        setSupportActionBar(binding.toolbarCrearJugadores)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCrearJugadores.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun cambiarImagenTemporal() {
        binding.imageButtonBalon.setImageResource(R.drawable.balonnormal)
        toques.contador++
        toques.puntos++
        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"

        handler.postDelayed({
            binding.imageButtonBalon.setImageResource(R.drawable.balonblancoynegro)
        }, 1000)
    }

    private fun cargarDatos() {
        val loadedToques = dbHelper.queryToques(auth.currentUser?.email.orEmpty())

        if (loadedToques != null) {
            toques = loadedToques
        } else {
            toques = Toques(usuario = auth.currentUser?.email.orEmpty(), puntos = 0, contador = 0, adjetivo = "")
            dbHelper.insertOrUpdateToques(toques)
        }

        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"
    }

    private fun guardarDatos() {
        dbHelper.insertOrUpdateToques(toques)
        Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
    }

    private fun borrarDatos() {
        dbHelper.deleteToques()
        Toast.makeText(this, "Datos borrados", Toast.LENGTH_SHORT).show()

        toques = Toques(usuario = auth.currentUser?.email.orEmpty(), puntos = 0, contador = 0, adjetivo = "")
        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"
    }
}

