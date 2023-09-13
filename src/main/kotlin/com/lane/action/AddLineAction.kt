package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class AddLineAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val cursor = editor?.caretModel?.primaryCaret
        val line = cursor?.logicalPosition?.line
        println("current line $line")

        val basePath = e.project?.basePath
        println("current project basePath: $basePath")

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null) {
            val filePath = file.path
            println("current file path:$filePath")
        }


    }
}