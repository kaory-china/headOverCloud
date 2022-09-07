package com.biologic.myapplication.domain

data class PublicationList (
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: ArrayList<Publication>?,
)