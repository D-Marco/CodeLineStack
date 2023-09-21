package com.lane.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.lane.services.MyProjectService


class ShowLineIndexAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            myProjectService.switchLineIndexNumberValue()
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            val showLineIndexNumber = myProjectService.showLineIndexNumberValue()
            e.presentation.icon = if (showLineIndexNumber) AllIcons.Actions.Checked else null
        }


    }
}