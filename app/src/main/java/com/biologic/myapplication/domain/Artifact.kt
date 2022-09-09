package com.example.myfirstapp

data class Artifact(
    val pulp_href: String,
    val pulp_created: String,
    val file: String,
    val size: Int,
    val md5: String?,
    val sha1: String?,
    val sha224: String?,
    val sha256: String?,
    val sha384: String?,
    val sha512: String?,
)
