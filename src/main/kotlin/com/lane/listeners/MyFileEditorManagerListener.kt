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
        document.addDocumentListener(object : DocumentListener {
            var beforeCaretNumber = 0
            private var lastLineCount = document.lineCount
            override fun beforeDocumentChange(event: DocumentEvent) {
                val editor: Editor? = FileEditorManager.getInstance(myProjectService.project).selectedTextEditor
                if (editor != null) {
                    val caretModel: CaretModel = editor.caretModel
                    val caretOffset = caretModel.offset
                    val lineNumber = document.getLineNumber(caretOffset)
                    beforeCaretNumber = lineNumber
                }
                super.beforeDocumentChange(event)
            }

            override fun documentChanged(event: DocumentEvent) {
                super.documentChanged(event)
                val lineList: ArrayList<Line>? = myProjectService.getLineListByFileName(fileRelationPath)

                if (lineList != null) {
                    val currentLineCount = document.lineCount
                    val offset = event.offset
//                    val lineNumber = document.getLineNumber(offset)
//                    println("lineNumber :$lineNumber  beforelinenumber:$beforeCaretNumber")
                    val insertedLines: Int = currentLineCount - lastLineCount
//                    println("插入了 $insertedLines 行")
                    lastLineCount = currentLineCount
                    val showBeRemoveLineList: ArrayList<Line> = ArrayList()
                    for (line in lineList) {
                        if (beforeCaretNumber < line.selectionLine) {
                            line.selectionLine += insertedLines
                            myProjectService.updateTree()
                        } else if (beforeCaretNumber == line.selectionLine && insertedLines < 0) {
                            myProjectService.deleteLine(line.id)
                            showBeRemoveLineList.add(line)
                        }
                    }
                    if (showBeRemoveLineList.size > 0) {
                        lineList.removeAll(showBeRemoveLineList.toSet())
                    }
                    myProjectService.saveLineStack()
                }
            }

        }, fileEditor)
    }
}