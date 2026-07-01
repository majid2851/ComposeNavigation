package com.majid2851.navigator.map

import com.majid2851.navigator.model.Edge
import com.majid2851.navigator.model.NavGraph

/** Grid coordinate of a node: [layer] is the horizontal column, [index] the vertical slot. */
data class NodePosition(val route: String, val layer: Int, val index: Int)

/** Result of the layered layout: where every node sits plus overall dimensions in grid units. */
data class GraphLayout(
    val positions: Map<String, NodePosition>,
    val layerCount: Int,
    val maxLayerSize: Int,
    val reachable: Set<String>,
)

/**
 * Assigns every destination to a column based on its shortest distance (in edges) from the start
 * destination — a simple layered / BFS layout that reads left-to-right like the classic Navigation
 * Component editor. Unreachable screens are placed in a trailing "orphans" column so they are still
 * visible on the map.
 */
fun computeGraphLayout(graph: NavGraph, edges: List<Edge>): GraphLayout {
    val adjacency: Map<String, List<String>> = edges
        .filter { it.from in graph.routes && it.to in graph.routes }
        .groupBy({ it.from }, { it.to })

    val depth = LinkedHashMap<String, Int>()
    val queue = ArrayDeque<String>()
    depth[graph.startRoute] = 0
    queue.addLast(graph.startRoute)

    while (queue.isNotEmpty()) {
        val node = queue.removeFirst()
        val nodeDepth = depth.getValue(node)
        adjacency[node]?.forEach { next ->
            if (next !in depth) {
                depth[next] = nodeDepth + 1
                queue.addLast(next)
            }
        }
    }

    val reachable = depth.keys.toSet()
    val reachedMaxDepth = depth.values.maxOrNull() ?: 0
    // Any destination never reached from start becomes an orphan in a trailing column.
    graph.routes.forEach { route ->
        if (route !in depth) depth[route] = reachedMaxDepth + 1
    }

    val byLayer: Map<Int, List<String>> = depth.entries
        .groupBy(keySelector = { it.value }, valueTransform = { it.key })

    val positions = LinkedHashMap<String, NodePosition>()
    byLayer.keys.sorted().forEach { layer ->
        byLayer.getValue(layer)
            .sorted()
            .forEachIndexed { index, route ->
                positions[route] = NodePosition(route, layer, index)
            }
    }

    val layerCount = (byLayer.keys.maxOrNull() ?: 0) + 1
    val maxLayerSize = byLayer.values.maxOfOrNull { it.size } ?: 1
    return GraphLayout(positions, layerCount, maxLayerSize, reachable)
}

/** Consecutive pairs of the back stack, i.e. the edges currently highlighted as the active path. */
fun activePathEdges(backStack: List<String>): Set<Pair<String, String>> =
    backStack.zipWithNext().toSet()
