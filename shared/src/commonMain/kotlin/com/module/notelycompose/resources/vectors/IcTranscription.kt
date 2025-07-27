package com.module.notelycompose.resources.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import dev.sergiobelda.compose.vectorize.core.imageVector

public val Images.Icons.IcTranscription: ImageVector
    get() {
        if (_icTranscription != null) {
            return _icTranscription!!
        }
        _icTranscription = imageVector(
            name = "IcTranscription",
            width = 24f,
            height = 24f,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
            autoMirror = false
        ) {
            // Document/file icon
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = Round,
                strokeLineJoin = StrokeJoin.Companion.Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(14.0f, 2.0f)
                horizontalLineTo(6.0f)
                curveTo(5.47f, 2.0f, 4.961f, 2.211f, 4.586f, 2.586f)
                curveTo(4.211f, 2.961f, 4.0f, 3.47f, 4.0f, 4.0f)
                verticalLineTo(20.0f)
                curveTo(4.0f, 20.53f, 4.211f, 21.039f, 4.586f, 21.414f)
                curveTo(4.961f, 21.789f, 5.47f, 22.0f, 6.0f, 22.0f)
                horizontalLineTo(18.0f)
                curveTo(18.53f, 22.0f, 19.039f, 21.789f, 19.414f, 21.414f)
                curveTo(19.789f, 21.039f, 20.0f, 20.53f, 20.0f, 20.0f)
                verticalLineTo(8.0f)
                lineTo(14.0f, 2.0f)
                close()
            }
            // Corner fold
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = Round,
                strokeLineJoin = StrokeJoin.Companion.Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(14.0f, 2.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(20.0f)
            }
            // Text lines
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = Round,
                strokeLineJoin = StrokeJoin.Companion.Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(16.0f, 13.0f)
                horizontalLineTo(8.0f)
            }
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = Round,
                strokeLineJoin = StrokeJoin.Companion.Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(16.0f, 17.0f)
                horizontalLineTo(8.0f)
            }
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = Round,
                strokeLineJoin = StrokeJoin.Companion.Round,
                strokeLineWidth = 2.0f
            ) {
                moveTo(10.0f, 9.0f)
                horizontalLineTo(8.0f)
            }
        }
        return _icTranscription!!
    }

private var _icTranscription: ImageVector? = null