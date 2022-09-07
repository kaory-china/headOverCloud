package com.biologic.myapplication.domain

data class DistributionList(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: ArrayList<Distribution>?,
)
