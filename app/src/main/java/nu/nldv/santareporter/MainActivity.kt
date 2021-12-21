package nu.nldv.santareporter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import nu.nldv.santareporter.ui.theme.SantaReporterTheme
import nu.nldv.santareporter.ui.theme.Typography
import java.util.logging.Logger

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val scaffoldState = rememberScaffoldState()

            val lifecycleOwner = LocalLifecycleOwner.current

            val eventsFlowLifecycleAware = remember(vm.eventsFlow, lifecycleOwner) {
                vm.eventsFlow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            }
//            val snackbarHostState = remember { vm.snackbarHostState }
            eventsFlowLifecycleAware.collectAsState(initial = Event.NoState)
            LaunchedEffect(scaffoldState.snackbarHostState) {
                eventsFlowLifecycleAware.collectAsState().onEach {
                    when(it) {
                        Event.CloseAddDialog -> Log.d("MainActivity", "eventsFlow: Event.CloseAddDialog")
                        Event.DismissSnackbar -> TODO()
                        Event.OpenAddDialog -> Log.d("MainActivity", "eventsFlow: Event.CloseAddDialog")
                        is Event.ShowSnackbar -> ShowSnack(scaffoldState, it)
                    }
                }
            }

            SantaReporterTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
//                    snackbarHost = { state -> SnackyBarHost(state) },
                    floatingActionButton = { Fab(vm) },
                    floatingActionButtonPosition = FabPosition.End,
                    isFloatingActionButtonDocked = true,
                    bottomBar = { BottomBar(vm) }
                ) {
                    Surface() {
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
                    }
                }
            }
        }
    }

}
@Composable
private fun ShowSnack(scaffoldState: ScaffoldState, event: Event.ShowSnackbar) {
    val text: String = when (event.msg) {
        SnackbarMessage.Duplicate -> stringResource(id = R.string.snack_duplicate)
        SnackbarMessage.Sent -> stringResource(id = R.string.snack_report)
    }
    val actionLabel = stringResource(id = R.string.ok)

    scaffoldState.snackbarHostState.showSnackbar(text, actionLabel, SnackbarDuration.Short)
}

@Composable
private fun SnackyBarHost(state: SnackbarHostState) {
    SnackbarHost(state) { data ->
        data
        Snackbar(
            modifier = Modifier.border(2.dp, MaterialTheme.colors.secondary),
            snackbarData = data
        )
    }
}

@Composable
private fun Snacky(modifier: Modifier, vm: MainVM, message: SnackbarMessage) {
    val text: String = when (message) {
        SnackbarMessage.Duplicate -> stringResource(id = R.string.snack_duplicate)
        SnackbarMessage.Sent -> stringResource(id = R.string.snack_report)
    }

    Snackbar(
        modifier = modifier,
        action = {
            Button(onClick = { vm.dismissSnack() }) {
                Text(stringResource(id = R.string.ok))
            }
        }
    ) {
        Text(text)
    }
}

@Composable
private fun ChildRow(child: Child, vm: MainVM) {
    Column(modifier = Modifier.padding(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 8.dp)) {
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

@Composable
private fun Fab(vm: MainVM) {
    ExtendedFloatingActionButton(
        text = { Text(stringResource(R.string.send_to_santa)) },
        onClick = { vm.sendToSanta() },
        icon = { Icon(Icons.Filled.Send, contentDescription = "Send") },
        backgroundColor = Color.Red,
        contentColor = Color.White
    )
}

@Composable
private fun BottomBar(vm: MainVM) {
    BottomAppBar {
        val openDialogState = vm.addDialogOpen.observeAsState()

        IconButton(onClick = { vm.addChildDialog() }) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "More",
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            )
        }

        if (openDialogState.value == true) {
            AddChildDialog(vm)
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
    override val addDialogOpen: LiveData<Boolean>
        get() = MutableLiveData(false)
    override val children: LiveData<List<Child>>
        get() = MutableLiveData(listOf())
    override val eventsFlow: Flow<Event>
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


}