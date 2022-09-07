package com.biologic.myapplication.domain

data class Publication(
    val pulp_href: String?,
    val pulp_created: String?,
    val repository_version: String?,
    val repository: String?,
    val distributions: ArrayList<Object>?,
    val manifest: String?,
)