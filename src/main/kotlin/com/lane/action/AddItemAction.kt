package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import org.apache.commons.lang3.StringUtils
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode


class AddItemAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CodeLineStackToolWindow")
            val firstContentOfToolWindow = toolWindow?.contentManager?.getContent(0)
            val scrollPane = firstContentOfToolWindow?.component as JBScrollPane
            val tree = scrollPane.getComponent(0) as Tree
            val treeModel = tree.model as DefaultTreeModel
            val root = treeModel.root as DefaultTreeModel

            val newItem = Messages.showInputDialog("Input Item name", "Add An Item", Messages.getInformationIcon())
            if (StringUtils.isNotBlank(newItem)) {
                root.add(buildChild(newItem.toString()))
            }
        }

    }
}