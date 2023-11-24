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

        firebaseauth = FirebaseAuth.getInstance()

        binding.txtEmail.text = intent.getStringExtra("email").toString()
        binding.txtProveedor.text = intent.getStringExtra("provider").toString()
        binding.txtNombre.text = intent.getStringExtra("nombre").toString()

        binding.btCerrarSesion.setOnClickListener {
            firebaseauth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseauth.signOut()
                    Log.e(TAG, "Cerrada sesión completamente")
                } else {
                    Log.e(TAG, "Hubo algún error al cerrar la sesión")
                }
            }
            firebaseauth.signOut()
            val signInClient = Identity.getSignInClient(this)
            signInClient.signOut()
            Log.e(TAG, "Cerrada sesión completamente")
            finish()
        }

        binding.btVolver.setOnClickListener {
            firebaseauth.signOut()
            finish()
        }
        binding.btGuardar.setOnClickListener {
            val nombre = binding.edNombre.text.toString()
            val edad = binding.edEdad.text.toString()

            if (nombre.isNotEmpty() && edad.isNotEmpty()) {
                val usuario = hashMapOf(
                    "nombre" to nombre,
                    "edad" to edad,
                    "roles" to arrayListOf(1, 2, 3),
                    "timestamp" to FieldValue.serverTimestamp()
                )

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

                irMenuOpciones()
            } else {
                Toast.makeText(this, "Por favor, ingrese nombre y edad", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun irMenuOpciones() {
        val menuOpcionesIntent = Intent(this, MenuOpciones::class.java)
        startActivity(menuOpcionesIntent)
    }
}


