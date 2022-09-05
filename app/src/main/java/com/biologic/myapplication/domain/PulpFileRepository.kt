package com.biologic.myapplication.domain

data class PulpFileRepository (
    var name: String?,
    val pulp_href: String?,
    val pulp_created: String?,
    val versions_href: String?,
    val pulp_labels: HashMap<String, String>?,
    val latest_version_href: String?,
    val description: String?,
    val retain_repo_versions: Integer?,
    val remote: String?,
    val autopublish: Boolean?,
    val manifest: String?,
    val results: ArrayList<PulpFileRepository>?,
)