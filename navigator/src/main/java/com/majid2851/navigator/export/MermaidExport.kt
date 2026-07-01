package com.majid2851.navigator.export

import com.majid2851.navigator.model.Edge
import com.majid2851.navigator.model.NavGraph

/**
 * Exports a [NavGraph] (optionally merged with runtime-[observed] edges) as a
 * [Mermaid](https://mermaid.js.org/) flow-chart string. Paste the result into a README, a PR
 * description or any Mermaid renderer to get a versionable picture of your navigation.
 *
 * ```
 * val diagram = graph.toMermaid(controller.observedEdges)
 * ```
 */
fun NavGraph.toMermaid(observed: List<Edge> = emptyList()): String {
    val builder = StringBuilder()
    builder.appendLine("graph LR")

    val ids = HashMap<String, String>()
    destinations.values.forEachIndexed { index, destination ->
        val id = "n$index"
        ids[destination.route] = id
        val marker = if (destination.route == startRoute) "([${destination.label}])" else "[${destination.label}]"
        builder.appendLine("    $id$marker")
    }

    val allEdges = declaredEdges + observed
    allEdges.forEach { edge ->
        val from = ids[edge.from] ?: return@forEach
        val to = ids[edge.to] ?: return@forEach
        val arrow = if (edge.declared) "-->" else "-.->"
        if (edge.label.isNullOrBlank()) {
            builder.appendLine("    $from $arrow $to")
        } else {
            builder.appendLine("    $from $arrow|${edge.label}| $to")
        }
    }

    return builder.toString().trimEnd()
}
