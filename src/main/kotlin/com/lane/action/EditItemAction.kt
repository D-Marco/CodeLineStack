package com.lane.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.lane.dataBeans.Item
import com.lane.services.MyProjectService
import org.apache.commons.lang3.StringUtils


class EditItemAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val myProjectService = project.service<MyProjectService>()
            val treeNode = myProjectService.getLastSelectedTreeNode()
            if (treeNode != null && treeNode.userObject is Item) {
                val item = treeNode.userObject as Item
                val newItemStr =
                    Messages.showInputDialog("Input new Item name", "Rename Item Name", Messages.getInformationIcon())
                if (newItemStr != null && StringUtils.isNotBlank(newItemStr)) {
                    myProjectService.updateItemName(newItemStr, item.id)
                }
            }
        }
    }
}