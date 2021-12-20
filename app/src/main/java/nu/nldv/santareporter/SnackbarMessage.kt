package nu.nldv.santareporter

sealed class SnackbarMessage {

    object Sent : SnackbarMessage()
    object Duplicate : SnackbarMessage()

    override fun toString(): String = this.javaClass.simpleName
}
