package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.ui.treeStructure.Tree
import com.lane.services.MyProjectService

class MakeItemAsDefaultAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val event = e.dataContext
            val tree: Tree? = event.getData(Tree.SELECTION_MODEL_PROPERTY)
            val myProjectService = project.service<MyProjectService>()
            val editor  = e.getData(CommonDataKeys.EDITOR)
            val selectedText = editor!!.selectionModel.selectedText
            println(selectedText)
        }
    }
}