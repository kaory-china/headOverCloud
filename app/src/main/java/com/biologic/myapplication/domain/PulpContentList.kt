package com.biologic.myapplication.domain

data class PulpContentList(
    val count: Int?,
    val next: String?,
    val previous: String?,
    val results: ArrayList<PulpContent>?,
)