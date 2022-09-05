package com.biologic.myapplication.domain

data class PulpArtifact (
    val pulp_href: String?,
    val pulp_created: String?,
    val file: String?,
    val size: Integer?,
    val md5: String?,
    val sha1: String?,
    val sha224: String?,
    val sha256: String?,
    val sha384: String?,
    val sha512: String?,
)