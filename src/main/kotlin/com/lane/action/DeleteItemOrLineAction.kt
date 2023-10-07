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
import javax.swing.tree.DefaultMutableTreeNode


class DeleteItemOrLineAction : AnAction() {
    private fun deleteItemInFile(treeNode: DefaultMutableTreeNode, editor: Editor, myProjectService: MyProjectService) {
        val userObj = treeNode.userObject as Item
        if (userObj.lineList != null) {
            for (it in userObj.lineList!!) {
                val lineWithItemArray = myProjectService.getLineWithItemListByFileNameAndLineNumber(
                    it.fileRelativePath,
                    it.selectionLine
                )
                if (lineWithItemArray != null && lineWithItemArray.size == 1) {
                    MyFileEditorManagerListener.removeHighlighter(
                        it.selectionLine,
                        editor
                    )
                }
            }
        }
    }


    private fun deleteLineInFile(treeNode: DefaultMutableTreeNode, editor: Editor, myProjectService: MyProjectService) {
        val userObj = treeNode.userObject as Line
        myProjectService.deleteLine(userObj.id, treeNode)
        if (myProjectService.getLineWithItemListByFileNameAndLineNumber(
                userObj.fileRelativePath,
                userObj.selectionLine
            )?.size!! < 1
        ) {
            MyFileEditorManagerListener.removeHighlighter(
                userObj.selectionLine,
                editor
            )
        }


    }

    override fun actionPerformed(e:AnActionEvent) {
        val project = e.project
        if (project != null) {
            val allEditors = FileEditorManager.getInstance(project).allEditors
            val myProjectService = project.service<MyProjectService>()
            val treeNode = myProjectService.getLastSelectedTreeNode() ?: return
            for (editor in allEditors) {
                if (editor is TextEditor) {
                    when (treeNode.userObject) {
                        is Item -> {
                            deleteItemInFile(treeNode, editor.editor, project.service<MyProjectService>())
                        }

                        is Line -> {
                            deleteLineInFile(treeNode, editor.editor, project.service<MyProjectService>())
                        }
                    }
                }
            }
            val userObj = treeNode.userObject
            if (userObj is Item) {
                myProjectService.deleteItem(userObj.id)//后删数据是为了前面的userObj.lineList数据不为空，如果先删数据，userObj.lineList为空走不进判断 ，则无法删除标记
            }
        }
    }
}