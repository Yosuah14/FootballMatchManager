
package com.example.footballmatchmanager

import DbHelper // Asegúrate de que la importación sea correcta y que DbHelper esté ubicado en el paquete adecuado.
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.footballmatchmanager.databinding.ActivityJugarBalonBinding

class JugarBalon : AppCompatActivity() {
    private lateinit var binding: ActivityJugarBalonBinding
    private lateinit var toques: Toques
    private lateinit var handler: Handler
    private lateinit var dbHelper: DbHelper
    private lateinit var toquesRepository: ToquesRepository
    private var ultimoToquesCargado: Toques? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJugarBalonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de instancias y configuración de la interfaz
        toques = Toques(usuario = "nombre_de_usuario")
        handler = Handler()
        dbHelper = DbHelper(this)
        toquesRepository = ToquesRepository(this, dbHelper)
        cargarUltimoDatoDesdeBD()

        // Configuración de botones y toolbar
        binding.btnGuardar.setOnClickListener {
            // Ejemplo de cómo insertar o actualizar datos en la base de datos
            toquesRepository.insertToques(toques)

            // Ejemplo de cómo obtener datos de la base de datos
            val toquesList = toquesRepository.getToquesByUsuario("nombre_de_usuario")
            for (toques in toquesList) {
                Log.d("Toques", "ID: ${toques.id}, Puntos: ${toques.puntos}, Adjetivo: ${toques.adjetivo}")
            }

            ultimoToquesCargado = toquesRepository.getUltimoToquesByUsuario("nombre_de_usuario")
        }

        binding.crearJugador.setOnClickListener {
            cambiarImagenTemporal()
        }

        binding.btnBorrar.setOnClickListener {
            borrarDatosUsuario()
        }

        setSupportActionBar(binding.toolbarCrearJugadores)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCrearJugadores.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    // Método para cambiar la imagen temporal y actualizar puntos
    private fun cambiarImagenTemporal() {
        // Cambiar la imagen a blanco y negro temporalmente
        binding.crearJugador.setImageResource(R.drawable.balonnormal)

        // Incrementar los puntos solo cuando se hace clic en el imageButtonBalon
        toques.puntos++
        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"

        // Restaurar la imagen a la original después de un breve tiempo
        handler.postDelayed({
            binding.crearJugador.setImageResource(R.drawable.balonblancoynegro)
        }, 1000)
    }

    // Método para cargar el último dato desde la base de datos
    private fun cargarUltimoDatoDesdeBD() {
        ultimoToquesCargado = toquesRepository.getUltimoToquesByUsuario("nombre_de_usuario")

        if (ultimoToquesCargado != null) {
            Log.d("Toques", "ID: ${ultimoToquesCargado!!.id}, Puntos: ${ultimoToquesCargado!!.puntos}, Adjetivo: ${ultimoToquesCargado!!.adjetivo}")
            binding.textViewPuntos.text = "Puntos: ${ultimoToquesCargado!!.puntos}"
            toques = ultimoToquesCargado!!
        } else {
            Log.d("Toques", "No se encontraron datos para el usuario 'nombre_de_usuario'")
        }
    }

    // Método para cambiar el adjetivo y guardarlo en la base de datos
    private fun cambiarAdjetivo(nuevoAdjetivo: String) {
        toques.adjetivo = nuevoAdjetivo
        toquesRepository.insertToques(toques)
        Log.d("Toques", "Nuevo Adjetivo: $nuevoAdjetivo")
    }

    // Método para borrar todos los datos del usuario
    private fun borrarDatosUsuario() {
        toquesRepository.deleteToquesByUsuario("nombre_de_usuario")

        // Restablecer los puntos y el adjetivo en la vista
        toques = Toques(usuario = "nombre_de_usuario")
        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"

        // Log y mensaje informativo
        Log.d("Toques", "Todos los datos del usuario 'nombre_de_usuario' han sido eliminados.")
        // Puedes mostrar un Toast o un mensaje en la interfaz para informar al usuario si es necesario.
    }
}
