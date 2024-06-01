package com.example.musicalignapp.ui.screens.align.stylus

import android.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

data class StylusState(
    var path: Path = Path(),
    var stroke: Stroke? = null
)