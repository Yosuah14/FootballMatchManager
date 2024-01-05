package com.example.footballmatchmanager

data class Partido(
    var fecha: String = "",
    var horaInicio: String = "",
    var horaFin: String = "",
    var jugadores: List<JugadorBase>? = listOf()
)