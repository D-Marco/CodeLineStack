package com.lane.util

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.lane.services.MyProjectService
import javax.swing.Icon

class MyGutterIconRenderer(val myProjectService: MyProjectService, val filePath: String, val lineNum: Int) : GutterIconRenderer() {
    private val highlightIcon = AllIcons.General.InspectionsEye

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
                    tooltip = "$tooltip$step step in $itemName\n"
                }

            }
        }


        return tooltip

    }

}