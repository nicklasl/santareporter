package nu.nldv.santareporter

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private lateinit var vm: MainViewModel
    private lateinit var prefs: SharedPreferences

    @get:Rule
    val coroutinesTestRule = CoroutinesTestRule()
    @get:Rule
    val testInstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        prefs = PrefsFake()
        prefs.edit().putStringSet(CHILDREN, testData).commit()
        vm = MainViewModel(prefs)
    }

    @After
    fun cleanUp() {
        prefs.edit().clear().commit()
    }

    @Test
    fun initShouldLoadFromSharedPrefs() {
        assertNotNull(vm.children.value)
        with(vm.children.value!!) {
            assertEquals(3, size)
            assertEquals("A", get(0).name)
            assertEquals(99, get(0).rating)
            assertEquals("B", get(1).name)
            assertEquals(1, get(1).rating)
            assertEquals("C", get(2).name)
            assertEquals(50, get(2).rating)
        }
    }

    @Test
    fun sendToSantaShouldEmitSnackbar() = runTest {
        vm.sendToSanta()
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.ShowSnackbar)
        val dirty = vm.dirty.value
        assertNotNull(dirty)
        assertFalse(dirty!!)
    }

    @Test
    fun updateRatingShouldMarkDirty() = runTest {
        val child = Child("A", 1)
        vm.updateRating(child, 42f)
        val dirty = vm.dirty.value!!
        assertTrue(dirty)
    }

    @Test
    fun updateRatingShouldUpdateChildrenLiveData() = runTest {
        val child = Child("A", 1)
        vm.updateRating(child, 42f)
        vm.children.value!!.let {
            val updated = it.get(0)
            assertEquals(42, updated.rating)
            assertEquals("A", updated.name)
        }
    }

    @Test
    fun addChildDialogShouldSendUiState() = runTest {
        vm.addChildDialog()
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.AddDialog)
    }

    @Test
    fun dismissAddDialogShouldReturnToNormal() = runTest {
        vm.dismissAddDialog()
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.Normal)
    }

    @Test
    fun dismissSnackShouldReturnToNormal() = runTest {
        vm.dismissSnack()
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.Normal)
    }

    @Test
    fun saveAddDialogShouldAddChildToLivedata() = runTest {
        vm.saveAddDialog("NewChild")
        vm.children.value!!.let {
            val added = it.find { it.name == "NewChild" }
            assertNotNull(added)
            assertEquals("NewChild", added!!.name)
            assertEquals(50, added.rating)
        }
    }

    @Test
    fun saveAddDialogShouldStoreInSharedPrefs() = runTest {
        vm.saveAddDialog("NewChild")
        val stringSet = prefs.getStringSet(CHILDREN, emptySet())
        assertNotNull(stringSet)
        assertEquals(4, stringSet?.size)
        assertTrue(stringSet?.any { it == "NewChild||50" } == true)
    }

    @Test
    fun saveAddDialogWithDuplicateShouldShowSnackbar() = runTest {
        vm.saveAddDialog("A")
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.ShowSnackbar)
        assertEquals(SnackbarMessage.Duplicate, (state as UiState.ShowSnackbar).msg)
        assertEquals(3, vm.children.value?.size)
    }

    @Test
    fun editShouldChangeState() = runTest {
        vm.edit()
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.Edit)
    }

    @Test
    fun saveNameShouldReturnToNormalState() = runTest {
        vm.edit()
        val editState = vm.uiStateFlow.first()
        assertTrue(editState is UiState.Edit)

        val child = vm.children.value!!.first()
        vm.saveName(child, "Luke")
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.Normal)
    }

    @Test
    fun exitEditShouldReturnToNormalState() = runTest {
        vm.edit()
        val editState = vm.uiStateFlow.first()
        assertTrue(editState is UiState.Edit)

        vm.exitEdit()
        val state = vm.uiStateFlow.first()
        assertTrue(state is UiState.Normal)
    }

    @Test
    fun saveNameShouldUpdateLivedata() = runTest {
        val child = vm.children.value!!.first()
        val ratingBefore = child.rating
        vm.saveName(child, "Luke")
        vm.children.value!!.let {
            val updated = it.find { it.name == "Luke" }
            assertNotNull(updated)
            assertEquals(ratingBefore, updated?.rating)
        }
    }

    @Test
    fun saveNameShouldUpdateSharedPrefs() = runTest {
        val child = vm.children.value!!.first()
        val ratingBefore = child.rating
        vm.saveName(child, "Luke")

        val stringSet = prefs.getStringSet(CHILDREN, emptySet())
        assertNotNull(stringSet)
        assertEquals(3, stringSet?.size)
        assertTrue(stringSet?.any { it == "Luke||$ratingBefore" } == true)
    }

    @Test
    fun removeShouldUpdateLivedata() = runTest {
        val child = vm.children.value!!.first()
        vm.remove(child)

        vm.children.value!!.let {
            assertEquals(2, it.size)
            assertTrue(it.none { it.name == child.name })
        }
    }

    @Test
    fun removeShouldUpdateSharedPrefs() = runTest {
        val child = vm.children.value!!.first()
        vm.remove(child)

        val stringSet = prefs.getStringSet(CHILDREN, emptySet())
        assertNotNull(stringSet)
        assertEquals(2, stringSet?.size)
        assertTrue(stringSet!!.none { it.contains(child.name) })
    }

    private val testData = listOf(
        "A||99",
        "B||1",
        "C||50",
    ).toSet()
}