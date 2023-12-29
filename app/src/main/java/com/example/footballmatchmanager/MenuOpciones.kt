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

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


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

        binding.btnAcercaDe.setOnClickListener{
            val intent = Intent(this@MenuOpciones, AcercaDe::class.java)
            startActivity(intent)
        }

        // Configurar el OnClickListener para el botón "contacto"
        binding.contacto.setOnClickListener {
            enviarCorreo()
        }

        // Configurar el OnClickListener para el botón "cerrarsesionmenuopciones"
        binding.cerrarsesionmenuopciones.setOnClickListener {
            mostrarVentanaConfirmacionCerrarSesion()

        }
        binding.btnBorrarUsuario.setOnClickListener {
            mostrarVentanaConfirmacionBorrar()
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
    private fun mostrarVentanaConfirmacionBorrar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Borrar Usuario")
            .setMessage("¿Estás seguro de que deseas borrar el usuario?")
            .setPositiveButton("Sí") { _, _ ->
                // Realiza la acción de borrado cuando el usuario confirma
                borrarJugadoresEnFirestore()
                borrarDatosJugadoresPartido()
                borrarUsuarioEnUsersCollection()
                borrarPartidosEnFirestore()
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    private fun navigateToLogin() {
        // Navegar a la actividad de inicio de sesión (Login)
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
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
    private fun borrarJugadoresEnFirestore() {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail).collection("jugadores")

            // Obtener todos los documentos de la colección "jugadores" del usuario actual
            jugadoresCollection.get().addOnSuccessListener { querySnapshot ->
                // Borrar cada documento de la colección
                for (document in querySnapshot.documents) {
                    jugadoresCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("BORRAR_JUGADOR", "Jugador borrado exitosamente de Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("BORRAR_JUGADOR", "Error al borrar jugador de Firestore", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("BORRAR_JUGADOR", "Error al obtener jugadores de Firestore", e)
            }
        }
    }
    private fun borrarPartidosEnFirestore() {
        val currentUserEmail = firebaseAuth.currentUser?.email

        if (currentUserEmail != null) {
            val partidosCollection = db.collection("usuarios").document(currentUserEmail).collection("partidos")

            // Obtener todos los documentos de la colección "partidos" del usuario actual
            partidosCollection.get().addOnSuccessListener { querySnapshot ->
                // Borrar cada documento de la colección
                for (document in querySnapshot.documents) {
                    partidosCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("BORRAR_PARTIDO", "Partido borrado exitosamente de Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("BORRAR_PARTIDO", "Error al borrar partido de Firestore", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("BORRAR_PARTIDO", "Error al obtener partidos de Firestore", e)
            }
        } else {
            Log.e("BORRAR_PARTIDO", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }


    fun borrarUsuarioEnUsersCollection() {
        val currentUserEmail = firebaseAuth.currentUser?.email

        if (currentUserEmail != null) {
            val userDocument = db.collection("users").document(currentUserEmail)

            // Borrar el documento del usuario en Firestore
            userDocument.delete()
                .addOnSuccessListener {
                    Log.d("BORRAR_USUARIO", "Usuario borrado exitosamente de Firestore")
                    // Realizar acciones adicionales después de borrar el usuario si es necesario
                }
                .addOnFailureListener { e ->
                    Log.e("BORRAR_USUARIO", "Error al borrar usuario de Firestore", e)
                }
        } else {
            Log.e("BORRAR_USUARIO", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }
    fun borrarDatosJugadoresPartido() {
        val currentUserEmail = firebaseAuth.currentUser?.email

        if (currentUserEmail != null) {
            val datosJugadoresPartidoCollection = db.collection("usuarios")
                .document(currentUserEmail)
                .collection("datosjugadorespartido")

            // Borrar todos los documentos en la colección "datosjugadorespartido"
            datosJugadoresPartidoCollection.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        datosJugadoresPartidoCollection.document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("BORRAR_DATOS_JUGADORES", "Datos del jugador para el partido borrados exitosamente de Firestore")
                                // Realizar acciones adicionales después de borrar los datos si es necesario
                            }
                            .addOnFailureListener { e ->
                                Log.e("BORRAR_DATOS_JUGADORES", "Error al borrar datos del jugador para el partido de Firestore", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BORRAR_DATOS_JUGADORES", "Error al obtener datos de jugadores para el partido de Firestore", e)
                }
        } else {
            Log.e("BORRAR_DATOS_JUGADORES", "El email del usuario es nulo")
            // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
        }
    }

}





