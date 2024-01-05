package com.example.footballmatchmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.footballmatchmanager.databinding.ActivityMenuOpcionesBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MenuOpciones : AppCompatActivity() {
    private lateinit var binding: ActivityMenuOpcionesBinding
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuOpcionesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Ocultar la flecha de retroceso en la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        // Inflar el menú de opciones en el Toolbar
        binding.toolbarmenuprincipal.inflateMenu(R.menu.toolbar)
        // Configurar el listener para los elementos del menú de opciones en el Toolbar
        binding.toolbarmenuprincipal.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_crear_jugador -> {
                    // Manejar la acción del menú "Crear Jugador"
                    val intent = Intent(this@MenuOpciones, CrearJugadores::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_opciones -> {
                    // Manejar la acción del menú "Menu Opciones"
                    Toast.makeText(this, "Opción de menú Opciones", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.jugarbalon -> {
                    // Manejar la acción del menú "Jugar al Balon"
                    val intent = Intent(this@MenuOpciones, JugarBalon::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Opción de jugar al balon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.Preferencias -> {
                    // Manejar la acción del menú "Jugar al Balon"
                    val intent = Intent(this@MenuOpciones, Preferencias::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Opción de preferencias", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.ranking -> {
                    // Manejar la acción del menú "Jugar al Balon"
                    val intent = Intent(this@MenuOpciones, Rankings::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Opción de ranking", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.acercade -> {
                    // Manejar la acción del menú "Jugar al Balon"
                    val intent = Intent(this@MenuOpciones, AcercaDe::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Opción de acerca de", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.crearparrtido -> {
                    // Manejar la acción del menú "Jugar al Balon"
                    val intent = Intent(this@MenuOpciones, CrearPartidos::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Opción de jugar al balon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        binding.Rankingbutton.setOnClickListener {
            val intent = Intent(this@MenuOpciones, Rankings::class.java)
            startActivity(intent)

        }
        // Configurar el OnClickListener para el botón btnCrearJugador
        binding.crearJugador.setOnClickListener {
            // Intent para abrir la actividad CrearJugadores
            val intent = Intent(this@MenuOpciones, CrearJugadores::class.java)
            startActivity(intent)
        }
        binding.btnCrearPartidos.setOnClickListener{
            val intent = Intent(this@MenuOpciones, CrearPartidos::class.java)
            startActivity(intent)
        }

        // Configurar el OnClickListener para el botón crearBalon
        binding.crearBalon.setOnClickListener {
            val intent = Intent(this@MenuOpciones, JugarBalon::class.java)
            startActivity(intent)
        }

        binding.btnAcercaDe.setOnClickListener{
            val intent = Intent(this@MenuOpciones, AcercaDe::class.java)
            startActivity(intent)
        }

        // Configurar el OnClickListener para el botón "contacto"
        binding.contacto.setOnClickListener {
            enviarCorreo()
        }
        binding.Preferencias.setOnClickListener{
            val intent = Intent(this@MenuOpciones, Preferencias::class.java)
            startActivity(intent)
        }
    }
    private fun enviarCorreo() {
        // Crear un intent para enviar un correo electrónico
        val email = "jose14romero2003@gmail.com"
        val subject = "Asunto del correo"
        val message = "Cuerpo del mensaje"

        val uri = Uri.parse("mailto:$email?subject=$subject&body=$message")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(intent)
    }


}





