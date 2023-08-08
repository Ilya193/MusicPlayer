package com.example.audioplayer.core

// ? ? s14.9
fun String.s149(): String {
    return substring(lastIndexOf("/") + 1, lastIndexOf("."))
        .replace("_", " ")
}