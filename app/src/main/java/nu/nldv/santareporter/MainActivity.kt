package nu.nldv.santareporter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import nu.nldv.santareporter.ui.theme.SantaReporterTheme
import nu.nldv.santareporter.ui.theme.Typography

const val SnackbarSlideOutTimeInMs = 700

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current
            val uiStateFlowLifecycleAware = remember(vm.uiStateFlow, lifecycleOwner) {
                vm.uiStateFlow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            }
            val uiState = uiStateFlowLifecycleAware.collectAsState(initial = UiState.Normal)

            val scaffoldState = rememberScaffoldState()

            SantaReporterTheme {

                Scaffold(
                    scaffoldState = scaffoldState,
                    floatingActionButton = { Fab(vm) },
                    floatingActionButtonPosition = FabPosition.End,
                    isFloatingActionButtonDocked = true,
                    bottomBar = { BottomBar(vm) }
                ) {
                    Surface {
                        Background()
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Column(modifier = Modifier.align(Alignment.Center)) {
                                Heading()
                                vm.children.observeAsState().value?.forEach {
                                    ChildRow(it, vm)
                                }
                            }
                        }
                        when (uiState.value) {
                            UiState.Normal -> { }
                            UiState.AddDialog -> AddChildDialog(vm)
                            is UiState.ShowSnackbar -> {
                                val text: String =
                                    when ((uiState.value as UiState.ShowSnackbar).msg) {
                                        SnackbarMessage.Duplicate -> stringResource(id = R.string.snack_duplicate)
                                        SnackbarMessage.Sent -> stringResource(id = R.string.snack_report)
                                    }
                                val actionLabel = stringResource(id = R.string.ok)
                                LaunchedEffect(scaffoldState.snackbarHostState) {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        text,
                                        actionLabel,
                                        SnackbarDuration.Short
                                    )
                                }
                            }
                            is UiState.Edit -> {
                                var expanded by remember { mutableStateOf(false) }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(onClick = { vm.remove((uiState.value as UiState.Edit).child) }) {
                                        Text(stringResource(id = R.string.remove))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun ChildRow(child: Child, vm: MainVM) {
    Column(modifier = Modifier
        .padding(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 8.dp)
        .pointerInput(Unit) {
            detectTapGestures(onLongPress = { vm.longPress(child) })
        }) {
        var sliderPosition by remember { mutableStateOf(child.rating.toFloat()) }

        Text(
            text = child.name,
            modifier = Modifier.fillMaxWidth(),
            style = Typography.h2,
            textAlign = TextAlign.Center,
        )
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = sliderPosition,
            valueRange = 1f..100f,
            onValueChange = {
                sliderPosition = it
            },
            onValueChangeFinished = {
                vm.updateRating(child, sliderPosition)
            })
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.naughty),
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(stringResource(id = R.string.nice), modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun Fab(vm: MainVM) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = vm.dirty.observeAsState().value == true,
        enter = fadeIn(
            // Fade in with the initial alpha of 0.3f.
            initialAlpha = 0.2f
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { with(density) { 220.dp.roundToPx() } },
            animationSpec = tween(SnackbarSlideOutTimeInMs)
        )
    ) {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.send_to_santa)) },
            onClick = { vm.sendToSanta() },
            icon = { Icon(Icons.Filled.Send, contentDescription = "Send") },
            backgroundColor = Color.Red,
            contentColor = Color.White
        )
    }
}

@Composable
private fun BottomBar(vm: MainVM) {
    BottomAppBar {
        IconButton(onClick = { vm.addChildDialog() }) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "More",
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            )
        }
    }
}

@Composable
private fun AddChildDialog(vm: MainVM) {
    val textState = remember { mutableStateOf(TextFieldValue()) }
    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.add_child),
                style = Typography.h3
            )
        },
        text = {
            Column {
                Text(stringResource(id = R.string.add_child_description))


                OutlinedTextField(
                    value = textState.value,
                    placeholder = { Text(stringResource(id = R.string.add_child_hint)) },
                    label = { Text(stringResource(id = R.string.name)) },
                    maxLines = 1,
                    onValueChange = { textState.value = it },
                )
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { vm.dismissAddDialog() }
                ) {
                    Text(stringResource(id = R.string.dismiss), color = Color.White)
                }
                Button(
                    onClick = { vm.saveAddDialog(textState.value.text) }
                ) {
                    Text(stringResource(id = R.string.save), color = Color.White)
                }
            }
        },
        onDismissRequest = { vm.dismissAddDialog() })
}

@Composable
private fun Background() {
    Image(
        painter = painterResource(id = R.drawable.santa_sleigh_1920_long),
        contentDescription = "Background",
        modifier = Modifier.fillMaxSize(),
        alignment = Alignment.Center,
        contentScale = ContentScale.FillBounds,
        alpha = 0.5f
    )
}


@Composable
fun Heading() {
    Text(
        text = stringResource(R.string.app_name),
        style = Typography.h1,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, bottom = 24.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SantaReporterTheme {
        Background()
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Heading()
                ChildRow(child = Child("Child1", 40), vm = mockVM)
                ChildRow(child = Child("Child2", 80), vm = mockVM)
            }
        }
    }
}

val mockVM = object : MainVM {
    override val children: LiveData<List<Child>>
        get() = MutableLiveData(listOf())
    override val dirty: LiveData<Boolean>
        get() = MutableLiveData(false)
    override val uiStateFlow: Flow<UiState>
        get() = TODO("Not yet implemented")

    override fun sendToSanta() {
        TODO("Not yet implemented")
    }

    override fun addChildDialog() {
        TODO("Not yet implemented")
    }

    override fun dismissAddDialog() {
        TODO("Not yet implemented")
    }

    override fun saveAddDialog(name: String) {
        TODO("Not yet implemented")
    }

    override fun updateRating(child: Child, rating: Float) {
        TODO("Not yet implemented")
    }

    override fun dismissSnack() {
        TODO("Not yet implemented")
    }

    override fun longPress(child: Child) {
        TODO("Not yet implemented")
    }

    override fun remove(child: Child) {
        TODO("Not yet implemented")
    }
}