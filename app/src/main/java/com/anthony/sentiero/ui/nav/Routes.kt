// UI layer (navigation): the app's destinations as typed routes. @Serializable lets
// Navigation Compose turn each one into a route string, so arguments like the hike id
// are checked by the compiler instead of being typed into strings by hand.
package com.anthony.sentiero.ui.nav

import kotlinx.serialization.Serializable

@Serializable
object Upcoming

@Serializable
data class HikeForm(val editId: String? = null) // editId stays unused until "Edit hike" (P3)

@Serializable
data class HikeDetail(val id: String)
