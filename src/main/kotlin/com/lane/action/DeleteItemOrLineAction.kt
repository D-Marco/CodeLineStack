package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.lane.dataBeans.Item
import com.lane.dataBeans.Line
import com.lane.listeners.MyFileEditorManagerListener
import com.lane.services.MyProjectService


class DeleteItemOrLineAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val editor = e.getData(CommonDataKeys.EDITOR)
            val myProjectService = project.service<MyProjectService>()
            val treeNode = myProjectService.getLastSelectedTreeNode()
            if (treeNode != null) {
                if (editor == null) {
                    return
                }
                when (treeNode.userObject) {
                    is Item -> {
                        val userObj = treeNode.userObject as Item
                        myProjectService.deleteItem(userObj.id)
                        if (userObj.lineList != null) {
                            for (it in userObj.lineList!!) {
                                MyFileEditorManagerListener.updateEyeState(
                                    myProjectService,
                                    it.fileRelativePath,
                                    editor
                                )

                            }
                        }

                    }

                    is Line -> {
                        val userObj = treeNode.userObject as Line
                        myProjectService.deleteLine(userObj.id, treeNode)
                        MyFileEditorManagerListener.updateEyeState(
                            myProjectService,
                            userObj.fileRelativePath,
                            editor
                        )

                    }
                }
            }
        }
    }
}