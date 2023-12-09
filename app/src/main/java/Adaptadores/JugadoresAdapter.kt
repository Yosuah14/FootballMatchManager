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

    override fun getItemCount(): Int {
        return jugadoresList.size
    }

    inner class JugadorViewHolder(private val binding: RecycleJugadoresBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
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

            btnCerrar.setOnClickListener {
                dialog.dismiss()
                true
            }
        }
    }
}









