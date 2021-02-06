package com.ihd2.graphics

import java.awt.Color

interface Renderable {
    fun render(color: Color, renderer: GraphicsRenderer)
}