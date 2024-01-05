package Adaptadores

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.R
import com.example.footballmatchmanager.databinding.DatosjugadorBinding
import com.example.footballmatchmanager.databinding.RecycleJugadoresBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide


class JugadoresAdapter(private val jugadoresList: MutableList<JugadorBase>) :
    RecyclerView.Adapter<JugadoresAdapter.JugadorViewHolder>() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding =
            RecycleJugadoresBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JugadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        holder.bind(jugadoresList[position])
    }

    override fun getItemCount(): Int {
        return jugadoresList.size
    }


    inner class JugadorViewHolder(private val binding: RecycleJugadoresBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // Configurar el clic largo en el botón
            binding.btnmodi.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mostrarDetallesJugador(jugadoresList[adapterPosition])
                } else {
                    Toast.makeText(
                        binding.root.context,
                        "Debes mantener pulsado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true // Indica que se ha manejado el evento
            }
        }
        fun bind(jugador: JugadorBase) {
            //Cargar la imagen
            Glide.with(binding.root.context)
                .load(jugador.imagenUrl)
                .into(binding.imageJugador)
            binding.textViewNombre.text = jugador.nombre
            binding.textViewDetalle.text = "Detalles: ${jugador.posicion}"
            // Configurar el botón "Borrar Jugador"
            binding.btnBorrarJugador.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Llamar al método para borrar el jugador
                    borrarJugador(position)
                }
            }
        }

        private fun mostrarDetallesJugador(jugador: JugadorBase) {
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogBinding = DatosjugadorBinding.inflate(inflater)
            dialogBinding.tvNombre.text = "Nombre: ${jugador.nombre}"
            dialogBinding.tvTipo.text = "Tipo: ${jugador.posicion}"
            dialogBinding.tvValoracion.text = "Mvp: ${jugador.valoracion}"

            when (jugador) {
                is Jugadores -> {
                    dialogBinding.tvGoles.text = "Goles: ${jugador.goles}"
                    dialogBinding.tvAsistencias.text = "Asistencias: ${jugador.asistencias}"
                }
                is Portero -> {
                    dialogBinding.tvGoles.text =  "Goles: ${jugador.goles}"
                    dialogBinding.tvAsistencias.text = "Asistencias: ${jugador.asistencias}"
                }
            }

            val dialogBuilder = AlertDialog.Builder(binding.root.context)
                .setView(dialogBinding.root)
                .setTitle("Detalles del Jugador")

            val btnCerrar = dialogBinding.btnCerrar
            btnCerrar.setOnClickListener {
                dialogBuilder.create().dismiss()
            }

            val dialog = dialogBuilder.create()
            dialog.show()

            btnCerrar.setOnClickListener {
                dialog.dismiss()
                true
            }
        }

        fun borrarJugador(position: Int) {
            val jugadorBorrado = jugadoresList[position]
            val currentUserEmail = firebaseAuth.currentUser?.email

            if (currentUserEmail != null) {
                val jugadoresCollection =
                    db.collection("usuarios").document(currentUserEmail).collection("jugadores")

                jugadoresCollection.whereEqualTo("nombre", jugadorBorrado.nombre).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                // Borrar el documento correspondiente al jugador en Firestore
                                document.reference.delete()
                            }

                            // Borrar el jugador de la lista local
                            jugadoresList.removeAt(position)

                            // Notificar al adaptador sobre el cambio en los datos
                            notifyDataSetChanged()
                        } else {
                            Log.e("Firebase", "Error al obtener los jugadores", task.exception)
                        }
                    }
            } else {
                Log.e("Firebase", "El email del usuario es nulo")
                // Puedes mostrar un mensaje o realizar alguna acción adecuada si el email es nulo
            }
        }
    }
}









