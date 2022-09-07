package com.biologic.myapplication.domain

import java.util.*

data class RepoVersion(
    val pulp_href: String,
    val pulp_created: String,
    val number: Int,
    val base_version: String?,
    val content_summary: Objects,
)