package com.example.footballmatchmanager

class Jugadores(
    valoracion: Double,
    nombre: String,
    goles: Int,
    asistencias: Int,
    posicion: String = ""
) : JugadorBase(valoracion, nombre, posicion) {
    // Puedes añadir más funcionalidades específicas para los jugadores aquí
}