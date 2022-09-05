package com.example.myfirstapp

import com.google.gson.annotations.SerializedName

data class PulpResponse (
    var versions: ArrayList<Version> = ArrayList<Version>(),
    var online_workers: ArrayList<Worker> = ArrayList<Worker>(),
    var online_content_apps: ArrayList<Content> = ArrayList<Content>(),
    var database_connection: DB = DB(),
    var redis_connection: Cache = Cache(),
    var storage: String? = null
)

data class Version(
    var component: String? = null,
    var version: String? = null,

    @SerializedName("package")
    var Package: String? = null
)


data class Worker(
    var pulp_href: String? = null,
    var pulp_created:  String? = null,
    var name: String? = null,
    var last_heartbeat: String? = null,
    var current_task: String? = null
)

data class Content(
    var name: String? = null,
    var last_heartbeat: String? = null
)

data class DB(
    var connected: Boolean? = null
)

data class Cache(
    var connected: Boolean? = null
)