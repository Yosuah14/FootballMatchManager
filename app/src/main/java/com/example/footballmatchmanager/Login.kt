package com.example.footballmatchmanager

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.footballmatchmanager.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Login : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseauth: FirebaseAuth
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseauth = FirebaseAuth.getInstance()


        // Verificar si ya hay una sesión iniciada
        if (firebaseauth.currentUser != null) {
            // Si hay una sesión iniciada, ir directamente al menú de opciones
            irMenuOpciones()
            finish()  // Cerrar esta actividad para evitar volver atrás
        }

        binding.btRegistrar.setOnClickListener {
            if (binding.edEmail.text.isNotEmpty() && binding.edPass.text.isNotEmpty()) {
                firebaseauth.createUserWithEmailAndPassword(
                    binding.edEmail.text.toString(),
                    binding.edPass.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        irMenuPrincipal(it.result?.user?.email ?: "", Proveedor.BASIC)
                    } else {
                        showAlert("Error registrando al usuario.")
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Conexión no establecida", Toast.LENGTH_SHORT).show()
                }
            } else {
                showAlert("Rellene los campos")
            }
        }

        binding.btLogin.setOnClickListener {
            if (binding.edEmail.text.isNotEmpty() && binding.edPass.text.isNotEmpty()) {
                firebaseauth.signInWithEmailAndPassword(
                    binding.edEmail.text.toString(),
                    binding.edPass.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        irMenuPrincipal(it.result?.user?.email ?: "", Proveedor.BASIC)
                    } else {
                        showAlert()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Conexión no establecida", Toast.LENGTH_SHORT).show()
                }
            } else {
                showAlert("Rellene los campos")
            }
        }

        firebaseauth.signOut()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btGoogle.setOnClickListener {
            loginEnGoogle()
        }
    }

    private fun loginEnGoogle() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        val signInClient = googleSignInClient.signInIntent
        launcherVentanaGoogle.launch(signInClient)
    }

    private val launcherVentanaGoogle =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                manejarResultados(task)
            }
        }

    private fun manejarResultados(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                actualizarUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseauth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                irMenuPrincipal(account.email.toString(), Proveedor.GOOGLE, account.displayName.toString())
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAlert(msg: String = "Se ha producido un error autenticando al usuario") {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun irMenuPrincipal(email: String, provider: Proveedor, nombre: String = "Usuario") {
        val menuPrincipalIntent = Intent(this, MenuPrincipal::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("nombre", nombre)
        }
        startActivity(menuPrincipalIntent)
    }
    private fun verificarUsuarioEnFirestore(email: String) {
        lifecycleScope.launch {
            try {
                val querySnapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    // Si hay un usuario registrado con información en Firestore, ir a MenuOpciones
                    val menuOpcionesIntent = Intent(this@Login, MenuOpciones::class.java)
                    startActivity(menuOpcionesIntent)
                    finish() // Para cerrar la actividad actual
                } else {
                    // Si no hay información asociada, mostrar mensaje o realizar otras acciones
                    Toast.makeText(
                        this@Login,
                        "Usuario sin información asociada.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error al verificar usuario en Firestore: ${e.message}")
            }
        }

    }
    private fun irMenuOpciones() {
        val menuOpcionesIntent = Intent(this, MenuOpciones::class.java)
        startActivity(menuOpcionesIntent)
    }

}














