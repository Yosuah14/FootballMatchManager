package Adaptadores

import android.view.LayoutInflater
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
import com.example.footballmatchmanager.databinding.RecycleSeleccionarBinding

class JugadoresAdapter2(private val jugadoresList: MutableList<JugadorBase>) :
    RecyclerView.Adapter<JugadoresAdapter2.JugadorViewHolder>() {

    private val selectedJugadores: MutableList<JugadorBase> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding =
            RecycleSeleccionarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JugadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        holder.bind(jugadoresList[position])
    }

    override fun getItemCount(): Int {
        return jugadoresList.size
    }

    inner class JugadorViewHolder(private val binding: RecycleSeleccionarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnAccion.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    toggleSelection(jugadoresList[adapterPosition])
                    mostrarDetallesJugador(jugadoresList[adapterPosition])
                } else {
                    Toast.makeText(
                        binding.root.context,
                        "Debes mantener pulsado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
        }

        fun bind(jugador: JugadorBase) {
            // Configurar la imagen según el tipo de jugador
            when (jugador) {
                is Jugadores -> {
                    binding.imageJugador.setImageResource(R.drawable.pedroleon)
                }
                is Portero -> {
                    binding.imageJugador.setImageResource(R.drawable.karius)
                }
            }

            binding.textViewNombre.text = jugador.nombre
            binding.textViewDetalle.text = "Detalles: ${jugador.posicion}"

            // Configurar la apariencia de selección
            if (selectedJugadores.contains(jugador)) {
                // Aquí puedes personalizar la apariencia de un jugador seleccionado si es necesario
                // Por ejemplo, cambiar el color de fondo del elemento
                binding.root.setBackgroundResource(R.drawable.karius)
            } else {
                binding.root.setBackgroundResource(0) // Resetear el fondo si no está seleccionado
            }
        }

        private fun toggleSelection(jugador: JugadorBase) {
            // Toggle de la selección del jugador
            if (selectedJugadores.contains(jugador)) {
                selectedJugadores.remove(jugador)
            } else {
                selectedJugadores.add(jugador)
            }
            notifyDataSetChanged()
        }

        private fun mostrarDetallesJugador(jugador: JugadorBase) {
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogBinding = DatosjugadorBinding.inflate(inflater)
            dialogBinding.tvNombre.text = "Nombre: ${jugador.nombre}"
            dialogBinding.tvTipo.text = "Tipo: ${jugador.posicion}"
            dialogBinding.tvValoracion.text = "Valoración: ${jugador.valoracion}"

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
        }
    }

    // Método para obtener la lista de jugadores seleccionados
    fun getSelectedJugadores(): List<JugadorBase> {
        return selectedJugadores.toList()
    }
}

