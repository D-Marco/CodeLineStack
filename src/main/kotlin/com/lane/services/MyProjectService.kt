package com.lane.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.lane.MyBundle
import javax.swing.tree.DefaultMutableTreeNode

@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {
    private var globalProject: Project

    init {
        globalProject = project
        thisLogger().info(MyBundle.message("projectService", project.name))
//        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    fun getRandomNumber() = (1..100).random()

    fun openSelectedFile(defaultTreeModel: DefaultMutableTreeNode) {
        val nodeStr = defaultTreeModel.userObject.toString()
        val line = 82
        println("selected node text:$nodeStr")
        val filePath = "E:/util_project/gateway_debug/gateway-mi/src/main/java/com/linewell/gateway/config/RabbitMqConfig.java"
        var localFileSystem = LocalFileSystem.getInstance()
        val virtualFile = localFileSystem.findFileByPath(filePath)
        if (virtualFile != null) {
            FileEditorManager.getInstance(globalProject).openFile(virtualFile, true)
            val editor = FileEditorManager.getInstance(globalProject).selectedTextEditor
            val cursorModel = editor?.caretModel
            val document: Document = editor!!.document
            if (line > 0 && line <= document.getLineCount()) {
                cursorModel?.moveToOffset(editor.document.getLineStartOffset(line-1))
                editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
            }

        }
    }
}
