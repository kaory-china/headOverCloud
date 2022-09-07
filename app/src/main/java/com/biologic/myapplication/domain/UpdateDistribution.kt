package com.biologic.myapplication.domain

data class UpdateDistribution(
    val base_path: String,
    val content_guard: String?,
    val name: String,
    val publication: String?,
)
