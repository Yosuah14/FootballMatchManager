package com.example.footballmatchmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.footballmatchmanager.databinding.ActivityMenuPrincipalBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MenuPrincipal : AppCompatActivity() {
    lateinit var binding: ActivityMenuPrincipalBinding
    private lateinit var firebaseauth: FirebaseAuth
    val TAG = "ACSCO"
    val db = Firebase.firestore
    private var registrado: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la instancia de FirebaseAuth
        firebaseauth = FirebaseAuth.getInstance()

        // Mostrar información del usuario en la interfaz
        binding.txtEmail.text = intent.getStringExtra("email").toString()
        binding.txtProveedor.text = intent.getStringExtra("provider").toString()
        binding.txtNombre.text = intent.getStringExtra("nombre").toString()

        // Configurar el botón "Cerrar Sesión"
        binding.btCerrarSesion.setOnClickListener {
            // Eliminar la cuenta del usuario actual y cerrar sesión
            firebaseauth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseauth.signOut()
                    Log.e(TAG, "Cerrada sesión completamente")
                } else {
                    Log.e(TAG, "Hubo algún error al cerrar la sesión")
                }
            }
            // Cerrar sesión también utilizando el proveedor de autenticación de Google
            firebaseauth.signOut()
            val signInClient = Identity.getSignInClient(this)
            signInClient.signOut()
            Log.e(TAG, "Cerrada sesión completamente")
            finish() // Finalizar la actividad actual
        }

        // Configurar el botón "Volver"
        binding.btVolver.setOnClickListener {
            // Cerrar sesión y finalizar la actividad
            firebaseauth.signOut()
            finish()
        }

        // Configurar el botón "Guardar"
        binding.btGuardar.setOnClickListener {
            val nombre = binding.edNombre.text.toString()
            val edad = binding.edEdad.text.toString()

            if (nombre.isNotEmpty() && edad.isNotEmpty()) {
                // Crear un mapa con la información del usuario
                val usuario = hashMapOf(
                    "nombre" to nombre,
                    "edad" to edad,
                    "roles" to arrayListOf(1, 2, 3),
                    "timestamp" to FieldValue.serverTimestamp()
                )

                // Guardar la información del usuario en Firestore
                db.collection("users")
                    .document(binding.txtEmail.text.toString())
                    .set(usuario)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                    }
                registrado = true // Marcar como registrado al guardar datos

                // Ir a la actividad de Menú de Opciones
                irMenuOpciones()
            } else {
                Toast.makeText(this, "Por favor, ingrese nombre y edad", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para navegar a la actividad de Menú de Opciones
    private fun irMenuOpciones() {
        val menuOpcionesIntent = Intent(this, MenuOpciones::class.java)
        startActivity(menuOpcionesIntent)
    }
}


