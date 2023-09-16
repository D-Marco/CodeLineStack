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
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.xml.bind.JAXBContext

@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {
    @Volatile
    private var defaultItem: Item? = null
    private var tree: Tree? = null
    private var treeModel: DefaultTreeModel? = null
    private var treeRoot: DefaultMutableTreeNode? = null
    private var lastSelectedTreeNode: DefaultMutableTreeNode? = null

    fun getProject(): Project {
        return project
    }

    fun setTree(tree: Tree) {
        this.tree = tree
        this.treeModel = tree.model as DefaultTreeModel
        this.treeRoot = treeModel!!.root as DefaultMutableTreeNode
    }


    fun setLastSelectedTreeNode(node: DefaultMutableTreeNode) {
        lastSelectedTreeNode = node
    }

    fun getLastSelectedTreeNode(): DefaultMutableTreeNode? {
        return lastSelectedTreeNode
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
        val localFileSystem = LocalFileSystem.getInstance()
        val virtualFile = localFileSystem.findFileByPath(filePath)
        if (virtualFile != null) {
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val cursorModel = editor?.caretModel
            val document: Document = editor!!.document
            if (lineNum >= 0 && lineNum < document.lineCount) {
                cursorModel?.moveToOffset(editor.document.getLineStartOffset(lineNum))
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

    private fun saveLineStack(lineStack: LineStack) {
        val fullFilePath = project.basePath + "/codeLineStack.xml"
        val context = JAXBContext.newInstance(LineStack::class.java)
        val marshaller = context.createMarshaller()
        marshaller.marshal(lineStack, FileOutputStream(fullFilePath))
    }

    fun renderTree() {
        val lineStack = getLineStack()
        val itemList = lineStack.itemList
        treeRoot?.removeAllChildren()
        if (itemList != null) {
            for (item in itemList) {
                addItemNode(item)
            }
        }
        treeModel?.nodeStructureChanged(treeRoot)
    }

    private fun addItemNode(item: Item) {
        val itemNode = DefaultMutableTreeNode()
        itemNode.userObject = item
        val lineList = item.lineList
        if (lineList != null) {
            for (line in lineList) {
                addLineNode(line, itemNode)
            }
        }
        treeRoot?.insert(itemNode, 0)
    }


    private fun addLineNode(line: Line, itemNode: DefaultMutableTreeNode) {
        val lineNode = DefaultMutableTreeNode()
        lineNode.userObject = line
        itemNode.insert(lineNode, 0)
    }

    private fun getItemTreeNodeByItemId(itemId: String): DefaultMutableTreeNode? {
        val itemNodeList = treeRoot?.children()
        if (itemNodeList != null) {
            for (itemNode in itemNodeList) {
                val defaultMutableTreeNode: DefaultMutableTreeNode = itemNode as DefaultMutableTreeNode
                if (defaultMutableTreeNode.userObject is Item) {
                    val currentItem = defaultMutableTreeNode.userObject as Item
                    if (currentItem.id == itemId) {
                        return itemNode
                    }
                }
            }
        }
        return null
    }

    fun addItem(itemName: String) {
        val newItem = Item()
        newItem.id = UUID.randomUUID().toString()
        newItem.name = itemName
        val lineStack = getLineStack()
        if (lineStack.itemList == null) {
            lineStack.itemList = ArrayList()
        }
        lineStack.itemList?.add(newItem)
        saveLineStack(lineStack)

        val newNode = DefaultMutableTreeNode()
        newNode.userObject = newItem
        treeRoot?.insert(newNode, 0)
        if (lineStack.itemList?.size == 1) {
            treeModel?.nodeStructureChanged(treeRoot)
        }
        tree?.updateUI()
    }

    fun deleteItem(itemId: String) {
        val lineStack = getLineStack()
        val itemList = lineStack.itemList
        val filteredItemList = itemList?.filter { it.id != itemId }
        lineStack.itemList = filteredItemList?.toMutableList() as ArrayList<Item>
        saveLineStack(lineStack)

        val targetItem = getItemTreeNodeByItemId(itemId)
        if (targetItem != null) {
            treeRoot?.remove(targetItem)
            tree?.updateUI()
        }
    }


    fun updateItemName(newName: String, selectedItemId: String) {
        val lineStack = getLineStack()
        val itemList = lineStack.itemList
        itemList?.forEach { item ->
            run {
                if (item.id == selectedItemId) {
                    item.name = newName
                }
            }
        }
        saveLineStack(lineStack)

        val targetItem = getItemTreeNodeByItemId(selectedItemId)
        if (targetItem != null) {
            val item = targetItem.userObject as Item
            item.name = newName
        }
    }

    fun makeItemAsDefault(item: Item) {
        this.defaultItem = item
        val lineStack = getLineStack()
        lineStack.defaultItemId = item.id
        saveLineStack(lineStack)
        tree?.updateUI()
    }

    fun getDefaultItem(): Item? {
        if (defaultItem == null) {
            val lineStack = getLineStack()
            val defaultItemId = lineStack.defaultItemId
            val itemList = lineStack.itemList
            if (itemList != null) {
                for (item in itemList) {
                    if (item.id == defaultItemId) {
                        defaultItem = item
                        break
                    }
                }
            }

        }
        return defaultItem
    }

    fun existDefaultItem(): Boolean {
        val lineStack = getLineStack()
        val itemList = lineStack.itemList
        if (itemList != null) {
            for (item in itemList) {
                if (defaultItem?.id == item.id) {
                    return true
                }
            }
        }
        return false
    }

    fun addLineToDefaultItem(line: Line) {
        if (defaultItem != null) {
            val lineStack = getLineStack()
            val itemList = lineStack.itemList
            if (itemList != null) {
                for (item in itemList) {
                    if (item.id == defaultItem?.id) {
                        var lineList = item.lineList
                        if (lineList == null) {
                            lineList = ArrayList()
                        }
                        item.lineList = lineList
                        lineList.add(line)
                        break
                    }
                }
            }
            saveLineStack(lineStack)
            val targetItem = getItemTreeNodeByItemId(defaultItem!!.id)
            val lineNode = DefaultMutableTreeNode()
            lineNode.userObject = line
            targetItem?.insert(lineNode, 0)
            tree?.updateUI()
        }
    }

    fun deleteLine(lineId: String, lineNode: DefaultMutableTreeNode) {
        val lineStack = getLineStack()
        val itemList = lineStack.itemList
        if (itemList != null) {
            for (item in itemList) {
                val lineList = item.lineList
                if (lineList != null) {
                    val filteredLineList = lineList.filter { it.id != lineId }
                    item.lineList = filteredLineList.toMutableList() as ArrayList<Line>
                    break
                }
            }
        }
        saveLineStack(lineStack)

        val itemNode: DefaultMutableTreeNode = lineNode.parent as DefaultMutableTreeNode
        itemNode.remove(lineNode)
        tree?.updateUI()
    }
}
