// DbHelper.kt


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.footballmatchmanager.Toques

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "toques_database"
        const val DATABASE_VERSION = 1

        const val TABLE_TOQUES = "toques"
        const val COLUMN_ID = "id"
        const val COLUMN_USUARIO = "usuario"
        const val COLUMN_PUNTOS = "puntos"
        const val COLUMN_ADJETIVO = "adjetivo"
    }

    // Creación de la tabla
    private val CREATE_TOQUES_TABLE = """
        CREATE TABLE $TABLE_TOQUES (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USUARIO TEXT NOT NULL,
            $COLUMN_PUNTOS INTEGER NOT NULL,
            $COLUMN_ADJETIVO TEXT
        )
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TOQUES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Se puede implementar la actualización de la base de datos si es necesario
    }
}





