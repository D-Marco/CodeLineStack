package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.lane.dataBeans.Item
import com.lane.dataBeans.Line
import com.lane.services.MyProjectService


class MoveDownItemOrLineAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            val treeNode = myProjectService.getLastSelectedTreeNode()
            if (treeNode != null) {
                when (treeNode.userObject) {
                    is Item -> {
                        myProjectService.downItemTreeNode(treeNode)
                    }

                    is Line -> {
                        myProjectService.downLineTreeNode(treeNode)
                    }
                }
            }
        }
    }
}