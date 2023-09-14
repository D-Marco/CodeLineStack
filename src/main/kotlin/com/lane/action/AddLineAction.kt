package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.lane.dataBeans.Line
import com.lane.services.MyProjectService


class AddLineAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val cursor = editor?.caretModel?.primaryCaret
        val lineNum = cursor?.logicalPosition?.line
        println("current line $lineNum")

        val basePath = e.project?.basePath
        println("current project basePath: $basePath")

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null) {
        }
        val document = editor?.document
        if (document != null && lineNum != null && file != null) {
            val lineStartOffset = document.getLineStartOffset(lineNum)
            val lineEndOffset = document.getLineEndOffset(lineNum)
            val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
            val fileName = file.name
            val filePath = file.path

            val line = Line()
            line.text = lineText
            line.selectionLine = lineNum
            line.fileName = fileName
            line.fileRelativePath = filePath

            val project = e.project
            val myProjectService = project?.service<MyProjectService>()
                myProjectService!!.addLine(line,)

        }


    }
}