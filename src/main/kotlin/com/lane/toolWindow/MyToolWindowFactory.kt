package com.lane.toolWindow

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import com.lane.dataBeans.Item
import com.lane.dataBeans.Line
import com.lane.services.MyProjectService
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        //创建视图
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)

        //初始化服务
        val myProjectService = toolWindow.project.service<MyProjectService>()
        myProjectService.setTree(myToolWindow.getTree())

        //添加菜单栏
        val group = ActionManager.getInstance().getAction("MenuActions") as ActionGroup
        toolWindow.setTitleActions(group.getChildren(null).toMutableList())

        val fullFilePath = project.basePath + "/codeLineStack.xml"
        val storeFile = File(fullFilePath)
        if (!storeFile.exists()) {
            storeFile.createNewFile()
            storeFile.writeText(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                        "<lineStack >\n" +
                        "</lineStack>", Charsets.UTF_8
            )
        } else {
            myProjectService.renderTree()
        }
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val scrollPane: JBScrollPane = JBScrollPane()
        private val tree: Tree

        init {
            val panel = JPanel().apply {
                layout = FlowLayout(FlowLayout.LEFT)
            }
            val root = DefaultMutableTreeNode()
            tree = Tree(DefaultTreeModel(root))
            tree.setExpandableItemsEnabled(true)
            tree.setDragEnabled(true)
            tree.isRootVisible = false
            tree.cellRenderer = CustomTreeCellRenderer()
            val myProjectService = toolWindow.project.service<MyProjectService>()
            tree.addMouseListener(MyMouseAdapter(tree, myProjectService))

            // tree add to panel
            panel.add(tree)
            scrollPane.viewport.add(panel)
        }

        fun getContent(): JBScrollPane {
            return scrollPane
        }

        fun getTree(): Tree {
            return tree
        }

    }

    class MyMouseAdapter(private val tree: Tree, private val service: MyProjectService) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            if (e!!.clickCount == 2) { // 检查是否双击
                val reePath = tree.getPathForLocation(e.x, e.y)
                if (reePath != null) {
                    val node = reePath.lastPathComponent as DefaultMutableTreeNode
                    if (node.isLeaf) {
                        service.openSelectedFile(node)
                    }
                }
            }
        }
    }

    class CustomTreeCellRenderer : DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(
            tree: JTree?,
            value: Any?,
            sel: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): Component {
            val nodeLabel: JLabel =
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
            if (value != null) {
                val treeNode = value as DefaultMutableTreeNode
                val nodeUserObj = treeNode.userObject
                when (nodeUserObj) {
                    is Line -> {
                        // 加载并设置您的 SVG 图标
                        nodeLabel.icon = IconLoader.getIcon("/META-INF/bookmark.svg", javaClass)
                    }

                    is Item -> {
                        nodeLabel.icon = IconLoader.getIcon("/META-INF/bookmarksList.svg", javaClass)
                    }
                }
            }

            return nodeLabel
        }
    }

}
