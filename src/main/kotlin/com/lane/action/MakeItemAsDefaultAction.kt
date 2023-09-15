package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.lane.dataBeans.Item
import com.lane.dataBeans.Line
import com.lane.services.MyProjectService


class MakeItemAsDefaultAction : AnAction() {

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            val treeNode = myProjectService.getLastSelectedTreeNode()
            if (treeNode != null) {
                when (val nodeUserObj = treeNode.userObject) {
                    is Line -> {
                        println(nodeUserObj.selectionLine)
                    }
                    is Item -> {
                        myProjectService.makeItemAsDefault(nodeUserObj)
                    }
                }

            }

        }
    }
}