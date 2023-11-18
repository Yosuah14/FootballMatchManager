package com.example.footballmatchmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.net.Uri
import com.example.footballmatchmanager.databinding.ActivityMenuOpcionesBinding

class MenuOpciones : AppCompatActivity() {
    private lateinit var binding: ActivityMenuOpcionesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuOpcionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el OnClickListener para el botón btnCrearJugador
        binding.btnCrearJugador.setOnClickListener {
            // Intent para abrir la actividad CrearJugadores
            val intent = Intent(this@MenuOpciones, CrearJugadores::class.java)
            startActivity(intent)
        }
        binding.txtCreadorApp.setOnClickListener {
            val url = "https://github.com/Yosuah14"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.contacto.setOnClickListener {
            enviarCorreo()
        }
    }
    private fun enviarCorreo() {
        val email = "jose14romero2003@gmail.com"
        val subject = "Asunto del correo"
        val message = "Cuerpo del mensaje"

        val uri = Uri.parse("mailto:$email?subject=$subject&body=$message")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(intent)
    }
}
