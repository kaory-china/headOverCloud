package com.biologic.myapplication.domain

data class Distribution (
    var name: String?,
    val pulp_href: String,
    val pulp_created: String?,
    val base_path: String?,
    val base_url: String?,
    val content_guard: String?,
    val pulp_labels: Object,
    val repository: String?,
    val publication: String?,
)