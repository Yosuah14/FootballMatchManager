package com.example.footballmatchmanager

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.footballmatchmanager.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var firebaseauth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseauth = FirebaseAuth.getInstance()
        //------------------------------ Autenticación con email y password ------------------------------------
        binding.btRegistrar.setOnClickListener {
            if (binding.edEmail.text.isNotEmpty() && binding.edPass.text.isNotEmpty()) {
                firebaseauth.createUserWithEmailAndPassword(
                    binding.edEmail.text.toString(),
                    binding.edPass.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        irHome(
                            it.result?.user?.email ?: "",
                            Proveedor.BASIC
                        )  //Esto de los interrogantes es por si está vacío el email, que enviaría una cadena vacía.
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
                        irHome(
                            it.result?.user?.email ?: "",
                            Proveedor.BASIC
                        )  //Esto de los interrogantes es por si está vacío el email.
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


        //------------------ Login Google -------------------
        //------------------------------- -Autenticación Google --------------------------------------------------
        //se hace un signOut por si había algún login antes.
        firebaseauth.signOut()
        //clearGooglePlayServicesCache()


        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btGoogle.setOnClickListener {
            loginEnGoogle()
        }


    }

    private fun loginEnGoogle(){
        //este método es nuestro.
        val signInClient = googleSignInClient.signInIntent
        launcherVentanaGoogle.launch(signInClient)
        //milauncherVentanaGoogle.launch(signInClient)
    }


    //con este launcher, abro la ventana que me lleva a la validacion de Google.
    private val launcherVentanaGoogle =  registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        //si la ventana va bien, se accede a las propiedades que trae la propia ventana q llamamos y recogemos en result.
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            manejarResultados(task)
        }
    }

    //es como una muñeca rusa, vamos desgranando, de la ventana a task y de task a los datos concretos que me da google.
    private fun manejarResultados(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                actualizarUI(account)
            }
        }
        else {
            Toast.makeText(this,task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    //esta funcion actualiza o repinta la interfaz de usuario UI.
    private fun actualizarUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        //pido un token, y con ese token, si todo va bien obtengo la info.
        firebaseauth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                //hacer account. y ver otras propiedades interesantes.
                irHome(account.email.toString(),Proveedor.GOOGLE, account.displayName.toString())
            }
            else {
                Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
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

    private fun irHome(email: String, provider: Proveedor, nombre: String = "Usuario") {
        Log.e(TAG, "Valores: ${email}, ${provider}, ${nombre}")
        val homeIntent = Intent(this, MenuPrincipal::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("nombre", nombre)
        }
        startActivity(homeIntent)
    }




}






