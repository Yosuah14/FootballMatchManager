import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.R
import com.example.footballmatchmanager.databinding.RecycleSeleccionarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JugadoresAdapter2(

    private val jugadoresList: MutableList<JugadorBase>,
    var nuevaLista: MutableList<JugadorBase> = mutableListOf(),
    private val onJugadorSeleccionadoListener: OnJugadorSeleccionadoListener? = null ,
    private var selectedImageUri: Uri? = null
) : RecyclerView.Adapter<JugadoresAdapter2.JugadorViewHolder>() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    interface OnJugadorSeleccionadoListener {
        fun onJugadorSeleccionado(jugador: JugadorBase?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding = RecycleSeleccionarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JugadorViewHolder(binding)
    }

    override fun getItemCount(): Int = jugadoresList.size

    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        val jugador = jugadoresList[position]
        holder.bind(jugador)
    }

    fun getJugadoresSeleccionados(): List<JugadorBase> = jugadoresList.toList()

    inner class JugadorViewHolder(private val binding: RecycleSeleccionarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                Log.d("JugadoresAdapter2", "Debe seleccionar un jugador")
            }

            binding.seleccionar.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val jugador = jugadoresList[position]
                    cargarDatosJugador(jugador.nombre) { jugadorCargado ->
                        if (jugadorCargado != null) {
                            nuevaLista.add(jugadorCargado)
                            jugadoresList.remove(jugador) // Elimina el jugador de la lista actual
                            notifyItemRemoved(position)
                            // Notificar al listener sobre el jugador seleccionado
                            onJugadorSeleccionadoListener?.onJugadorSeleccionado(jugadorCargado)


                            Log.d("JugadoresAdapter2", "Jugador seleccionado: $jugadorCargado")
                        } else {
                            Log.e("JugadoresAdapter2", "No se pudo cargar el jugador")
                        }
                    }
                }
            }
        }
        fun bind(jugador: JugadorBase) {

            binding.textViewNombre.text = jugador.nombre
            binding.textViewDetalle.text = "Detalles: ${jugador.posicion}"

            val isSelected = jugadoresList.contains(jugador)
            binding.root.isSelected = isSelected
        }
    }

    private fun cargarDatosJugador(nombreJugador: String, callback: (JugadorBase?) -> Unit) {
        Log.d("JugadoresAdapter2", "Cargando datos del jugador desde Firestore: $nombreJugador")
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail != null) {
            val jugadoresCollection = db.collection("usuarios").document(currentUserEmail)
                .collection("jugadores")

            jugadoresCollection.whereEqualTo("nombre", nombreJugador).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result?.documents

                        if (documents != null && documents.isNotEmpty()) {
                            val document = documents[0]

                            try {
                                val nombre = document.getString("nombre")
                                val valoracion = document.getDouble("valoracion")
                                val posicion = document.getString("posicion")
                                val goles = document.getLong("goles")
                                val asistencias = document.getLong("asistencias")

                                val jugador:
                                        JugadorBase? = when (posicion) {
                                    "Portero" -> Portero(
                                        valoracion!!,
                                        nombre!!,
                                        posicion!!,
                                        goles!!,
                                        asistencias!!,
                                        selectedImageUri.toString()
                                    )
                                    "Jugador Normal" -> Jugadores(
                                        valoracion!!,
                                        nombre!!,
                                        posicion!!,
                                        goles!!,
                                        asistencias!!,
                                        selectedImageUri.toString()
                                    )
                                    else -> null
                                }

                                Log.d("JugadoresAdapter2", "Datos del jugador cargados: $jugador")
                                callback.invoke(jugador)
                            } catch (e: Exception) {
                                Log.e("JugadoresAdapter2", "Error al cargar datos del jugador", e)
                                callback.invoke(null)
                            }
                        } else {
                            Log.d("JugadoresAdapter2", "El documento del jugador no existe")
                            callback.invoke(null)
                        }
                    } else {
                        Log.e("JugadoresAdapter2", "Error al obtener datos del jugador", task.exception)
                        callback.invoke(null)
                    }
                }
        } else {
            Log.e("JugadoresAdapter2", "Usuario no autenticado")
            callback.invoke(null)
        }
    }
}











