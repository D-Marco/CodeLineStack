package com.lane.toolWindow

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionPopupMenu
import com.intellij.openapi.actionSystem.DefaultActionGroup
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
import com.lane.dataBeans.LineStack
import com.lane.services.MyProjectService
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
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
        myProjectService.initData(myToolWindow.getTree())


        //添加菜单栏
        val titleActionMenuActionGroup = ActionManager.getInstance().getAction("titleActionMenu") as ActionGroup
        toolWindow.setTitleActions(titleActionMenuActionGroup.getChildren(null).toMutableList())

        //可选菜单栏
        val additionalGearActionGroup = ActionManager.getInstance().getAction("additionalGearActions") as ActionGroup
        toolWindow.setAdditionalGearActions(additionalGearActionGroup)

        val additionalGearActionGroup1 = ActionManager.getInstance().getAction("additionalGearActions")


    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val scrollPane: JBScrollPane = JBScrollPane()
        private val tree: Tree

        init {
//            val panel = JPanel().apply {
//                layout = FlowLayout(FlowLayout.LEFT)
//            }
            val root = DefaultMutableTreeNode()
            tree = Tree(DefaultTreeModel(root))
            tree.setExpandableItemsEnabled(true)
            tree.setDragEnabled(true)
            tree.isRootVisible = false
            val myProjectService = toolWindow.project.service<MyProjectService>()
            tree.cellRenderer = CustomTreeCellRenderer(myProjectService)
            tree.addMouseListener(MyMouseAdapter(myProjectService))
            // tree add to panel
//            panel.add(tree)
            scrollPane.viewport.add(tree)
        }

        fun getContent(): JBScrollPane {
            return scrollPane
        }

        fun getTree(): Tree {
            return tree
        }

    }

    class MyMouseAdapter(private val service: MyProjectService) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            if (e!!.clickCount == 2) { // 检查是否双击
                val tree = e.component as Tree
                val reePath = tree.getPathForLocation(e.x, e.y)
                if (reePath != null) {
                    val node = reePath.lastPathComponent as DefaultMutableTreeNode
                    if (node.userObject is Line) {
                        service.openSelectedFile(node)
                    }
                }
            }
        }

        override fun mousePressed(e: MouseEvent?) {
            if (e != null && e.button == MouseEvent.BUTTON3) {
                val tree = e.component as Tree
                val selectedNode = tree.lastSelectedPathComponent as DefaultMutableTreeNode
                if (selectedNode.parent == null) {
                    return
                }
                val firstChildOfParent = selectedNode.parent.getChildAt(0)
                val lastChildOfParent = (selectedNode.parent as DefaultMutableTreeNode).lastChild

                val userObj = selectedNode.userObject
                when (userObj) {
                    is Item -> {
                        val group: DefaultActionGroup =
                            ActionManager.getInstance().getAction("ItemActionMenu") as DefaultActionGroup
                        group.remove(ActionManager.getInstance().getAction("action.MoveUpItemAction"))
                        group.remove(ActionManager.getInstance().getAction("action.MoveDownItemAction"))
                        if (firstChildOfParent != selectedNode) {
                            group.add(ActionManager.getInstance().getAction("action.MoveUpItemAction"))
                        }
                        if (lastChildOfParent != selectedNode) {
                            group.add(ActionManager.getInstance().getAction("action.MoveDownItemAction"))
                        }

                        val popupMenu: ActionPopupMenu =
                            ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group)
                        popupMenu.component.show(e.component, e.x, e.y)
                    }

                    is Line -> {
                        val group = ActionManager.getInstance().getAction("lineActionMenu") as DefaultActionGroup
                        group.remove(ActionManager.getInstance().getAction("action.MoveUpLineAction"))
                        group.remove(ActionManager.getInstance().getAction("action.MoveDownLineAction"))
                        if (firstChildOfParent != selectedNode) {
                            group.add(ActionManager.getInstance().getAction("action.MoveUpLineAction"))
                        }
                        if (lastChildOfParent != selectedNode) {
                            group.add(ActionManager.getInstance().getAction("action.MoveDownLineAction"))
                        }
                        val popupMenu: ActionPopupMenu =
                            ActionManager.getInstance().createActionPopupMenu(ActionPlaces.POPUP, group)
                        popupMenu.component.show(e.component, e.x, e.y)
                    }
                }
            }
        }
    }

    class CustomTreeCellRenderer(private val myProjectService: MyProjectService) : DefaultTreeCellRenderer() {

        override fun getTreeCellRendererComponent(
            tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean
        ): Component {
            val defaultItem = myProjectService.getDefaultItem()
            val nodeLabel: JLabel =
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
            if (value != null) {
                val treeNode = value as DefaultMutableTreeNode

                when (val nodeUserObj = treeNode.userObject) {
                    is Line -> {
                        val rootNode = (tree as Tree).model.root as DefaultMutableTreeNode
                        val rootUserObj = rootNode.userObject as LineStack
                        val parentNode = treeNode.parent
                        val currentIndexOfParent = parentNode.getIndex(treeNode)
                        val reverseIndex =
                            if (rootUserObj.showLineIndexNumber) ((parentNode.childCount - currentIndexOfParent).toString()+": ") else ""
                        val fileName = if (rootUserObj.showClassName) ("[" + nodeUserObj.fileName + "]") else ""
                        val text = nodeUserObj.text
                        nodeLabel.text = "<html><font color='#ffffff'>$reverseIndex$fileName</font> $text</html>"
                        nodeLabel.toolTipText = nodeUserObj.describe
                        nodeLabel.icon = IconLoader.getIcon("/META-INF/bookmark.svg", javaClass)

                    }

                    is Item -> {

                        nodeLabel.icon = IconLoader.getIcon("/META-INF/bookmarksList.svg", javaClass)
                        val name = nodeUserObj.name
                        if (defaultItem != null && defaultItem.id == nodeUserObj.id) {
                            nodeLabel.text = "<html><font color='#ffffff'>[Default]</font> $name</html>"
                        } else {
                            nodeLabel.text = name
                        }
                    }
                }
            }
            return nodeLabel
        }
    }

}
