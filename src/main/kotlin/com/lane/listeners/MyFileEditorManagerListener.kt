package com.lane.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile
import com.lane.dataBeans.Line
import com.lane.dataBeans.LineWithItem
import com.lane.services.MyProjectService
import java.util.ArrayList


class MyFileEditorManagerListener : FileEditorManagerListener {
    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        super.fileOpened(source, file)
        val openProjects: Array<Project> = ProjectManager.getInstance().openProjects
        val myProjectService = openProjects[0].service<MyProjectService>()
        val fileEditor = source.getSelectedEditor(file)
        val basePath = myProjectService.project.basePath
        val fileRelationPath = file.path.substring(basePath!!.length + 1)
        val document: Document? = FileDocumentManager.getInstance().getDocument(file)
        if (fileEditor == null || document == null) {
            return
        }
        if (myProjectService.getLineListByFileName(fileRelationPath) == null) {
            return
        }
        document.addDocumentListener(object : DocumentListener {
            var beforeCaretNumber = -1
            private var lastLineCount = document.lineCount
            override fun beforeDocumentChange(event: DocumentEvent) {
                val editor: Editor? = FileEditorManager.getInstance(myProjectService.project).selectedTextEditor
                if (editor != null) {
                    val caretModel: CaretModel = editor.caretModel
                    val caretOffset = caretModel.offset
                    if (caretOffset <= document.textLength) {
                        val lineNumber = document.getLineNumber(caretOffset)
                        beforeCaretNumber = lineNumber
                    }

                }
                super.beforeDocumentChange(event)
            }

            override fun documentChanged(event: DocumentEvent) {
                super.documentChanged(event)
                val lineWithItemList: ArrayList<LineWithItem>? = myProjectService.getLineListByFileName(fileRelationPath)

                if (lineWithItemList != null) {
                    val currentLineCount = document.lineCount
                    val offset = event.offset
                    val lineNumber = document.getLineNumber(offset)
                    println("lineNumber :$lineNumber  beforelinenumber:$beforeCaretNumber")
                    val insertedLines: Int = currentLineCount - lastLineCount
                    println("插入了 $insertedLines 行")
                    lastLineCount = currentLineCount
                    val showBeRemoveLineList: ArrayList<LineWithItem> = ArrayList()
                    for (lineWithItem in lineWithItemList) {
                        if (insertedLines > 0) {//插入

                        }else if (insertedLines < 0) {//删除

                        }

                        if (beforeCaretNumber < lineWithItem.line.selectionLine && beforeCaretNumber > -1) {
                            lineWithItem.line.selectionLine += insertedLines
                            myProjectService.updateTree()
                        } else if (beforeCaretNumber == lineWithItem.line.selectionLine && insertedLines < 0) {
                            myProjectService.deleteLine(lineWithItem.line.id)
                            showBeRemoveLineList.add(LineWithItem(lineWithItem.line,null))
                        }
                    }
                    if (showBeRemoveLineList.size > 0) {
                        lineWithItemList.removeAll(showBeRemoveLineList.toSet())
                    }
                    myProjectService.saveLineStack()
                }
            }

        }, fileEditor)
    }
}