package nu.nldv.santareporter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

const val SHARED_PREFS = "kids"
const val CHILDREN = "CHILDREN"

@HiltAndroidApp
class SantaApp : Application()