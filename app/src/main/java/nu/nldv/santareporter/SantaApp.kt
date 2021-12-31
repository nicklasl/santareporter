package nu.nldv.santareporter

import android.app.Application
import nu.nldv.santareporter.persistence.SharedPrefsStorageImpl
import nu.nldv.santareporter.persistence.Storage

const val SHARED_PREFS = "kids"
const val CHILDREN = "CHILDREN"

class SantaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    //TODO real di
    val storage: Storage by lazy {
        val sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        SharedPrefsStorageImpl(sharedPrefs)
    }

    companion object {
        lateinit var instance: SantaApp
            private set
    }
}