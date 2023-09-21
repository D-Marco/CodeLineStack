package com.lane.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.lane.dataBeans.Line
import com.lane.services.MyProjectService
import java.util.*


class AddLineAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val myProjectService = project?.service<MyProjectService>()
        if (myProjectService != null) {
            if (!myProjectService.existDefaultItem()) {
                showNotExistDefaultItemTip(project, "CodeLineStack")
                return
            }
            val editor = e.getData(CommonDataKeys.EDITOR)
            val cursor = editor?.caretModel?.primaryCaret
            val lineNum = cursor?.logicalPosition?.line
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
            val document = editor?.document
            if (document != null && lineNum != null && file != null) {
                val lineStartOffset = document.getLineStartOffset(lineNum)
                val lineEndOffset = document.getLineEndOffset(lineNum)
                val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))
                val fileName = file.name
                val basePath = e.project?.basePath
                if (basePath != null) {
                    val filePath = file.path.substring(basePath.length + 1)
                    val line = Line()
                    line.text = lineText
                    line.selectionLine = lineNum
                    line.fileName = fileName
                    line.fileRelativePath = filePath
                    line.id = UUID.randomUUID().toString()

                    if (myProjectService.existDefaultItem()) {
                        myProjectService.addLineToDefaultItem(line)
                        val markupModel: MarkupModel = editor.markupModel
                        val rangeHighlighter: RangeHighlighter = markupModel.addLineHighlighter(lineNum, HighlighterLayer.ERROR, null)

                    }
                }
            }
        }
    }

    private fun showNotExistDefaultItemTip(project: Project, pluginId: String) {
        val title = "CodeLineStack"
        val content = "The default item has not been set yet"

        val notification = Notification(
            pluginId,
            title,
            content,
            NotificationType.WARNING
        )

        Notifications.Bus.notify(notification, project)
    }
}