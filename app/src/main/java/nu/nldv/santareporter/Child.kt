package nu.nldv.santareporter

data class Child(val name: String, var rating: Int = 50) {

    fun serialized(): String = "${name}||${rating}"

    companion object {
        fun fromSerialized(serialized: String): Child {
            val (name, rating) = serialized.split("||")
            return Child(name, rating.toInt())
        }
    }
}
