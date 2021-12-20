package nu.nldv.santareporter

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

const val SHARED_PREFS = "kids"
const val CHILDREN = "CHILDREN"

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val sharedPrefs by lazy {
        (getApplication() as SantaApp).getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
    }


    private val _snackBar = MutableLiveData<SnackbarMessage?>()
    val snackBar: LiveData<SnackbarMessage?> get() = _snackBar

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
        val newChild = Child(name.trim())
        val currentList = children.value ?: listOf()
        if (currentList.any { it.name == newChild.name }) {
            snack(SnackbarMessage.Duplicate)
        } else {
            val newList = currentList.plus(newChild)
            _children.postValue(newList)
            _addDialogOpen.postValue(false)
            save(newList)
        }
    }

    private fun snack(msg: SnackbarMessage) {
        Log.d("MainViewModel", "snack() with $msg")
        _snackBar.postValue(msg)
    }

    private fun save(list: List<Child>) {
        val serialized = list.map { it.serialized() }.toSet()
        sharedPrefs.edit().putStringSet(CHILDREN, serialized).apply()
    }

    private fun load() {
        val list = sharedPrefs.getStringSet(CHILDREN, emptySet())
            ?.map { Child.fromSerialized(it) }?.toList()
            ?: listOf()
        _children.postValue(list)
    }

}
