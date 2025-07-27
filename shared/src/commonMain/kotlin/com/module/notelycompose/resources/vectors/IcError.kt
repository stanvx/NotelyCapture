package com.module.notelycompose.resources.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin.Companion.Round
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import dev.sergiobelda.compose.vectorize.core.imageVector

public val Images.Icons.IcError: ImageVector
    get() {
        if (_icError != null) {
            return _icError!!
        }
        _icError = imageVector(
            name = "IcError",
            width = 24f,
            height = 24f,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
            autoMirror = false
        ) {
            // Error circle
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineJoin = Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(12.0f, 2.0f)
                curveToRelative(5.523f, 0.0f, 10.0f, 4.477f, 10.0f, 10.0f)
                reflectiveCurveToRelative(-4.477f, 10.0f, -10.0f, 10.0f)
                reflectiveCurveToRelative(-10.0f, -4.477f, -10.0f, -10.0f)
                reflectiveCurveToRelative(4.477f, -10.0f, 10.0f, -10.0f)
                close()
            }
            // X mark
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineJoin = Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(15.0f, 9.0f)
                lineToRelative(-6.0f, 6.0f)
            }
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineJoin = Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(9.0f, 9.0f)
                lineToRelative(6.0f, 6.0f)
            }
        }
        return _icError!!
    }

private var _icError: ImageVector? = null