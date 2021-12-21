package nu.nldv.santareporter

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

const val SHARED_PREFS = "kids"
const val CHILDREN = "CHILDREN"

interface MainVM {

//    val addDialogOpen: LiveData<Boolean>
    val children: LiveData<List<Child>>
    fun sendToSanta()
    fun addChildDialog()
    fun dismissAddDialog()
    fun saveAddDialog(name: String)
    fun updateRating(child: Child, rating: Float)
    fun dismissSnack()
    val uiStateFlow: Flow<UiState>
}

sealed class UiState {
    object Normal : UiState()
    object AddDialog : UiState()
    class ShowSnackbar(val msg: SnackbarMessage) : UiState()
}

class MainViewModel(app: Application) : MainVM, AndroidViewModel(app) {

    private val sharedPrefs by lazy {
        (getApplication() as SantaApp).getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
    }


    private val uiStateChannel = Channel<UiState>(Channel.BUFFERED)
    override val uiStateFlow: Flow<UiState> = uiStateChannel.receiveAsFlow()

    private val _children = MutableLiveData<List<Child>>(mutableListOf())
    override val children: LiveData<List<Child>> get() = _children

    private val _addDialogOpen = MutableLiveData(false)
//    override val addDialogOpen: LiveData<Boolean> get() = _addDialogOpen

    init {
        load()
    }

    override fun sendToSanta() {
        Log.d("MainViewModel", "sendToSanta()")
        snack(SnackbarMessage.Sent)
        children.value?.let {
            save(it)
        }
    }

    override fun dismissSnack() {
        Log.d("MainViewModel", "dismissSnack()")
        viewModelScope.launch {
            uiStateChannel.send(UiState.Normal)
        }
    }

    override fun addChildDialog() {
        Log.d("MainViewModel", "addChildDialog()")
        viewModelScope.launch {
            uiStateChannel.send(UiState.AddDialog)
        }
        _addDialogOpen.postValue(true)
    }

    override fun dismissAddDialog() {
        Log.d("MainViewModel", "dismissAddDialog()")
        viewModelScope.launch {
            uiStateChannel.send(UiState.Normal)
        }
        _addDialogOpen.postValue(false)
    }

    override fun saveAddDialog(name: String) {
        Log.d("MainViewModel", "saveAddDialog() with $name")
        val newChild = Child(name.trim())
        val currentList = children.value ?: listOf()
        if (currentList.any { it.name == newChild.name }) {
            snack(SnackbarMessage.Duplicate)
        } else {
            val newList = currentList.plus(newChild)
            _children.postValue(newList.sortedBy { it.name })
            viewModelScope.launch {
                uiStateChannel.send(UiState.Normal)
            }
            save(newList)
        }
    }

    override fun updateRating(child: Child, rating: Float) {
        Log.d("MainViewModel", "updateRating() with ${child.name} and $rating")
        _children.value?.find { it.name == child.name }?.let {
            it.rating = rating.toInt()
        }
        _children.postValue(_children.value?.sortedBy { it.name })
    }

    override fun onCleared() {
        Log.d("MainViewModel", "onCleared()")
        children.value?.let {
            save(it)
        }
        super.onCleared()
    }

    private fun snack(msg: SnackbarMessage) {
        Log.d("MainViewModel", "snack() with $msg")
        viewModelScope.launch {
            uiStateChannel.send(UiState.ShowSnackbar(msg))
        }
    }

    private fun save(list: List<Child>) {
        val serialized = list.map { it.serialized() }.toSet()
        sharedPrefs.edit().putStringSet(CHILDREN, serialized).apply()
    }

    private fun load() {
        val list = sharedPrefs.getStringSet(CHILDREN, emptySet())
            ?.map { Child.fromSerialized(it) }?.toList()
            ?: listOf()
        _children.postValue(list.sortedBy { it.name })
    }

}
