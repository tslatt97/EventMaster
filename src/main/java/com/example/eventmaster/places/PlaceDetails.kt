package com.example.eventmaster.places
//Dataklasse for stedsdetaljer, brukes for å sette inn lat/long til valgt addresse inn i backend
data class PlaceDetails(
    val name: String,
    val address: ArrayList<Address>,
    val lat: Double,
    val lng: Double,

)