package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.lane.dataBeans.Item
import com.lane.dataBeans.Line
import com.lane.listeners.MyFileEditorManagerListener
import com.lane.services.MyProjectService


class DeleteItemOrLineAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val currentFile = FileEditorManager.getInstance(project).selectedFiles[0]
            val fileEditors = FileEditorManager.getInstance(project).getEditors(currentFile)
            var editor: Editor? = null

            for (fileEditor in fileEditors) {
                if (fileEditor is TextEditor) {
                    editor = fileEditor.editor
                    break
                }
            }


            val myProjectService = project.service<MyProjectService>()
            val treeNode = myProjectService.getLastSelectedTreeNode()
            if (treeNode != null) {
                when (treeNode.userObject) {
                    is Item -> {
                        val userObj = treeNode.userObject as Item
                        if (userObj.lineList != null) {
                            for (it in userObj.lineList!!) {
                                val lineWithItemArray = myProjectService.getLineWithItemListByFileNameAndLineNumber(
                                    it.fileRelativePath,
                                    it.selectionLine
                                )
                                if (lineWithItemArray != null && lineWithItemArray.size == 1) {
                                    MyFileEditorManagerListener.removeHighlighter(
                                        myProjectService,
                                        it.selectionLine,
                                        editor
                                    )
                                }

                            }
                        }
                        myProjectService.deleteItem(userObj.id)//后删数据是为了前面的userObj.lineList数据不为空，如果先删数据，userObj.lineList为空走不进判断 ，则无法删除标记

                    }

                    is Line -> {
                        val userObj = treeNode.userObject as Line
                        myProjectService.deleteLine(userObj.id, treeNode)
                        if (myProjectService.getLineWithItemListByFileNameAndLineNumber(
                                userObj.fileRelativePath,
                                userObj.selectionLine
                            )?.size!! < 1
                        ) {
                            MyFileEditorManagerListener.removeHighlighter(
                                myProjectService,
                                userObj.selectionLine,
                                editor
                            )
                        }

                    }
                }
            }
        }
    }
}