package com.majid2851.navigator.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.majid2851.navigator.runtime.ScreenScope

/**
 * A single screen (node) in the navigation map.
 *
 * @param route unique identifier of the destination, also used to navigate to it.
 * @param label human readable title shown on the map node.
 * @param group optional grouping (e.g. "Auth", "Main") used to color / cluster nodes.
 * @param icon optional icon rendered on the map node.
 * @param content the composable rendered by [com.majid2851.navigator.runtime.NavigatorHost]
 *  when this destination is on top of the back stack.
 */
@Immutable
class Destination(
    val route: String,
    val label: String,
    val group: String? = null,
    val icon: ImageVector? = null,
    val content: @Composable ScreenScope.() -> Unit,
)
