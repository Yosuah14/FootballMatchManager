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

        binding.btRecuperar.setOnClickListener {
            var roles: ArrayList<Int>

            db.collection("users")
                .document(binding.txtEmail.text.toString())
                .get()
                .addOnSuccessListener { document ->
                    binding.edNombre.setText(document.get("name") as String?)
                    binding.edEdad.setText(document.get("age") as String?)

                    if (document.get("roles") != null) {
                        roles = document.get("roles") as ArrayList<Int>
                        Log.e(TAG, roles.toString())
                        binding.txtRoles.text = roles.toString()
                    } else {
                        Log.e(TAG, "Sin roles")
                        binding.txtRoles.text = "SIN ROLES"
                    }

                    Toast.makeText(this, "Recuperado", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Algo ha ido mal al recuperar", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btEliminar.setOnClickListener {
            db.collection("users")
                .whereEqualTo("email", binding.txtEmail.text.toString())
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection("users")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
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
        }

        binding.btRecuperarTodos.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val querySnapshot = db.collection("users").get().await()

                    val userList = ArrayList<User>() // Asumiendo que tienes una clase User

                    for (document in querySnapshot.documents) {
                        val user = document.toObject(User::class.java)
                        user?.let {
                            userList.add(it)
                        }
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }

                    Log.d(TAG, "Usuarios recuperados: $userList")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al recuperar usuarios: ${e.message}")
                    Toast.makeText(
                        this@MenuPrincipal,
                        "Error al recuperar usuarios",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun irMenuOpciones() {
        val menuOpcionesIntent = Intent(this, MenuOpciones::class.java)
        startActivity(menuOpcionesIntent)
    }
}


