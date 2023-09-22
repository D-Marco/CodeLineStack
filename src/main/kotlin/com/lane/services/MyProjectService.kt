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
import com.lane.dataBeans.LineWithItem
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import javax.xml.bind.JAXBContext

@Service(Service.Level.PROJECT)
class MyProjectService(val project: Project) {
    @Volatile
    private var defaultItem: Item? = null
    private var tree: Tree? = null
    private var treeModel: DefaultTreeModel? = null
    private var treeRoot: DefaultMutableTreeNode? = null

    private var fullFilePath = ""

    private var lineStack: LineStack? = null

    //以文件路径为key
    private var lineMap: ConcurrentHashMap<String, ArrayList<LineWithItem>> = ConcurrentHashMap()

    fun initData(tree: Tree) {
        this.tree = tree
        this.treeModel = tree.model as DefaultTreeModel
        this.treeRoot = treeModel!!.root as DefaultMutableTreeNode
        lineStack = getLineStack()
        initLineMap()
        renderTree()
    }

    fun getLastSelectedTreeNode(): DefaultMutableTreeNode? {
        if (tree?.lastSelectedPathComponent != null) {
            return tree?.lastSelectedPathComponent as DefaultMutableTreeNode
        }
        return null
    }

    init {
        fullFilePath = project.basePath + "/codeLineStack.xml"
        val storeFile = File(fullFilePath)

        if (!storeFile.exists()) {
            storeFile.createNewFile()
            storeFile.writeText(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                        "<lineStack defaultItemId=\"item2Id\" showClassName=\"true\" showLineIndexNumber=\"true\">\n" +
                        "</lineStack>", Charsets.UTF_8
            )
        }
        thisLogger().info(MyBundle.message("projectService", project.name))
    }

    private fun initLineMap() {
        lineMap = ConcurrentHashMap()
        val itemList = lineStack?.itemList
        if (itemList != null) {
            for (item in itemList) {
                val lineList = item.lineList
                if (lineList != null) {
                    for (line in lineList) {
                        val lineListFromMap = lineMap[line.fileRelativePath]
                        if (lineListFromMap == null) {
                            lineMap[line.fileRelativePath] = ArrayList()
                        }
                        lineMap[line.fileRelativePath]?.add(LineWithItem(line, item))

                    }
                }
            }
        }
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
                editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            }

        }
    }

    private fun getLineStack(): LineStack {
        val storeFile = File(fullFilePath)
        val context = JAXBContext.newInstance(LineStack::class.java)
        val unmarshaller = context.createUnmarshaller()
        return unmarshaller.unmarshal(FileReader(storeFile)) as LineStack
    }

    fun saveLineStack() {
        val context = JAXBContext.newInstance(LineStack::class.java)
        val marshaller = context.createMarshaller()
        marshaller.marshal(lineStack, FileOutputStream(fullFilePath))
    }

    private fun renderTree() {
        val itemList = lineStack?.itemList
        treeRoot?.removeAllChildren()
        treeRoot?.userObject = lineStack
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
        getLineStack()
        val newItem = Item()
        newItem.id = UUID.randomUUID().toString()
        newItem.name = itemName
        if (lineStack?.itemList == null) {
            lineStack?.itemList = ArrayList()
        }
        lineStack?.itemList?.add(newItem)
        saveLineStack()

        val newNode = DefaultMutableTreeNode()
        newNode.userObject = newItem
        treeRoot?.insert(newNode, 0)
        if (lineStack?.itemList?.size == 1) {
            treeModel?.nodeStructureChanged(treeRoot)
        }
        tree?.updateUI()
    }

    fun deleteItem(itemId: String) {
        val itemList = lineStack?.itemList
        val filteredItemList = itemList?.filter { it.id != itemId }
        lineStack?.itemList = filteredItemList?.toMutableList() as ArrayList<Item>
        saveLineStack()

        initLineMap()
        val targetItem = getItemTreeNodeByItemId(itemId)
        if (targetItem != null) {
            treeRoot?.remove(targetItem)
            tree?.updateUI()
        }
    }


    fun updateItemName(newName: String, selectedItemId: String) {
        val itemList = lineStack?.itemList
        itemList?.forEach { item ->
            run {
                if (item.id == selectedItemId) {
                    item.name = newName
                }
            }
        }
        saveLineStack()

        val targetItem = getItemTreeNodeByItemId(selectedItemId)
        if (targetItem != null) {
            val item = targetItem.userObject as Item
            item.name = newName
        }
    }

    fun makeItemAsDefault(item: Item) {
        this.defaultItem = item
        lineStack?.defaultItemId = item.id
        saveLineStack()

        tree?.updateUI()
    }

    fun getDefaultItem(): Item? {
        if (defaultItem == null) {
            val defaultItemId = lineStack?.defaultItemId
            val itemList = lineStack?.itemList
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
        val itemList = lineStack?.itemList
        if (itemList != null) {
            for (item in itemList) {
                if (defaultItem?.id == item.id) {
                    return true
                }
            }
        }
        return false
    }

    fun showLineIndexNumberValue(): Boolean {
        return lineStack!!.showLineIndexNumber
    }

    fun switchLineIndexNumberValue() {
        lineStack?.showLineIndexNumber = !(lineStack?.showLineIndexNumber)!!
        saveLineStack()
    }

    fun showClassNameValue(): Boolean {
        return lineStack!!.showClassName
    }

    fun expandRowAll() {
        val itemList = lineStack?.itemList
        val itemNodeList: Enumeration<TreeNode>? = treeRoot?.children()
        if (itemNodeList != null) {
            for (itemNode in itemNodeList) {
                tree?.expandPath(TreePath((itemNode as DefaultMutableTreeNode).path));
            }
        }
    }

    fun collapseRowAll() {
        val itemList = lineStack?.itemList
        val itemNodeList: Enumeration<TreeNode>? = treeRoot?.children()
        if (itemNodeList != null) {
            for (itemNode in itemNodeList) {
                tree?.collapsePath(TreePath((itemNode as DefaultMutableTreeNode).path));
            }
        }
    }

    fun switchShowClassNameValue() {
        lineStack?.showClassName = !(lineStack?.showClassName)!!
        saveLineStack()
    }

    fun addLineToDefaultItem(line: Line) {
        if (defaultItem != null) {
            val itemList = lineStack?.itemList
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
            saveLineStack()

            val lineListFrmMap = lineMap[line.fileRelativePath]
            if (lineListFrmMap == null) {
                lineMap[line.fileRelativePath] = ArrayList()
            }
            lineMap[line.fileRelativePath]?.add(LineWithItem(line, defaultItem!!))


            val targetItem = getItemTreeNodeByItemId(defaultItem!!.id)
            val lineNode = DefaultMutableTreeNode()
            lineNode.userObject = line
            targetItem?.insert(lineNode, 0)
            tree?.updateUI()
        }
    }


    fun deleteLine(lineId: String, lineNode: DefaultMutableTreeNode) {
        val itemList = lineStack?.itemList
        if (itemList != null) {
            for (item in itemList) {
                val lineList = item.lineList
                if (lineList != null) {
                    var targetLine: Line? = null
                    for (line in lineList) {
                        if (line.id == lineId) {
                            targetLine = line
                            break
                        }
                    }
                    if (targetLine != null) {
                        lineList.remove(targetLine)
                        lineMap[targetLine.fileRelativePath]?.remove(LineWithItem(targetLine,null))
                        break
                    }

                }
            }
        }
        saveLineStack()
        if (lineNode.parent != null) {
            val itemNode: DefaultMutableTreeNode = lineNode.parent as DefaultMutableTreeNode
            itemNode.remove(lineNode)
            tree?.updateUI()
        }

    }

    fun deleteLine(lineId: String) {
        val itemList = lineStack?.itemList
        var targetItem: Item? = null
        var targetLine: Line? = null
        if (itemList != null) {
            for (item in itemList) {
                val lineList = item.lineList
                if (lineList != null) {
                    for (line in lineList) {
                        if (line.id == lineId) {
                            targetLine = line
                            break
                        }
                    }
                    if (targetLine != null) {
                        targetItem = item
                        lineList.remove(targetLine)
                        break
                    }

                }
            }
        }
        saveLineStack()

        val itemListNode: Enumeration<TreeNode>? = treeRoot?.children()
        if (itemListNode != null) {
            for (itemNode in itemListNode) {
                val parseItemNode = itemNode as DefaultMutableTreeNode
                val itemData = parseItemNode.userObject as Item
                if (itemData.id == targetItem?.id) {
                    val lineListNode: Enumeration<TreeNode>? = itemNode.children()
                    if (lineListNode != null) {
                        for (lineNode in lineListNode) {
                            val parseLineNode = lineNode as DefaultMutableTreeNode
                            val lineData = parseLineNode.userObject as Line
                            if (lineData.id == targetLine?.id) {
                                itemNode.remove(lineNode)
                                tree?.updateUI()
                                return
                            }

                        }
                    }
                }
            }
        }
    }

    fun updateLineDesc(newDesc: String, selectedLineId: String) {
        val itemList = lineStack?.itemList
        if (itemList != null) {
            for (item in itemList) {
                val lineList = item.lineList
                if (lineList != null) {
                    for (line in lineList) {
                        if (line.id == selectedLineId) {
                            line.describe = newDesc
                            println("fff")
                            break
                        }
                    }
                }
            }
        }

        saveLineStack()
    }

    fun getLineListByFileName(filePath: String): ArrayList<LineWithItem>? {
        return lineMap[filePath]
    }

    fun updateTree() {
        tree?.updateUI()
    }

    fun upItemTreeNode(treeNode: DefaultMutableTreeNode) {
        val userObj = treeNode.userObject as Item
        val parentNode: DefaultMutableTreeNode = treeNode.parent as DefaultMutableTreeNode
        val siblingNodes = parentNode.children()
        var treeNodeIndex = 0
        for (siblingNode in siblingNodes) {
            if (siblingNode == treeNode) {
                break
            }
            treeNodeIndex += 1
        }
        parentNode.remove(treeNode)
        val upNode = parentNode.getChildAt(treeNodeIndex - 1) as DefaultMutableTreeNode
        parentNode.remove(upNode)
        parentNode.insert(treeNode, treeNodeIndex - 1)
        parentNode.insert(upNode, treeNodeIndex)

        val itemList = lineStack?.itemList
        var targetIndex = -1
        if (itemList != null) {
            for (item in itemList) {
                if (item == userObj) {
                    targetIndex = itemList.indexOf(item)
                    break
                }
            }
            if (targetIndex != -1) {
                val upItem = itemList[targetIndex + 1]
                itemList[targetIndex] = upItem
                itemList[targetIndex + 1] = userObj
                saveLineStack()
                tree?.updateUI()
            }
        }
    }

    fun downItemTreeNode(treeNode: DefaultMutableTreeNode) {
        val userObj = treeNode.userObject as Item
        val parentNode: DefaultMutableTreeNode = treeNode.parent as DefaultMutableTreeNode
        val siblingNodes = parentNode.children()
        var treeNodeIndex = 0
        for (siblingNode in siblingNodes) {
            if (siblingNode == treeNode) {
                break
            }
            treeNodeIndex += 1
        }
        val downNode = parentNode.getChildAt(treeNodeIndex + 1) as DefaultMutableTreeNode
        parentNode.remove(treeNode)
        parentNode.remove(downNode)
        parentNode.insert(downNode, treeNodeIndex)
        parentNode.insert(treeNode, treeNodeIndex + 1)

        val itemList = lineStack?.itemList
        var targetIndex = -1
        if (itemList != null) {
            for (item in itemList) {
                if (item == userObj) {
                    targetIndex = itemList.indexOf(item)
                    break
                }
            }
            if (targetIndex != -1) {
                val downItem = itemList[targetIndex - 1]
                itemList[targetIndex] = downItem
                itemList[targetIndex - 1] = userObj
                saveLineStack()
                tree?.updateUI()
            }

        }

    }

    fun upLineTreeNode(treeNode: DefaultMutableTreeNode) {
        val userObj = treeNode.userObject as Line
        val parentNode: DefaultMutableTreeNode = treeNode.parent as DefaultMutableTreeNode
        val siblingNodes = parentNode.children()
        var treeNodeIndex = 0
        for (siblingNode in siblingNodes) {
            if (siblingNode == treeNode) {
                break
            }
            treeNodeIndex += 1
        }
        parentNode.remove(treeNode)
        val upNode = parentNode.getChildAt(treeNodeIndex - 1) as DefaultMutableTreeNode
        parentNode.remove(upNode)
        parentNode.insert(treeNode, treeNodeIndex - 1)
        parentNode.insert(upNode, treeNodeIndex)

        val itemList = lineStack?.itemList
        var targetLineIndex = -1
        if (itemList != null) {
            for (item in itemList) {
                val lineList = item.lineList
                if (lineList != null) {
                    for (line in lineList) {
                        if (line == userObj) {
                            targetLineIndex = lineList.indexOf(line)
                            break
                        }
                    }
                    if (targetLineIndex != -1) {
                        val upLine = lineList[targetLineIndex + 1]
                        lineList[targetLineIndex] = upLine
                        lineList[targetLineIndex + 1] = userObj
                        saveLineStack()
                        tree?.updateUI()
                        return
                    }
                }
            }
        }
    }

    fun downLineTreeNode(treeNode: DefaultMutableTreeNode) {
        val userObj = treeNode.userObject as Line
        val parentNode: DefaultMutableTreeNode = treeNode.parent as DefaultMutableTreeNode
        val siblingNodes = parentNode.children()
        var treeNodeIndex = 0
        for (siblingNode in siblingNodes) {
            if (siblingNode == treeNode) {
                break
            }
            treeNodeIndex += 1
        }
        val downNode = parentNode.getChildAt(treeNodeIndex + 1) as DefaultMutableTreeNode
        parentNode.remove(treeNode)
        parentNode.remove(downNode)
        parentNode.insert(downNode, treeNodeIndex)
        parentNode.insert(treeNode, treeNodeIndex + 1)

        val itemList = lineStack?.itemList
        var targetLineIndex = -1
        if (itemList != null) {
            for (item in itemList) {
                val lineList = item.lineList
                if (lineList != null) {
                    for (line in lineList) {
                        if (line == userObj) {
                            targetLineIndex = lineList.indexOf(line)
                            break
                        }
                    }
                    if (targetLineIndex != -1) {
                        val upLine = lineList[targetLineIndex - 1]
                        lineList[targetLineIndex] = upLine
                        lineList[targetLineIndex - 1] = userObj
                        saveLineStack()
                        tree?.updateUI()
                        return
                    }
                }
            }
        }
    }

}
