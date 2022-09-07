package com.biologic.myapplication.domain

data class PulpContentList(
    val count: Integer?,
    val next: String?,
    val previous: String?,
    val results: ArrayList<PulpContent>?,
)