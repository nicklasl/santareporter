package nu.nldv.santareporter

import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nu.nldv.santareporter.ui.theme.SantaReporterTheme
import nu.nldv.santareporter.ui.theme.Typography

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SantaReporterTheme {
                Scaffold(
                    floatingActionButton = { fab() },
                    floatingActionButtonPosition = FabPosition.End,
                    isFloatingActionButtonDocked = true,
                    bottomBar = { bottomBar() }
                ) {
                    Surface() {
                        background()
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Heading()
                            vm.children.observeAsState().value?.forEach {
                                Text(it.name)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun fab() {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.send_to_santa)) },
            onClick = { vm.sendToSanta() },
            icon = { Icon(Icons.Filled.Send, contentDescription = "Send") },
            backgroundColor = Color.Red,
            contentColor = Color.White
        )
    }

    @Composable
    private fun bottomBar() {
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
                addChildDialog()
            }
        }
    }

    @Composable
    private fun addChildDialog() {
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
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
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
    private fun background() {
        Image(
            painter = painterResource(id = R.drawable.santa_sleigh_1920_long),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            alpha = 0.5f
        )
    }
}


@Composable
fun Heading() {
    Text(
        text = stringResource(R.string.app_name),
        style = Typography.h1,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, top = 32.dp, bottom = 24.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SantaReporterTheme {
        Heading()
    }
}