package com.majid2851.navigator.model

import androidx.compose.runtime.Immutable

/**
 * A directed connection between two [Destination]s on the map.
 *
 * @param from source route.
 * @param to target route.
 * @param label optional text describing the transition (e.g. "View item").
 * @param declared true when the edge was declared statically in the DSL, false when it was
 *  discovered at runtime from an actual [com.majid2851.navigator.runtime.NavigatorController.navigate] call.
 */
@Immutable
data class Edge(
    val from: String,
    val to: String,
    val label: String? = null,
    val declared: Boolean = true,
)
