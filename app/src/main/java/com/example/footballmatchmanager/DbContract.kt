package com.example.footballmatchmanager

import android.provider.BaseColumns

    object DbContract {
        // ToquesEntry es una clase interna que define el contenido de la tabla Toques
        class ToquesEntry : BaseColumns {
            companion object {
                const val TABLE_NAME = "toques"
                const val COLUMN_NAME_ID = "id"
                const val COLUMN_NAME_USUARIO = "usuario"
                const val COLUMN_NAME_PUNTOS = "puntos"
                const val COLUMN_NAME_ADJETIVO = "adjetivo"
            }
        }

        // Aquí puedes agregar otras tablas si las tienes
    }
