package com.example.zorvyn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform