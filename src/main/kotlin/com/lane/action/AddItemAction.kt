package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.lane.dataBeans.Item
import com.lane.services.MyProjectService
import org.apache.commons.lang3.StringUtils
import java.util.UUID


class AddItemAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            val newItemStr =
                Messages.showInputDialog("Input Item name", "Add An Item", Messages.getInformationIcon())
            if (StringUtils.isNotBlank(newItemStr)) {
                val newItem = Item()
                newItem.id = UUID.randomUUID().toString()
                newItem.name = newItemStr!!
                myProjectService.addItem(newItem)
                myProjectService.invalidTree()
            }

        }
    }
}