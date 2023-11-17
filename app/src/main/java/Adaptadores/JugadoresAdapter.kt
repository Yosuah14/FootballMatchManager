package Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.R
import com.example.footballmatchmanager.databinding.RecycleJugadoresBinding

class JugadoresAdapter(private val jugadoresList: List<JugadorBase>) :
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

        fun bind(jugador: JugadorBase) {
            // Configurar la vista del elemento de la lista (imagen, textos y botón)
            binding.imageViewJugador.setImageResource(R.drawable.ic_launcher_foreground) // Asigna la imagen adecuada
            binding.textViewNombre.text = jugador.nombre
            binding.textViewDetalle.text = "Detalles: ${jugador.obtenerTipoJugador()}"
            binding.btnAccion.setOnClickListener {
                // Acción a realizar cuando se hace clic en el botón
                // Puedes personalizar esto según lo que necesites
            }
        }
    }
}


