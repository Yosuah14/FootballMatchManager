package com.example.footballmatchmanager

open class JugadorBase(
    var valoracion: Double = 0.0,
    var nombre: String = "",
    var posicion: String = "",
    var goles: Long? = 0,
    var asistencias: Long? = 0,
    val imagenUrl: String? = null
)