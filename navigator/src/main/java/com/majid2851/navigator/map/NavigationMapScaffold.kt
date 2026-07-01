package com.majid2851.navigator.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.majid2851.navigator.runtime.NavigatorController

/**
 * Wraps your app UI ([content]) and overlays a floating button that opens the full-screen
 * navigation map. Drop this around your [com.majid2851.navigator.runtime.NavigatorHost] and you get
 * the "see all my screens" view for free — ideal for debug builds.
 *
 * ```
 * NavigationMapScaffold(controller) {
 *     NavigatorHost(controller)
 * }
 * ```
 */
@Composable
fun NavigationMapScaffold(
    controller: NavigatorController,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    var showMap by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (enabled) {
            FloatingActionButton(
                onClick = { showMap = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
            ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = "Show navigation map")
            }
        }
    }

    if (showMap) {
        NavigationMapDialog(
            controller = controller,
            onDismiss = { showMap = false },
        )
    }
}

/** A full-screen dialog that hosts an interactive [NavigationMap] with a title bar and legend. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationMapDialog(
    controller: NavigatorController,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Navigation map") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                )
                Box(Modifier.weight(1f).fillMaxSize()) {
                    NavigationMap(
                        controller = controller,
                        modifier = Modifier.fillMaxSize(),
                    )
                    MapLegend(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLegend(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        color = colors.surface.copy(alpha = 0.92f),
        tonalElevation = 3.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LegendRow(colors.primaryContainer, "Current screen")
            LegendRow(colors.secondaryContainer, "In back stack")
            LegendRow(colors.surfaceVariant, "Reachable")
            LegendRow(colors.errorContainer, "Unreachable")
        }
    }
}

@Composable
private fun LegendRow(swatch: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(14.dp),
            color = swatch,
            shape = CircleShape,
            content = {},
        )
        Spacer(Modifier.size(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
