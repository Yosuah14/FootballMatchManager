package com.example.footballmatchmanager

import DbHelper
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

        toques = Toques(usuario = "nombre_de_usuario")
        handler = Handler()
        dbHelper = DbHelper(this)
        toquesRepository = ToquesRepository(this, dbHelper)
        cargarUltimoDatoDesdeBD()

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

        binding.imageButtonBalon.setOnClickListener {
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

    private fun cambiarImagenTemporal() {
        binding.imageButtonBalon.setImageResource(R.drawable.balonnormal)

        // Incrementar los puntos solo cuando se hace clic en el imageButtonBalon
        toques.puntos++
        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"

        // Verificar si se alcanzó una cantidad específica de puntos para cambiar el adjetivo
        when (toques.puntos) {
            10 -> cambiarAdjetivo("Matracas")
            20 -> cambiarAdjetivo("Normal")
            100 -> cambiarAdjetivo("SemiBueno")
            200 -> cambiarAdjetivo("Bueno")
            500 -> cambiarAdjetivo("Buenisimo")
            1000 -> cambiarAdjetivo("Dios")
            1500 -> cambiarAdjetivo("Tifon")
            2000 -> cambiarAdjetivo("Mastodonte")
            200000 -> cambiarAdjetivo("Bufalo")
        }

        handler.postDelayed({
            binding.imageButtonBalon.setImageResource(R.drawable.balonblancoynegro)
        }, 1000)
    }

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
    private fun cambiarAdjetivo(nuevoAdjetivo: String) {
        // Cambiar el adjetivo y guardarlo en la base de datos
        toques.adjetivo = nuevoAdjetivo
        toquesRepository.insertToques(toques)
        Log.d("Toques", "Nuevo Adjetivo: $nuevoAdjetivo")
    }
    private fun borrarDatosUsuario() {
        // Llamada al método para borrar todos los datos del usuario
        toquesRepository.deleteToquesByUsuario("nombre_de_usuario")

        // Restablecer los puntos y el adjetivo en la vista
        toques = Toques(usuario = "nombre_de_usuario")
        binding.textViewPuntos.text = "Puntos: ${toques.puntos}"

        // Log y mensaje informativo
        Log.d("Toques", "Todos los datos del usuario 'nombre_de_usuario' han sido eliminados.")
        // Puedes mostrar un Toast o un mensaje en la interfaz para informar al usuario si es necesario.
    }
}

