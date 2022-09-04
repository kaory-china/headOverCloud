package com.biologic.myapplication

data class PulpResponse(
    val versions: List<Component>,
    val onlineWorkers: List<OnlineWorkers>,
    val onlineContentApp: List<OnlineContentApp>,
    val databaseConnection: DatabaseConnection,
    val redisConnection: RedisConnection,
    val storage: Storage
)


data class Component(
    val component: String,
    val version: String
)

data class OnlineWorkers(
    val pulpHref: String,
    val pulpCreated: String,
    val name: String,
    val lastHeartbeat: String
)

data class OnlineContentApp(
    val name: String,
    val lastHeartbeat: String
)

data class DatabaseConnection(
    val connected: Boolean
)

data class RedisConnection(
    val connected: Boolean
)

data class Storage(
    val total: Int,
    val used: Int,
    val free: Int
)