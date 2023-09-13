package com.lane.toolWindow

import com.intellij.ide.projectView.impl.ProjectViewRenderer
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.lane.services.MyProjectService
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)

        val group = ActionManager.getInstance().getAction("AddLineActions") as ActionGroup
        toolWindow.setTitleActions(group.getChildren(null).toMutableList())
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val myProjectService = toolWindow.project.service<MyProjectService>()
        fun getContent(): JBScrollPane {
            val scrollPane = JBScrollPane()
            val panel = JPanel().apply {
                layout = FlowLayout(FlowLayout.LEFT)
            }

            val root = DefaultMutableTreeNode()
            for (i in 0..5) {
                root.add(buildChild("$i"))
            }
            //add to tree
            val tree = Tree(DefaultTreeModel(root))
            tree.setDragEnabled(true)
            tree.setExpandableItemsEnabled(true)
            tree.isRootVisible = false
            tree.cellRenderer = ProjectViewRenderer()

            //add double click listener to leaf
            tree.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e!!.clickCount == 2) { // 检查是否双击
                        val reePath = tree.getPathForLocation(e.x, e.y)
                        if (reePath != null) {
                            val node = reePath.lastPathComponent as DefaultMutableTreeNode
                            if (node.isLeaf) {
                                handleDoubleClick(node)
                            }
                        }

                    }
                }
            })

            //add toolbar
//            val toolBarDecorator = ToolbarDecorator.createDecorator(tree)
//            toolBarDecorator.setAddAction { addAction(root) }

            // tree add to panel
            panel.add(tree)
            scrollPane.viewport.add(panel)
            return scrollPane
        }

        private fun handleDoubleClick(defaultTreeModel: DefaultMutableTreeNode) {
            myProjectService.openSelectedFile(defaultTreeModel)
        }


        private fun buildChild(name: String): DefaultMutableTreeNode {
            val childLeaf = DefaultMutableTreeNode()
            childLeaf.userObject = "childLeaf$name"
            val child = DefaultMutableTreeNode();
            child.add(childLeaf)
            child.userObject = "child$name"
            return child
        }

        /**
         * reference from <https://stackoverflow.com/questions/9851688/how-to-align-left-or-right-inside-gridbaglayout-cell>
         */
        private fun createGbc(x: Int, y: Int): GridBagConstraints {
            val gbc = GridBagConstraints()
            gbc.gridx = x
            gbc.gridy = y
            gbc.gridwidth = 1
            gbc.gridheight = 1
            gbc.anchor = if (x == 0) GridBagConstraints.WEST else GridBagConstraints.EAST
            gbc.fill = if (x == 0) GridBagConstraints.BOTH else GridBagConstraints.HORIZONTAL
            gbc.insets = if (x == 0) JBUI.insets(5, 0, 5, 5) else JBUI.insets(5, 5, 5, 0)
            gbc.weightx = if (x == 0) 0.1 else 1.0
            gbc.weighty = 1.0
            return gbc
        }
    }
}
