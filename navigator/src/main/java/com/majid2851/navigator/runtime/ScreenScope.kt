package com.majid2851.navigator.runtime

import androidx.compose.runtime.Immutable

/**
 * Receiver available inside a `screen { }` block. Gives each destination access to the
 * [navigator] so it can trigger navigation, and to its own [route].
 */
@Immutable
class ScreenScope internal constructor(
    val navigator: NavigatorController,
    val route: String,
)
