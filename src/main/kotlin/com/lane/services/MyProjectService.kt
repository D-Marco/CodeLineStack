package com.lane.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.treeStructure.Tree
import com.lane.MyBundle
import com.lane.dataBeans.Item
import com.lane.dataBeans.Line
import com.lane.dataBeans.LineStack
import java.io.File
import java.io.FileReader
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.xml.bind.JAXBContext

@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {
    private var tree: Tree? = null
    private var treeModel: DefaultTreeModel? = null
    private var treeRoot: DefaultMutableTreeNode? = null
    fun setTree(tree: Tree) {
        this.tree = tree
        this.treeModel = tree.model as DefaultTreeModel
        this.treeRoot = treeModel!!.root as DefaultMutableTreeNode
    }

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
//        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    /**
     * 打开选中的代码行所在的位置
     */
    fun openSelectedFile(defaultTreeModel: DefaultMutableTreeNode) {
        val line = defaultTreeModel.userObject as Line
        val lineNum = line.selectionLine
        val filePath = project.basePath + "/" + line.fileRelativePath
        var localFileSystem = LocalFileSystem.getInstance()
        val virtualFile = localFileSystem.findFileByPath(filePath)
        if (virtualFile != null) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val cursorModel = editor?.caretModel
            val document: Document = editor!!.document
            if (lineNum > 0 && lineNum <= document.lineCount) {
                cursorModel?.moveToOffset(editor.document.getLineStartOffset(lineNum - 1))
                editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
            }

        }
    }

    private fun getLineStack(): LineStack {
        val fullFilePath = project.basePath + "/codeLineStack.xml"
        val storeFile = File(fullFilePath)
        val context = JAXBContext.newInstance(LineStack::class.java)
        val unmarshaller = context.createUnmarshaller()
        return unmarshaller.unmarshal(FileReader(storeFile)) as LineStack
    }

    fun renderTree() {
        val lineStack = getLineStack()
        val itemList = lineStack.itemList
        if (itemList != null) {
            for (item in itemList) {
                addItem(item)
            }
        }
        invalidTree()
    }

    fun getDefaultItem(): Item? {
        val lineStack = getLineStack()
        val defaultItemId = lineStack.defaultItemId
        for (item in lineStack.itemList!!) {
            if (item.id == defaultItemId) {
                return item

            }
        }
        return null

    }


    fun addItem(item: Item) {
        val itemNode = DefaultMutableTreeNode()
        itemNode.userObject = item
        val lineList = item.lineList
        if (lineList != null) {
            for (line in lineList) {
                addLine(line, itemNode)
            }
        }
        treeRoot?.insert(itemNode, 0)
    }


    fun addLine(line: Line, itemNode: DefaultMutableTreeNode) {
        val lineNode = DefaultMutableTreeNode()
        lineNode.userObject = line
        itemNode.insert(lineNode, 0)
    }

    fun addLineToDefaultItem(line: Line) {
        val defaultItem = getDefaultItem()
        if (defaultItem != null) {
            val lineNode = DefaultMutableTreeNode()
            lineNode.userObject = line
            itemNode.insert(lineNode, 0)
        }

    }

    fun invalidTree() {
        treeModel?.nodeStructureChanged(treeRoot)

    }
}
