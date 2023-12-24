package com.example.footballmatchmanager
class Portero(
    valoracion: Double,
    nombre: String,
    posicion: String,
    goles: Long = 0,
    asistencias: Long = 0,
    imagenUrl: String? = null
) : JugadorBase(valoracion, nombre, posicion, goles, asistencias, imagenUrl)

