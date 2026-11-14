// UI layer: the corner-radius scale from DESIGN.md (squared-off, like trail signs).
// Material components pick these up by size, so screens rarely name a radius themselves.
package com.anthony.sentiero.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SentieroShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),  // tags, checkboxes
    small = RoundedCornerShape(6.dp),       // chips, text fields
    medium = RoundedCornerShape(8.dp),      // buttons, forecast panel
    large = RoundedCornerShape(12.dp),      // FAB
    extraLarge = RoundedCornerShape(20.dp), // bottom-sheet top corners
)
