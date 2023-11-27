import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// DbHelper es una clase que extiende SQLiteOpenHelper
class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Companion object para definir constantes relacionadas con la base de datos
    companion object {
        const val DATABASE_NAME = "toques_database"
        const val DATABASE_VERSION = 1

        const val TABLE_TOQUES = "toques"
        const val COLUMN_ID = "id"
        const val COLUMN_USUARIO = "usuario"
        const val COLUMN_PUNTOS = "puntos"
        const val COLUMN_ADJETIVO = "adjetivo"
    }

    // Sentencia SQL para crear la tabla 'toques'
    private val CREATE_TOQUES_TABLE = """
        CREATE TABLE $TABLE_TOQUES (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USUARIO TEXT NOT NULL,
            $COLUMN_PUNTOS INTEGER NOT NULL,
            $COLUMN_ADJETIVO TEXT
        )
    """.trimIndent()

    // Método llamado cuando se crea la base de datos
    override fun onCreate(db: SQLiteDatabase) {
        // Ejecutar la sentencia SQL para crear la tabla 'toques'
        db.execSQL(CREATE_TOQUES_TABLE)
    }

    // Método llamado cuando la versión de la base de datos se actualiza
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Aquí puedes implementar lógica para manejar la actualización de la base de datos
        // (por ejemplo, eliminar tablas existentes y recrearlas)
        // Esto es importante si cambias la estructura de la base de datos en futuras versiones
    }
}






