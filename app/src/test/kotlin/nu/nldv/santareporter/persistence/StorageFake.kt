package nu.nldv.santareporter.persistence

import nu.nldv.santareporter.Child

class StorageFake(val backingList: MutableList<Child> = mutableListOf()) : Storage {

    override fun save(list: List<Child>) = backingList.clear().also { backingList.addAll(list) }

    override fun load(): List<Child> = backingList.toList()

}
