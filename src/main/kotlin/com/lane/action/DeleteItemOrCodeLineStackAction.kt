package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.lane.Util
import javax.swing.tree.DefaultMutableTreeNode


class DeleteItemOrCodeLineStackAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val tree = Util.getTree(project)
            val selectedNodes: Array<DefaultMutableTreeNode> = tree.getSelectedNodes(
                DefaultMutableTreeNode::class.java, null
            )
            for (item in selectedNodes) {
                println("this item is Leaf?:${item.isLeaf}")
                println("this item is root?:${item.isRoot}")
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