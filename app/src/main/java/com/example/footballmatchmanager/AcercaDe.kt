package com.example.footballmatchmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.footballmatchmanager.databinding.ActivityAcercaDeBinding

class AcercaDe : AppCompatActivity() {
    private lateinit var binding: ActivityAcercaDeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcercaDeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("AcercaDe", "onCreate: View initialized successfully")

        binding.txtCreadorApp.setOnClickListener {
            Log.d("AcercaDe", "Click on txtCreadorApp")
            abrirEnlaceGitHub()
        }

        setSupportActionBar(binding.toolbarCrearJugadores)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCrearJugadores.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun abrirEnlaceGitHub() {
        Log.d("AcercaDe", "Opening GitHub link")
        // Abrir el enlace del perfil de GitHub en el navegador web
        val url = "https://github.com/Yosuah14"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
