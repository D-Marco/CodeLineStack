package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.lane.Util
import org.apache.commons.lang3.StringUtils
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode


class AddItemAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val tree = Util.getTree(project)
            val treeModel = tree.model as DefaultTreeModel
            val root = treeModel.root as DefaultMutableTreeNode

            val newItem = Messages.showInputDialog("Input Item name", "Add An Item", Messages.getInformationIcon())
            if (StringUtils.isNotBlank(newItem)) {
                root.add(buildChild(newItem.toString()))
                treeModel.nodeStructureChanged(root)
            }
        }
    }

    private fun buildChild(name: String): DefaultMutableTreeNode {
        val childLeaf = DefaultMutableTreeNode()
        childLeaf.userObject = "childLeaf$name"
        val child = DefaultMutableTreeNode();
        child.add(childLeaf)
        child.userObject = "child$name"
        return child
    }

}