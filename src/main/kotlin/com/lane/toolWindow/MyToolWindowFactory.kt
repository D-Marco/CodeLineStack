package com.lane.toolWindow

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.AnActionButton
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import com.lane.services.MyProjectService
import org.apache.commons.lang3.StringUtils
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JList
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
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {
        private val service = toolWindow.project.service<MyProjectService>()
        private val defaultListModel: DefaultListModel<Int>? = null
        fun getContent(): JBPanel<JBPanel<*>> {
            val defaultListModel = DefaultListModel<Any>()
            for (i in 0..9) {
                defaultListModel.addElement(i)
            }
            val list = JBList<Any?>(defaultListModel)


            // 修饰每一行的元素
            val coloredListCellRenderer: ColoredListCellRenderer<Any?> = object : ColoredListCellRenderer<Any?>() {
                override fun customizeCellRenderer(
                    list: JList<out Any?>,
                    value: Any?,
                    index: Int,
                    selected: Boolean,
                    hasFocus: Boolean
                ) {
                    append("$value-suffix")
                }
            }
            list.setCellRenderer(coloredListCellRenderer)

            // 触发快速查找

            // 触发快速查找
            ListSpeedSearch<Any>(list)

            // 增加工具栏（新增按钮、删除按钮、上移按钮、下移按钮）

            // 增加工具栏（新增按钮、删除按钮、上移按钮、下移按钮）
            val decorator: ToolbarDecorator = ToolbarDecorator.createDecorator(list)
            // 新增元素动作
            // 新增元素动作
            decorator.setAddAction { actionButton -> addAction() }
            // 自定义按钮
            // 自定义按钮
            decorator.addExtraAction(ExtraButtonAction(defaultListModel, list))

            val panel = JBPanel<JBPanel<*>>()
            panel.setLayout(BorderLayout())
            panel.add(decorator.createPanel(), BorderLayout.CENTER)
            return panel

        }

        private fun addAction() {
            val newItem = Messages.showInputDialog("Input A Item", "Add", Messages.getInformationIcon())
            if (StringUtils.isNotBlank(newItem)) {
                defaultListModel?.addElement(newItem!!.toInt())
            }
        }

        private fun buildTree(treeName: String): JBPanel<JBPanel<*>> {
            val child1Leaf1 = DefaultMutableTreeNode()
            child1Leaf1.userObject = "child1Leaf1$treeName"
            val child1 = DefaultMutableTreeNode();
            child1.add(child1Leaf1)
            child1.userObject = "child1$treeName"


            val child1Leaf2 = DefaultMutableTreeNode()
            child1Leaf2.userObject = "child1Leaf2$treeName"
            val child2 = DefaultMutableTreeNode();
            child2.add(child1Leaf2)
            child2.userObject = "child2$treeName"


            val root = DefaultMutableTreeNode()
            root.userObject = "root$treeName"
            root.add(child1)
            root.add(child2)

            val model = DefaultTreeModel(root)
            val tree = Tree(model)
            tree.setDragEnabled(true)
            tree.setExpandableItemsEnabled(true)

            val label = JBPanel<JBPanel<*>>()
            label.add(tree)
            return label
        }
    }

    class ExtraButtonAction(private val model: DefaultListModel<Any>, private val list: JBList<*>) :
        AnActionButton("Extra") {
        override fun actionPerformed(e: AnActionEvent) {
            val index = list.selectedIndex
            val newValue: @NlsSafe String? =
                Messages.showInputDialog(model[index].toString() + "", "Edit", Messages.getInformationIcon())
            if (StringUtils.isNotBlank(newValue)) {
                model.add(index, newValue?.toInt() ?: 3)
            }
        }
    }

}
