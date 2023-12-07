package com.example.footballmatchmanager



class Jugadores(
    valoracion: Double,
    nombre: String,
    posicion: String,
    goles: Int = 0,
    asistencias: Int = 0
) : JugadorBase(valoracion, nombre, posicion, goles, asistencias)