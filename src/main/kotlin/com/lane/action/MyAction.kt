package com.lane.action

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class MyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        Notifications.Bus.notify(
            Notification("code line stack", "欢迎来到插件世界！", NotificationType.INFORMATION),e.project
        );
    }
}