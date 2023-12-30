package com.example.footballmatchmanager

open class JugadorBase(
    var valoracion: Long = 0L,
    var nombre: String = "",
    var posicion: String = "",
    var goles: Long? = 0,
    var asistencias: Long? = 0,
    val imagenUrl: String? = null
)