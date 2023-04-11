package com.codercamp.quran.theme

import android.app.Activity
import com.codercamp.quran.R
import com.codercamp.quran.database.ApplicationData


class ApplicationTheme(context: Activity) {

    init {
        if (ApplicationData(context).darkTheme) {
            when(ApplicationData(context).primaryColor) {
                ApplicationData.PURPLE -> context.setTheme(R.style.ThemeDarkPurple)
                ApplicationData.BLUE -> context.setTheme(R.style.ThemeDarkBlue)
                ApplicationData.ORANGE -> context.setTheme(R.style.ThemeDarkOrange)
            }
        } else {
            when(ApplicationData(context).primaryColor) {
                ApplicationData.PURPLE -> context.setTheme(R.style.ThemeLightPurple)
                ApplicationData.BLUE -> context.setTheme(R.style.ThemeLightBlue)
                ApplicationData.ORANGE -> context.setTheme(R.style.ThemeLightOrange)
            }
        }
    }
}