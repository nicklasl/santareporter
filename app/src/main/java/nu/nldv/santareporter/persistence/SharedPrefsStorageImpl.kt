package nu.nldv.santareporter.persistence

import android.content.SharedPreferences
import nu.nldv.santareporter.CHILDREN
import nu.nldv.santareporter.Child

class SharedPrefsStorageImpl(private val sharedPrefs: SharedPreferences) : Storage {

    override fun save(list: List<Child>) = with(list.map { it.serialized() }) {
        sharedPrefs.edit().putStringSet(CHILDREN, this.toSet()).apply()
    }

    override fun load(): List<Child> = sharedPrefs.getStringSet(CHILDREN, emptySet())
        ?.map { Child.fromSerialized(it) }?.toList()
        ?: listOf()
}
