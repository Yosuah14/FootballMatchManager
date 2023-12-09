package Adaptadores
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.Partido
import com.example.footballmatchmanager.databinding.RecyclerJugadoresBinding
import com.example.footballmatchmanager.databinding.RecyclerPartidosBinding
import com.example.tuapp.databinding.RecyclePartidosBinding
import com.example.FootballMatchManager.databinding.DatosPartidoBinding
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
            binding.btnAccion.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    mostrarDetallesPartido(partidosList[adapterPosition])
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

        fun bind(partido: Partido) {
            binding.textViewNombre.text = "" Hora: ${partido.hora}"
            binding.textViewFecha.text="Fecha: ${partido.fecha}"
            // Puedes personalizar la lógica para configurar otras vistas según las propiedades del partido
            // ...

        }

        private fun isValidFecha(fecha: String): Boolean {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false

            try {
                dateFormat.parse(fecha)
                return true
            } catch (e: ParseException) {
                return false
            }
        }

        private fun isValidHora(hora: String): Boolean {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.isLenient = false

            try {
                timeFormat.parse(hora)
                return true
            } catch (e: ParseException) {
                return false
            }
        }

        private fun mostrarDetallesPartido(partido: Partido) {
            val inflater = LayoutInflater.from(binding.root.context)
            val dialogBinding = DatosPartidoBinding.inflate(inflater)
            dialogBinding.tvFecha.text = "Fecha: ${partido.fecha}"
            dialogBinding.tvHora.text = "Hora: ${partido.hora}"

            val dialogBuilder = AlertDialog.Builder(binding.root.context)
                .setView(dialogBinding.root)
                .setTitle("Detalles del Partido")

            val btnCerrar = dialogBinding.btnCerrar
            btnCerrar.setOnClickListener {
                dialogBuilder.create().dismiss()
            }

            val btnCrear = dialogBinding.btnCrear
            btnCrear.setOnClickListener {
                val fecha = dialogBinding.tvFecha.text.toString()
                val hora = dialogBinding.tvHora.text.toString()

                if (isValidFecha(fecha) && isValidHora(hora)) {
                    // La fecha y hora tienen un formato válido
                    // Puedes agregar la lógica para crear el partido
                    // ...

                    // Cerrar el diálogo después de crear el partido
                    dialogBuilder.create().dismiss()
                } else {
                    // Mostrar un mensaje de error si la fecha o la hora no son válidas
                    Toast.makeText(
                        binding.root.context,
                        "Formato de fecha u hora inválido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
