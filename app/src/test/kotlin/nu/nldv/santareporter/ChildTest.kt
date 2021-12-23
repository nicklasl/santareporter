package nu.nldv.santareporter

import org.junit.Assert.*
import org.junit.Test

class ChildTest {

    @Test
    fun shouldSerializeSuccessfully() {
        assertEquals(
            "Ultimate Answer||42",
            Child("Ultimate Answer", 42).serialized()
        )
    }

    @Test
    fun shouldCreateFromSerialized() {
        val c = Child.fromSerialized("Ultimate Answer||42")
        assertEquals("Ultimate Answer", c.name)
        assertEquals(42, c.rating)
    }

}