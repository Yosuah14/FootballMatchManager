package com.example.footballmatchmanager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "toques.db"
        const val DATABASE_VERSION = 1

        const val TABLE_TOQUES = "toques"
        const val COLUMN_ID = "id"
        const val COLUMN_PUNTOS = "puntos"
        const val COLUMN_CONTADOR = "contador"
        const val COLUMN_ADJETIVO = "adjetivo"

        const val CREATE_TABLE =
            "CREATE TABLE $TABLE_TOQUES ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_PUNTOS INTEGER, $COLUMN_CONTADOR INTEGER, $COLUMN_ADJETIVO TEXT)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TOQUES")
        onCreate(db)
    }
}