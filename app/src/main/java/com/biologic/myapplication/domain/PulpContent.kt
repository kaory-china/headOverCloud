package com.biologic.myapplication.domain

data class PulpContent(
    val relative_path: String?,
    val sha256: String?,
    val artifact: String?,
    val pulp_created: String?,
    val pulp_href: String?,
    val md5: String?,
    val sha1: String?,
    val sha224: String?,
    val sha384: String?,
    val sha512: String?,
)