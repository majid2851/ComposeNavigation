package com.majid2851.navigator.model

import androidx.compose.runtime.Immutable

/**
 * The full, static description of an app's navigation: its destinations, the declared
 * transitions between them and the entry point.
 *
 * This object is the single source of truth that both the runtime host and the visual
 * [com.majid2851.navigator.map.NavigationMap] read from.
 */
@Immutable
class NavGraph internal constructor(
    val startRoute: String,
    val destinations: Map<String, Destination>,
    val declaredEdges: List<Edge>,
) {
    init {
        require(destinations.containsKey(startRoute)) {
            "startRoute '$startRoute' is not declared as a screen in the graph."
        }
    }

    fun destinationOrNull(route: String): Destination? = destinations[route]

    /** All routes that appear in [destinations]. */
    val routes: Set<String> get() = destinations.keys
}
