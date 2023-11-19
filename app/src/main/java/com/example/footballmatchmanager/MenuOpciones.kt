package com.example.footballmatchmanager

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
import com.google.firebase.firestore.FirebaseFirestore

class MenuOpciones : AppCompatActivity() {
    private lateinit var binding: ActivityMenuOpcionesBinding
    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuOpcionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    // Agrega aquí el código para abrir el menú de opciones
                    Toast.makeText(this, "Opción de menú Opciones", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }


        // Configurar el OnClickListener para el botón btnCrearJugador
        binding.btnCrearJugador.setOnClickListener {
            // Intent para abrir la actividad CrearJugadores
            val intent = Intent(this@MenuOpciones, CrearJugadores::class.java)
            startActivity(intent)
        }

        binding.txtCreadorApp.setOnClickListener {
            abrirEnlaceGitHub()
        }

        binding.contacto.setOnClickListener {
            enviarCorreo()
        }

        binding.cerrarsesionmenuopciones.setOnClickListener {
            mostrarVentanaConfirmacionCerrarSesion()
        }

        binding.borrarusuario.setOnClickListener {
            eliminarUsuario()
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

    private fun eliminarUsuario() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("users")
                .whereEqualTo("email", currentUser.email)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection("users")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                                navigateMenuPrincipal()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Error al eliminar el documento",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "No se ha encontrado el documento a eliminar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Manejar el caso en el que el usuario no esté autenticado
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateMenuPrincipal() {
        val intent = Intent(this, MenuPrincipal::class.java)
        startActivity(intent)
        finish()
    }

    private fun abrirEnlaceGitHub() {
        val url = "https://github.com/Yosuah14"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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



