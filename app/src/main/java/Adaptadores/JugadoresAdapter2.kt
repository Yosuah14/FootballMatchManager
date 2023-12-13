import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.footballmatchmanager.JugadorBase
import com.example.footballmatchmanager.Jugadores
import com.example.footballmatchmanager.Portero
import com.example.footballmatchmanager.R
import com.example.footballmatchmanager.databinding.RecycleSeleccionarBinding

class JugadoresAdapter2(private val jugadoresList: MutableList<JugadorBase>) :
    RecyclerView.Adapter<JugadoresAdapter2.JugadorViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())

    // Lista para almacenar jugadores seleccionados
    private val jugadoresSeleccionados: MutableList<JugadorBase> = mutableListOf()

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

    // Método para obtener la lista de jugadores seleccionados
    fun getJugadoresSeleccionados(): List<JugadorBase> {
        return jugadoresSeleccionados.toList()
    }

    inner class JugadorViewHolder(private val binding: RecycleSeleccionarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                Toast.makeText(
                    binding.root.context,
                    "Debe seleccionar un jugador",
                    Toast.LENGTH_SHORT
                ).show()
            }

            binding.seleccionar.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val jugador = jugadoresList[adapterPosition]
                    jugadoresSeleccionados.add(jugador)

                    // Hacer que el botón sea invisible después de 0.5 segundos
                    handler.postDelayed({
                        binding.seleccionar.visibility = View.INVISIBLE
                        binding.imageView.visibility=View.VISIBLE
                    }, 500) // 500 milisegundos (0.5 segundos)
                }
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
    }
}





