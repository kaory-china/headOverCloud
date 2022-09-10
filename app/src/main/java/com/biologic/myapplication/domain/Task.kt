package com.biologic.myapplication.domain

data class Task(
    val pulp_href: String?,
    val pulp_created: String?,
    val state: String?,
    val name: String?,
    val logging_cid: String?,
    val started_at: String?,
    val finished_at: String?,
    val error: String?,
    val worker: String?,
    val parent_task: String?,
    val child_tasks: ArrayList<Object>?,
    val task_group: String?,
    val progress_reports: ArrayList<Object>,
    val created_resources: ArrayList<Object>,
    val reserved_resources_record: ArrayList<Object>,
)