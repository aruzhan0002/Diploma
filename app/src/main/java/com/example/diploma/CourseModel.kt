package com.example.diploma

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class Course(
    val id: Int,
    val title: String,
    val description: String,
    val price: String,
    val audienceLabel: String = "РОДИТЕЛЯМ",
    val rating: String = "4.9★"
)

object CourseRepository {

    private val _courses = mutableStateListOf(
        Course(
            id = 1,
            title = "Основы Аутизма",
            description = "A comprehensive guide for parents to understand ASD, recognize early signs...",
            price = "12.000 ₸"
        ),
        Course(
            id = 2,
            title = "Основы Аутизма",
            description = "A comprehensive guide for parents to understand ASD, recognize early signs...",
            price = "12.000 ₸"
        )
    )

    val courses: SnapshotStateList<Course>
        get() = _courses

    fun addCourse(
        title: String,
        description: String,
        price: String,
        audienceLabel: String = "РОДИТЕЛЯМ"
    ): Course {
        val nextId = (_courses.maxOfOrNull { it.id } ?: 0) + 1
        val course = Course(
            id = nextId,
            title = title,
            description = description,
            price = price,
            audienceLabel = audienceLabel
        )
        _courses.add(0, course)
        return course
    }
}

