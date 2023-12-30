package com.example.footballmatchmanager

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        // Configuración de opciones de inicio de sesión de Google
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

        // Botón para registrar un nuevo usuario
        binding.btRegistrar.setOnClickListener {
            if (binding.edEmail.text.isNotEmpty() && binding.edPass.text.isNotEmpty()) {
                firebaseauth.createUserWithEmailAndPassword(
                    binding.edEmail.text.toString(),
                    binding.edPass.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        irMenuPrincipal(it.result?.user?.email ?: "", Proveedor.BASIC)
                        mostrarNotificacionInicioSesion()
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

        // Botón para iniciar sesión
        binding.btLogin.setOnClickListener {
            if (binding.edEmail.text.isNotEmpty() && binding.edPass.text.isNotEmpty()) {
                firebaseauth.signInWithEmailAndPassword(
                    binding.edEmail.text.toString(),
                    binding.edPass.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        mostrarNotificacionInicioSesion()
                        irMenuPrincipal(it.result?.user?.email ?: "", Proveedor.BASIC)
                    } else {
                        showAlert("Credenciales incorrectas")
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Conexión no establecida", Toast.LENGTH_SHORT).show()
                }
            } else {
                showAlert("Rellene los campos")
            }
        }

        // Inicialización de inicio de sesión con Google
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
        mostrarNotificacionInicioSesion()
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
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseauth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                irMenuPrincipal(account.email.toString(), Proveedor.GOOGLE, account.displayName.toString())
            } else {
                Toast.makeText(this, "Error al autenticar con Google", Toast.LENGTH_SHORT).show()
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
    private fun mostrarNotificacionInicioSesion() {
        // Crear un canal de notificación (Obligatorio desde Android 8.0 Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "mi_canal_id"
            val channelName = "Mi Canal"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Descripción del canal"
            }

            // Registrar el canal con el NotificationManager
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Crear un intent para abrir la aplicación al hacer clic en la notificación
        val intent = Intent(this, MenuOpciones::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Crear la notificación
        val notification = NotificationCompat.Builder(this, "mi_canal_id")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("¡Inicio de sesión exitoso!")
            .setContentText("Has iniciado sesión en la aplicación.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        with(NotificationManagerCompat.from(this)) {
            // Verificar si se tiene el permiso necesario
            if (ActivityCompat.checkSelfPermission(
                    this@Login,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Si no se tiene el permiso, solicitarlo en tiempo de ejecución
                ActivityCompat.requestPermissions(
                    this@Login,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // Si se tiene el permiso, mostrar la notificación
                notify(NOTIFICATION_ID, notification)
            }
        }
    }

    // Definir una constante para el código de solicitud de permisos
    private val PERMISSION_REQUEST_CODE = 123

    // Definir una constante para el ID de notificación
    private val NOTIFICATION_ID = 1

    // Sobrescribir el método onRequestPermissionsResult para manejar la respuesta del usuario
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // Verificar si el permiso fue otorgado
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso otorgado, mostrar la notificación
                    mostrarNotificacionInicioSesion()
                } else {
                    // Permiso denegado, puedes manejar esto según tus necesidades
                    Toast.makeText(
                        this,
                        "Permiso de notificación denegado. La notificación no se mostrará.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    private fun irMenuOpciones() {
        val menuOpcionesIntent = Intent(this, MenuOpciones::class.java)
        startActivity(menuOpcionesIntent)
    }
}















