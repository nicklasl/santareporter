package nu.nldv.santareporter

import android.content.SharedPreferences

class PrefsFake : SharedPreferences {

    val backingMap = mutableMapOf<String?, Any?>()
    private val editor = object : SharedPreferences.Editor {
        override fun putString(p0: String?, p1: String?): SharedPreferences.Editor {
            backingMap[p0] = p1
            return this
        }

        override fun putStringSet(p0: String?, p1: MutableSet<String>?): SharedPreferences.Editor {
            backingMap[p0] = p1
            return this
        }

        override fun putInt(p0: String?, p1: Int): SharedPreferences.Editor {
            backingMap[p0] = p1
            return this
        }

        override fun putLong(p0: String?, p1: Long): SharedPreferences.Editor {
            backingMap[p0] = p1
            return this
        }

        override fun putFloat(p0: String?, p1: Float): SharedPreferences.Editor {
            backingMap[p0] = p1
            return this
        }

        override fun putBoolean(p0: String?, p1: Boolean): SharedPreferences.Editor {
            backingMap[p0] = p1
            return this
        }

        override fun remove(p0: String?): SharedPreferences.Editor {
            backingMap.remove(p0)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            backingMap.clear()
            return this
        }

        override fun commit(): Boolean {
            return true
        }

        override fun apply() {
            // no-op
        }

    }

    override fun getAll(): MutableMap<String, *> = backingMap.mapKeys { it.key!! }.toMutableMap()

    override fun getString(p0: String?, p1: String?): String? = backingMap[p0] as? String ?: p1

    override fun getStringSet(p0: String?, p1: MutableSet<String>?): MutableSet<String> =
        backingMap[p0] as? MutableSet<String> ?: p1 ?: mutableSetOf()

    override fun getInt(p0: String?, p1: Int): Int  = backingMap[p0] as? Int ?: p1

    override fun getLong(p0: String?, p1: Long): Long  = backingMap[p0] as? Long ?: p1

    override fun getFloat(p0: String?, p1: Float): Float  = backingMap[p0] as? Float ?: p1

    override fun getBoolean(p0: String?, p1: Boolean): Boolean  = backingMap[p0] as? Boolean ?: p1

    override fun contains(p0: String?): Boolean  = backingMap.contains(p0)

    override fun edit(): SharedPreferences.Editor = editor

    override fun registerOnSharedPreferenceChangeListener(p0: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("Not yet implemented")
    }

    override fun unregisterOnSharedPreferenceChangeListener(p0: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("Not yet implemented")
    }

}
