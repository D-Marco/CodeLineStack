package com.lane.util

import java.awt.Graphics
import javax.swing.Icon
import javax.swing.JLabel


class MultiIconLabel(image: Icon?) : JLabel(image) {
    private var icons: MutableList<Icon>? = null

    fun MultiIconLabel() {
        icons = ArrayList()
    }

    fun addIcon(icon: Icon) {
        icons!!.add(icon)
        repaint() // 重新绘制标签以包含新图标
    }

    fun removeIcon(icon: Icon) {
        icons!!.remove(icon)
        repaint() // 重新绘制标签以移除图标
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        var x = 0
        for (icon in icons!!) {
            icon.paintIcon(this, g, x, 0)
            x += icon.iconWidth
        }
    }
}