package com.example.myfirstapp

data class ArtifactList(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: ArrayList<Artifact>,
)
