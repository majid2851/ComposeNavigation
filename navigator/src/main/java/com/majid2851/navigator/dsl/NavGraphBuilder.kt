package com.majid2851.navigator.dsl

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.majid2851.navigator.model.Destination
import com.majid2851.navigator.model.Edge
import com.majid2851.navigator.model.NavGraph
import com.majid2851.navigator.runtime.ScreenScope

@DslMarker
annotation class NavGraphDsl

/**
 * Builder used by [navGraph] to declare screens and the transitions between them.
 *
 * Declared edges are optional: any [com.majid2851.navigator.runtime.NavigatorController.navigate]
 * call is also recorded at runtime and shown on the map. Declaring edges up front simply makes the
 * map complete before the user has walked every path.
 */
@NavGraphDsl
class NavGraphBuilder internal constructor(private val startRoute: String) {

    private val destinations = LinkedHashMap<String, Destination>()
    private val edges = mutableListOf<Edge>()

    /**
     * Declares a screen. The [route] must be unique. Optionally chain [goesTo] inside [content]
     * declaration via the [edge] function, or simply call `navigator.navigate(...)` at runtime.
     */
    fun screen(
        route: String,
        label: String = route,
        group: String? = null,
        icon: ImageVector? = null,
        content: @Composable ScreenScope.() -> Unit,
    ) {
        require(!destinations.containsKey(route)) { "Duplicate route declared: '$route'" }
        destinations[route] = Destination(route, label, group, icon, content)
    }

    /** Declares a static transition shown on the map even before the user navigates it. */
    fun edge(from: String, to: String, label: String? = null) {
        edges += Edge(from = from, to = to, label = label, declared = true)
    }

    internal fun build(): NavGraph {
        edges.forEach { e ->
            require(destinations.containsKey(e.from)) {
                "edge references unknown source route '${e.from}'"
            }
            require(destinations.containsKey(e.to)) {
                "edge references unknown target route '${e.to}'"
            }
        }
        return NavGraph(startRoute, destinations.toMap(), edges.toList())
    }
}

/**
 * Entry point of the DSL.
 *
 * ```
 * val graph = navGraph(start = "home") {
 *     screen("home", label = "Home") { HomeScreen(onOpenDetail = { navigator.navigate("detail") }) }
 *     screen("detail", label = "Detail") { DetailScreen(onBack = { navigator.popBackStack() }) }
 *     edge("home", "detail", label = "Open")
 * }
 * ```
 */
fun navGraph(start: String, block: NavGraphBuilder.() -> Unit): NavGraph =
    NavGraphBuilder(start).apply(block).build()
