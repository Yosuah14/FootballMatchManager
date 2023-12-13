package com.example.footballmatchmanager

data class Partido(
    var fecha: String = "",
    var horaInicio: String = "",
    var horaFin: String = "", // Nuevo atributo para la hora de fin
    var jugadores: List<JugadorBase>? = listOf()
) {
    // Constructor sin la lista de jugadores
    constructor(fecha: String, horaInicio: String, horaFin: String) : this(fecha, horaInicio, horaFin, listOf())
}