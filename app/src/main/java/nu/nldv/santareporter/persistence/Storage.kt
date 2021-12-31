package nu.nldv.santareporter.persistence

import nu.nldv.santareporter.Child

interface Storage {
    fun save(list: List<Child>)
    fun load(): List<Child>
}
