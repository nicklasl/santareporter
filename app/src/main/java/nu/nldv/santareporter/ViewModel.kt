package nu.nldv.santareporter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <T: ViewModel> T.createFactory(): ViewModelProvider.Factory {
    val vm = this
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = vm as T
    }
}