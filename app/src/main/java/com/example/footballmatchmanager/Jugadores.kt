package com.example.footballmatchmanager



class Jugadores(
    //Clase hija jugadores normales
    valoracion: Double,
    nombre: String,
    val goles: Int,
    val asistencias: Int,
    posicion: String
) : JugadorBase(valoracion, nombre, posicion) {

}