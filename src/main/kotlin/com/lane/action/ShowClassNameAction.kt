package com.lane.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.lane.services.MyProjectService


class ShowClassNameAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            myProjectService.switchShowClassNameValue()
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            val showClassNameValue = myProjectService.showClassNameValue()
            e.presentation.icon = if (showClassNameValue) AllIcons.Actions.Checked else null
        }


    }
}