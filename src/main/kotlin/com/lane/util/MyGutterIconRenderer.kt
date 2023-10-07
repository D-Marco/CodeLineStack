package com.lane.util

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.IconLoader
import com.lane.services.MyProjectService
import javax.swing.Icon

class MyGutterIconRenderer(val myProjectService: MyProjectService, private val filePath: String, private val lineNum: Int) : GutterIconRenderer() {
    private val highlightIcon = IconLoader.getIcon("/META-INF/stack.svg", javaClass)

    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return 0
    }

    override fun getIcon(): Icon {
        return highlightIcon
    }

    override fun getTooltipText(): String {
        val allRelLineWithItemList = myProjectService.getLineWithItemListByFileNameAndLineNumber(filePath, lineNum)
        var tooltip = ""
        if (allRelLineWithItemList != null) {
            for (it in allRelLineWithItemList) {
                if (it.line.selectionLine == lineNum) {
                    val step = it.item?.lineList?.indexOf(it.line)!! + 1
                    val itemName = it.item.name
                    tooltip = "$tooltip$step step of [$itemName]\n"
                }

            }
        }


        return tooltip

    }

}