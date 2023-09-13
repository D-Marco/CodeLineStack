package com.lane

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import javax.swing.JPanel

class Util {

    companion object {
        fun getTree(project: Project): Tree {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CodeLineStackToolWindow")
            val firstContentOfToolWindow = toolWindow?.contentManager?.getContent(0)
            val scrollPane = firstContentOfToolWindow?.component as JBScrollPane
            val jPanel = scrollPane.viewport.getComponent(0) as JPanel
            return jPanel.getComponent(0) as Tree
        }
    }
}