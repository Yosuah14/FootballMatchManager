package Adaptadores

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

class JugadoresAdapter(private val jugadoresList: MutableList<JugadorBase>) :
    RecyclerView.Adapter<JugadoresAdapter.JugadorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding =
            RecycleJugadoresBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JugadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        holder.bind(jugadoresList[position])
    }

    fun actualizarJugadores(nuevaLista: List<JugadorBase>) {
        // Limpiar la lista actual y agregar todos los elementos de la nueva lista
        jugadoresList.clear()
        jugadoresList.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return jugadoresList.size
    }

    inner class JugadorViewHolder(private val binding: RecycleJugadoresBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Configurar el clic largo en el botón
            binding.btnAccion.setOnLongClickListener {
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
            if (jugador is Portero) {
                binding.imageJugador.setImageResource(R.drawable.karius)
            } else {
                binding.imageJugador.setImageResource(R.drawable.pedroleon)
            }

            binding.textViewNombre.text = jugador.nombre
            binding.textViewDetalle.text = "Detalles: ${jugador.posicion}"
        }

        private fun mostrarDetallesJugador(jugador: JugadorBase) {
            // Crear el diálogo con los detalles del jugador
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogBinding = DatosjugadorBinding.inflate(inflater)
            dialogBinding.tvNombre.text = "Nombre: ${jugador.nombre}"
            dialogBinding.tvTipo.text = "Tipo: ${jugador.posicion}"
            dialogBinding.tvValoracion.text = "Valoración: ${jugador.valoracion}"
            dialogBinding.tvGoles.text =
                "Goles: ${if (jugador is Jugadores) jugador.goles else "-"}"
            dialogBinding.tvAsistencias.text =
                "Asistencias: ${if (jugador is Jugadores) jugador.asistencias else "-"}"

            // Configurar el diálogo
            val dialogBuilder = AlertDialog.Builder(binding.root.context)
                .setView(dialogBinding.root)
                .setTitle("Detalles del Jugador")

            // Configurar el botón Cerrar
            val btnCerrar = dialogBinding.btnCerrar
            btnCerrar.setOnClickListener {
                // Cerrar el diálogo al hacer clic en el botón
                dialogBuilder.create().dismiss()
            }

            // Mostrar el diálogo
            val dialog = dialogBuilder.create()
            dialog.show()

            // Añadir el mensaje si no se mantiene pulsado el botón Cerrar
            btnCerrar.setOnClickListener {
                dialog.dismiss()
                true // Indica que se ha manejado el evento
            }
        }
    }
}







