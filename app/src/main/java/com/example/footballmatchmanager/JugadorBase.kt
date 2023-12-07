package com.example.footballmatchmanager

open class JugadorBase(
    var valoracion: Double = 0.0,
    var nombre: String = "",
    var posicion: String = "",
    var goles: Int? = 0, // Cambiado a Int anulable
    var asistencias: Int? = 0 // Cambiado a Int anulable
)