package com.lane.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.lane.services.MyProjectService

class MyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
//        val project = e.project
//        val projectService = project?.service<MyProjectService>()
//        val numb = projectService?.getRandomNumber()
//
//        Notifications.Bus.notify(
//            Notification("code line stack", "numb=" + numb, NotificationType.INFORMATION), e.project
//        );

    }

}