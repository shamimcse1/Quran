package com.onik.quran.`interface`

import java.io.IOException

interface LocationInterface {
    fun located(city: String, country: String)
    fun error(error: IOException)
}