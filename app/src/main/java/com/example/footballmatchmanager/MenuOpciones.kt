package com.example.footballmatchmanager


import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.footballmatchmanager.databinding.ActivityMenuOpcionesBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MenuOpciones : AppCompatActivity() {
    private lateinit var binding: ActivityMenuOpcionesBinding
    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

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

        // Configurar el OnClickListener para el TextView "txtCreadorApp"
        binding.txtCreadorApp.setOnClickListener {
            abrirEnlaceGitHub()
        }

        // Configurar el OnClickListener para el botón "contacto"
        binding.contacto.setOnClickListener {
            enviarCorreo()
        }

        // Configurar el OnClickListener para el botón "cerrarsesionmenuopciones"
        binding.cerrarsesionmenuopciones.setOnClickListener {
            mostrarVentanaConfirmacionCerrarSesion()
        }

    }

    private fun mostrarVentanaConfirmacionCerrarSesion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarSesion() {
        // Cerrar sesión en Firebase y en el proveedor de autenticación de Google
        auth.signOut()
        val signInClient = Identity.getSignInClient(this)
        signInClient.signOut().addOnCompleteListener {
            Log.e(TAG, "Cerrada sesión completamente")
            navigateToLogin()
        }.addOnFailureListener {
            Log.e(TAG, "Hubo algún error al cerrar la sesión")
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        // Navegar a la actividad de inicio de sesión (Login)
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    private fun abrirEnlaceGitHub() {
        // Abrir el enlace del perfil de GitHub en el navegador web
        val url = "https://github.com/Yosuah14"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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



