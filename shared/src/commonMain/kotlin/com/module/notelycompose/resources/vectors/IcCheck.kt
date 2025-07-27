package com.module.notelycompose.resources.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import dev.sergiobelda.compose.vectorize.core.imageVector

public val Images.Icons.IcCheck: ImageVector
    get() {
        if (_icCheck != null) {
            return _icCheck!!
        }
        _icCheck = imageVector(
            name = "IcCheck",
            width = 24f,
            height = 24f,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
            autoMirror = false
        ) {
            // Checkmark path
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = Round,
                strokeLineJoin = StrokeJoin.Companion.Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(20.0f, 6.0f)
                lineToRelative(-11.0f, 11.0f)
                lineToRelative(-5.0f, -5.0f)
            }
        }
        return _icCheck!!
    }

private var _icCheck: ImageVector? = null