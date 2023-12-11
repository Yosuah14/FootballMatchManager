package Adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.Partido

import com.example.footballmatchmanager.databinding.RecyclerPartidosBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PartidosAdapter(private val partidosList: MutableList<Partido>) :
    RecyclerView.Adapter<PartidosAdapter.PartidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidoViewHolder {
        val binding =
            RecyclerPartidosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartidoViewHolder, position: Int) {
        holder.bind(partidosList[position])
    }

    override fun getItemCount(): Int {
        return partidosList.size
    }

    inner class PartidoViewHolder(private val binding: RecyclerPartidosBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                    Toast.makeText(
                        binding.root.context,
                        "Debe seleccionar un partido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        fun bind(partido: Partido) {
            binding.textViewFecha.text = "Fecha: ${partido.fecha}"
            binding.textViewHoraInicio.text = "Hora de inicio: ${partido.horaInicio}"
            binding.textViewHoraFin.text = "Hora de fin: ${partido.horaFin}"

            binding.btnVerJugadores.setOnClickListener {
                // Handle the click event for viewing players associated with this match
                // You can add logic to show the list of players here
            }
        }

    }
}

