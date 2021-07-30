package com.example.eventmaster.places
//Dataklasse for steder, blir brukt i autocomplete-textview for å vise navn og plass på steder
data class Place(
    val id: String,
    val description: String
) {
    override fun toString(): String {
        return ""
    }
}