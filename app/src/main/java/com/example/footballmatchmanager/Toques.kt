package com.example.footballmatchmanager

// Clase toques que se almacenara en una base de datos

data class Toques(
    val usuario: String,
    var puntos: Int = 0,
    var id: Int = 0,
    var adjetivo: String = ""
)
