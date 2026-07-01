package com.majid2851.navigator.runtime

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Provides the current [NavigatorController] to the composition tree. Useful for deeply nested
 * composables that want to navigate without threading callbacks through every layer.
 */
val LocalNavigator = staticCompositionLocalOf<NavigatorController> {
    error("No NavigatorController provided. Wrap your UI in a NavigatorHost.")
}

/**
 * Renders the destination currently on top of [controller]'s back stack and animates between
 * screens. Also wires Android's system back button to [NavigatorController.popBackStack].
 *
 * @param handleSystemBack set to false if you manage the back button yourself.
 */
@Composable
fun NavigatorHost(
    controller: NavigatorController,
    modifier: Modifier = Modifier,
    handleSystemBack: Boolean = true,
) {
    if (handleSystemBack) {
        BackHandler(enabled = controller.canNavigateBack) {
            controller.popBackStack()
        }
    }

    CompositionLocalProvider(LocalNavigator provides controller) {
        AnimatedContent(
            targetState = controller.currentRoute,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = modifier,
            label = "NavigatorHost",
        ) { route ->
            val destination = controller.graph.destinationOrNull(route)
            if (destination != null) {
                val scope = remember(route) { ScreenScope(controller, route) }
                destination.content(scope)
            }
        }
    }
}
