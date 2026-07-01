package com.majid2851.navigator.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.majid2851.navigator.dsl.navGraph
import com.majid2851.navigator.map.NavigationMapScaffold
import com.majid2851.navigator.runtime.NavigatorHost
import com.majid2851.navigator.runtime.rememberNavigatorController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SampleApp()
                }
            }
        }
    }
}

@Composable
private fun SampleApp() {
    val graph = rememberSampleGraph()
    val controller = rememberNavigatorController(graph)

    NavigationMapScaffold(controller = controller) {
        NavigatorHost(controller = controller, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun rememberSampleGraph() = androidx.compose.runtime.remember {
    navGraph(start = "home") {
        screen("home", label = "Home", group = "Main", icon = Icons.Filled.Home) {
            ScreenScaffold(title = "Home") {
                Button(onClick = { navigator.navigate("profile") }) { Text("Go to Profile") }
                Button(onClick = { navigator.navigate("catalog") }) { Text("Browse Catalog") }
                OutlinedButton(onClick = { navigator.navigate("settings") }) { Text("Settings") }
            }
        }
        screen("profile", label = "Profile", group = "Account", icon = Icons.Filled.Person) {
            ScreenScaffold(title = "Profile") {
                Button(onClick = { navigator.navigate("editProfile") }) { Text("Edit Profile") }
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        screen("editProfile", label = "Edit Profile", group = "Account") {
            ScreenScaffold(title = "Edit Profile") {
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        screen("catalog", label = "Catalog", group = "Shop", icon = Icons.Filled.ShoppingCart) {
            ScreenScaffold(title = "Catalog") {
                Button(onClick = { navigator.navigate("product") }) { Text("Open Product") }
                Button(onClick = { navigator.navigate("cart") }) { Text("Open Cart") }
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        screen("product", label = "Product", group = "Shop") {
            ScreenScaffold(title = "Product") {
                Button(onClick = { navigator.navigate("cart") }) { Text("Add to Cart") }
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        screen("cart", label = "Cart", group = "Shop") {
            ScreenScaffold(title = "Cart") {
                Button(onClick = { navigator.navigate("checkout") }) { Text("Checkout") }
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        screen("checkout", label = "Checkout", group = "Shop") {
            ScreenScaffold(title = "Checkout") {
                Button(onClick = { navigator.navigate("home", popUpToStart = true) }) { Text("Finish") }
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        screen("settings", label = "Settings", group = "Main", icon = Icons.Filled.Settings) {
            ScreenScaffold(title = "Settings") {
                Button(onClick = { navigator.navigate("about") }) { Text("About") }
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        screen("about", label = "About", group = "Main") {
            ScreenScaffold(title = "About") {
                OutlinedButton(onClick = { navigator.popBackStack() }) { Text("Back") }
            }
        }
        // A declared-but-not-yet-visited edge, plus an orphan screen to show both map states.
        edge(from = "home", to = "profile", label = "Account")
        screen("secret", label = "Secret (orphan)", group = "Hidden") {
            ScreenScaffold(title = "Secret") {
                OutlinedButton(onClick = { navigator.navigateToRoot() }) { Text("Home") }
            }
        }
    }
}
