package nu.nldv.santareporter

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

const val SHARED_PREFS = "kids"
const val CHILDREN = "CHILDREN"

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val sharedPrefs by lazy {
        (getApplication() as SantaApp).getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
    }

    private val _children = MutableLiveData<List<Child>>(mutableListOf())
    val children: LiveData<List<Child>> get() = _children

    private val _addDialogOpen = MutableLiveData(false)
    val addDialogOpen: LiveData<Boolean> get() = _addDialogOpen

    init {
        load()
    }

    fun sendToSanta() {
        Log.d("MainViewModel", "sendToSanta()")
    }

    fun addChildDialog() {
        Log.d("MainViewModel", "addChildDialog()")
        _addDialogOpen.postValue(true)
    }

    fun dismissAddDialog() {
        Log.d("MainViewModel", "dismissAddDialog()")
        _addDialogOpen.postValue(false)
    }

    fun saveAddDialog(name: String) {
        Log.d("MainViewModel", "saveAddDialog() with $name")
        _addDialogOpen.postValue(false)
        val new = Child(name.trim())
        _children.postValue(children.value?.plus(new))
        save()
        load()
    }

    private fun save() {
        val serialized = children.value?.map { "${it.name}||${it.rating}" }?.toSet()
        sharedPrefs.edit().putStringSet(CHILDREN, serialized).apply()
    }

    private fun load() {
        _children.postValue(
            sharedPrefs.getStringSet(CHILDREN, emptySet())?.map {
                val (name, rating) = it.split("||")
                Child(name, rating.toInt())
            }
        )
    }

    val name = "Android"
}
