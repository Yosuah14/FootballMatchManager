package com.example.footballmatchmanager

import DbHelper
import android.content.ContentValues
import android.content.Context
import android.database.Cursor


class ToquesRepository(context: Context, dbHelper: DbHelper) {

    private val dbHelper = DbHelper(context)

    fun insertToques(toques: Toques) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DbHelper.COLUMN_USUARIO, toques.usuario)
            put(DbHelper.COLUMN_PUNTOS, toques.puntos)
            put(DbHelper.COLUMN_ADJETIVO, toques.adjetivo)
        }
        db.insert(DbHelper.TABLE_TOQUES, null, values)
        db.close()
    }
    fun getUltimoToquesByUsuario(usuario: String): Toques? {
        val db = dbHelper.readableDatabase
        var toques: Toques? = null

        val query = "SELECT * FROM ${DbContract.ToquesEntry.TABLE_NAME} " +
                "WHERE ${DbContract.ToquesEntry.COLUMN_NAME_USUARIO} = ? " +
                "ORDER BY ${DbContract.ToquesEntry.COLUMN_NAME_ID} DESC LIMIT 1"

        val cursor = db.rawQuery(query, arrayOf(usuario))

        try {
            if (cursor.moveToFirst()) {
                toques = Toques(
                    id = cursor.getInt(cursor.getColumnIndex(DbContract.ToquesEntry.COLUMN_NAME_ID)),
                    usuario = cursor.getString(cursor.getColumnIndex(DbContract.ToquesEntry.COLUMN_NAME_USUARIO)),
                    puntos = cursor.getInt(cursor.getColumnIndex(DbContract.ToquesEntry.COLUMN_NAME_PUNTOS)),
                    adjetivo = cursor.getString(cursor.getColumnIndex(DbContract.ToquesEntry.COLUMN_NAME_ADJETIVO))
                )
            }
        } finally {
            cursor.close()
            db.close()
        }

        return toques
    }

    fun getToquesByUsuario(usuario: String): List<Toques> {
        val toquesList = mutableListOf<Toques>()
        val db = dbHelper.readableDatabase
        val columns = arrayOf(
            DbHelper.COLUMN_ID,
            DbHelper.COLUMN_USUARIO,
            DbHelper.COLUMN_PUNTOS,
            DbHelper.COLUMN_ADJETIVO
        )
        val selection = "${DbHelper.COLUMN_USUARIO} = ?"
        val selectionArgs = arrayOf(usuario)
        val cursor: Cursor = db.query(
            DbHelper.TABLE_TOQUES,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_ID))
                val puntos = cursor.getInt(cursor.getColumnIndex(DbHelper.COLUMN_PUNTOS))
                val adjetivo = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_ADJETIVO))

                val toques = Toques(usuario, puntos, id, adjetivo)
                toquesList.add(toques)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return toquesList
    }
    fun deleteToquesByUsuario(usuario: String) {
        val db = dbHelper.writableDatabase
        db.delete(DbHelper.TABLE_TOQUES, "${DbHelper.COLUMN_USUARIO} = ?", arrayOf(usuario))
        db.close()
    }

    // Agrega métodos adicionales según sea necesario, como actualizar o eliminar registros
}

