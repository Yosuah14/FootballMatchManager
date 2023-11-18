package Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.R
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

        fun bind(jugador: JugadorBase) {
            if (jugador is Portero) {
                binding.imageJugador.setImageResource(R.drawable.karius)
            } else {
                binding.imageJugador.setImageResource(R.drawable.pedroleon)
            }

            binding.textViewNombre.text = jugador.nombre
            binding.textViewDetalle.text = "Detalles: ${jugador.posicion}"

            binding.btnAccion.setOnClickListener {
                // Acción a realizar cuando se hace clic en el botón
                // Puedes personalizar esto según lo que necesites
            }
        }
    }
}





