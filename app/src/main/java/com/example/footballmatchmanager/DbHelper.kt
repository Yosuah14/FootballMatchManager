// DbHelper.kt

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.footballmatchmanager.Toques

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "toques.db"
        const val DATABASE_VERSION = 1

        const val TABLE_TOQUES = "toques"
        const val COLUMN_ID = "id"
        const val COLUMN_PUNTOS = "puntos"
        const val COLUMN_CONTADOR = "contador"
        const val COLUMN_ADJETIVO = "adjetivo"
        const val COLUMN_USUARIO = "usuario"

        const val CREATE_TABLE =
            "CREATE TABLE $TABLE_TOQUES ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_PUNTOS INTEGER, $COLUMN_CONTADOR INTEGER, $COLUMN_ADJETIVO TEXT, $COLUMN_USUARIO TEXT)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TOQUES")
        onCreate(db)
    }

    fun databaseExists(): Boolean {
        val db = readableDatabase
        val path = db.path
        db.close()
        return path != null
    }



    fun deleteToques() {
        val db = writableDatabase
        db.delete(TABLE_TOQUES, null, null)
        db.close()
    }

    // Dentro de la clase DbHelper
    fun insertOrUpdateToques(toques: Toques): Long {
        val db = writableDatabase

        val existingRow = queryToques(toques.usuario)

        if (existingRow != null) {
            val values = ContentValues().apply {
                put(COLUMN_PUNTOS, toques.puntos)
                put(COLUMN_CONTADOR, toques.contador)
                put(COLUMN_ADJETIVO, toques.adjetivo)
            }

            db.update(
                TABLE_TOQUES,
                values,
                "$COLUMN_USUARIO = ?",
                arrayOf(existingRow.usuario)
            )
            db.close()

            return existingRow.usuario.hashCode().toLong() // Puedes usar hashCode() del correo como "id"
        } else {
            val values = ContentValues().apply {
                put(COLUMN_PUNTOS, toques.puntos)
                put(COLUMN_CONTADOR, toques.contador)
                put(COLUMN_ADJETIVO, toques.adjetivo)
                put(COLUMN_USUARIO, toques.usuario)
            }

            val id = db.insert(TABLE_TOQUES, null, values)
            db.close()

            return id
        }
    }

    fun queryToques(usuario: String): Toques? {
        val db = readableDatabase
        val columns = arrayOf(COLUMN_PUNTOS, COLUMN_CONTADOR, COLUMN_ADJETIVO)
        val selection = "$COLUMN_USUARIO = ?"
        val selectionArgs = arrayOf(usuario)
        val cursor: Cursor = db.query(TABLE_TOQUES, columns, selection, selectionArgs, null, null, null)

        var toques: Toques? = null

        if (cursor.moveToFirst()) {
            toques = Toques(
                usuario = usuario,
                puntos = cursor.getInt(cursor.getColumnIndex(COLUMN_PUNTOS)),
                contador = cursor.getInt(cursor.getColumnIndex(COLUMN_CONTADOR)),
                adjetivo = cursor.getString(cursor.getColumnIndex(COLUMN_ADJETIVO))
            )
        }

        cursor.close()
        db.close()

        return toques
    }

}

