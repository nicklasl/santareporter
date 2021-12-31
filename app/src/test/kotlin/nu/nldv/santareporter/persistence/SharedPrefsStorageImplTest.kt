package nu.nldv.santareporter.persistence

import android.content.SharedPreferences
import nu.nldv.santareporter.CHILDREN
import nu.nldv.santareporter.Child
import nu.nldv.santareporter.PrefsFake
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SharedPrefsStorageImplTest {

    private lateinit var storage: SharedPrefsStorageImpl
    private lateinit var prefs: PrefsFake

    @Before
    fun setUp() {
        prefs = PrefsFake()
        storage = SharedPrefsStorageImpl(prefs)
    }

    @After
    fun cleanUp() {
        prefs.edit().clear().commit()
    }

    @Test
    fun emptyByDefault() {
        assertTrue(storage.load().isEmpty())
    }

    @Test
    fun canStoreAndLoadSingleChild() {
        val c = Child("A", 20)
        storage.save(listOf(c))

        val load = storage.load()
        assertEquals(1, load.size)
        assertEquals(c.name, load.first().name)
        assertEquals(c.rating, load.first().rating)
    }

    @Test
    fun canStoreAndLoadMultipleChildren() {
        val c1 = Child("A", 20)
        val c2 = Child("B", 40)
        storage.save(listOf(c1, c2))

        val load = storage.load()
        assertEquals(2, load.size)
        assertEquals(c1.name, load[0].name)
        assertEquals(c1.rating, load[0].rating)
        assertEquals(c2.name, load[1].name)
        assertEquals(c2.rating, load[1].rating)
    }


}
