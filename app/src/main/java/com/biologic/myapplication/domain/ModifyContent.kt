package com.biologic.myapplication.domain

data class ModifyContent (
    val add_content_units: ArrayList<String>?,
    val remove_content_units: ArrayList<String>?,
    val base_version: String?,
        )