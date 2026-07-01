package com.majid2851.navigator.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.majid2851.navigator.model.Edge
import com.majid2851.navigator.model.NavGraph

/**
 * Drives navigation and, as a side effect, records every transition so the
 * [com.majid2851.navigator.map.NavigationMap] always reflects reality — even for edges that were
 * never declared in the DSL.
 *
 * Obtain one with [rememberNavigatorController].
 */
class NavigatorController internal constructor(
    val graph: NavGraph,
    initialBackStack: List<String> = listOf(graph.startRoute),
) {

    var backStack: List<String> by mutableStateOf(initialBackStack.ifEmpty { listOf(graph.startRoute) })
        private set

    private val observed = mutableStateListOf<Edge>()

    /** Transitions discovered at runtime that were not declared in the DSL. */
    val observedEdges: List<Edge> get() = observed

    /** Declared edges merged with the ones discovered while the user navigates. */
    val edges: List<Edge> get() = graph.declaredEdges + observed

    val currentRoute: String get() = backStack.last()

    val canNavigateBack: Boolean get() = backStack.size > 1

    /**
     * Navigate to [route].
     *
     * @param launchSingleTop when true and [route] is already on top, nothing happens.
     * @param popUpToStart when true the back stack is reset to `[start, route]`.
     */
    fun navigate(
        route: String,
        launchSingleTop: Boolean = false,
        popUpToStart: Boolean = false,
    ) {
        val target = graph.destinationOrNull(route)
            ?: error("Cannot navigate to unknown route '$route'. Declare it with screen(\"$route\") { }.")
        if (launchSingleTop && currentRoute == target.route) return

        recordEdge(currentRoute, target.route)
        backStack = when {
            popUpToStart -> listOf(graph.startRoute, target.route)
            else -> backStack + target.route
        }
    }

    /** Pops the top of the stack. Returns false when already at the root. */
    fun popBackStack(): Boolean {
        if (!canNavigateBack) return false
        backStack = backStack.dropLast(1)
        return true
    }

    /** Pops until [route] is on top. Returns false if [route] is not on the stack. */
    fun popTo(route: String, inclusive: Boolean = false): Boolean {
        val index = backStack.lastIndexOf(route)
        if (index == -1) return false
        val newSize = if (inclusive) index else index + 1
        if (newSize <= 0) {
            backStack = listOf(graph.startRoute)
        } else {
            backStack = backStack.subList(0, newSize).toList()
        }
        return true
    }

    /** Clears the stack back to the start destination. */
    fun navigateToRoot() {
        backStack = listOf(graph.startRoute)
    }

    private fun recordEdge(from: String, to: String) {
        if (from == to) return
        val alreadyDeclared = graph.declaredEdges.any { it.from == from && it.to == to }
        val alreadyObserved = observed.any { it.from == from && it.to == to }
        if (!alreadyDeclared && !alreadyObserved) {
            observed += Edge(from = from, to = to, declared = false)
        }
    }

    internal companion object {
        fun saver(graph: NavGraph): Saver<NavigatorController, *> =
            listSaver(
                save = { it.backStack },
                restore = { NavigatorController(graph, it) },
            )
    }
}

/**
 * Creates and remembers a [NavigatorController] for [graph]. Survives configuration changes.
 */
@Composable
fun rememberNavigatorController(graph: NavGraph): NavigatorController =
    rememberSaveable(graph, saver = NavigatorController.saver(graph)) {
        NavigatorController(graph)
    }
