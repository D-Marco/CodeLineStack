package com.lane.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.Topic
import com.lane.dataBeans.LineWithItem
import com.lane.services.MyProjectService
import com.lane.util.MyGutterIconRenderer
import com.lane.util.UtilData


class MyFileEditorManagerListener : FileEditorManagerListener {

    companion object {
        fun updateStackStateInEditorFile(
            myProjectService: MyProjectService,
            fileRelationPath: String,
            textEditor: Editor?
        ) {
            if (textEditor == null) {
                return
            }

            var markupModel: MarkupModel = textEditor.markupModel
            markupModel.removeAllHighlighters()

            val allRelLineWithItemList = myProjectService.getLineWithItemListByFileName(fileRelationPath) ?: return
            for (it in allRelLineWithItemList) {
                markupModel = textEditor.markupModel
                val allHighLighters = markupModel.allHighlighters
                var breakOut = false
                val line = it.line
                for (itLighter in allHighLighters) {
                    val value = itLighter.getUserData(UtilData.HigherKey)
                    if (value != null && value == line.selectionLine) {
                        breakOut = true
                        break
                    }
                }
                if (breakOut) {
                    continue
                }
                val highlighter = markupModel.addLineHighlighter(line.selectionLine, HighlighterLayer.SYNTAX, null)
                highlighter.putUserData(UtilData.HigherKey, line.selectionLine)
                highlighter.gutterIconRenderer =
                    MyGutterIconRenderer(myProjectService, line.fileRelativePath, line.selectionLine)
            }
        }

        fun removeHighlighter(lineNumber: Int, textEditor: Editor?): Boolean {
            if (textEditor == null) {
                return false
            }
            val markupModel: MarkupModel = textEditor.markupModel
            val allHighLighters = markupModel.allHighlighters
            for (itLighter in allHighLighters) {
                val value = itLighter.getUserData(UtilData.HigherKey)
                if (value != null && value == lineNumber) {
                    markupModel.removeHighlighter(itLighter)
                    return true
                }

            }
            return false

        }

    }

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        val myProjectService = source.project.service<MyProjectService>()
        ProjectManager.getInstance().defaultProject
        val editors = source.getEditors(file)
        if (editors.isEmpty()) {
            return
        }
        var editor: Editor? = null

        for (editorItem in editors) {
            if (editorItem is TextEditor) {
                editor = editorItem.editor
                break
                // 在这里使用textEditor对象，它是Editor类型的对象
            }
        }

        if (editor == null) {
            return
        }
        val fileEditor = source.getSelectedEditor(file)
        val basePath = myProjectService.project.basePath
        val fileRelationPath = file.path.substring(basePath!!.length + 1)
        val document: Document? = FileDocumentManager.getInstance().getDocument(file)
        if (fileEditor == null || document == null) {
            return
        }
        updateStackStateInEditorFile(myProjectService, fileRelationPath, editor)

        document.addDocumentListener(
            MyDocumentListener(myProjectService, document, fileRelationPath, editor),
            fileEditor
        )
    }

    class MyDocumentListener(
        var myProjectService: MyProjectService, private var document: Document,
        private var fileRelationPath: String,
        private var editor: Editor
    ) : DocumentListener {
        //            var beforeCaretNumber = -1
        private var lastLineCount = 0
        private var lastDocumentText = ""

        override fun beforeDocumentChange(event: DocumentEvent) {
            lastDocumentText = document.text
            lastLineCount = document.lineCount
        }

        override fun documentChanged(event: DocumentEvent) {
            val newDocumentText = document.text
            val lineWithItemList: ArrayList<LineWithItem>? =
                myProjectService.getLineWithItemListByFileName(fileRelationPath)
            var hasChange = false
            if (lineWithItemList != null) {
                val insertedLines = document.lineCount - lastLineCount
                val showBeRemoveLineWithItemList: ArrayList<LineWithItem> = ArrayList()
                for (lineWithItem in lineWithItemList) {
                    val targetLineNumber = lineWithItem.line.selectionLine
                    if (insertedLines != 0) {
                        val hasChangeBefore =
                            textHasChangeBeforeLine(targetLineNumber, lastDocumentText, newDocumentText)
                        val hasChangeAfter = textHasChangeAfterLine(targetLineNumber, lastDocumentText, newDocumentText)
                        if (hasChangeBefore) {
                            hasChange = if (!hasChangeAfter) {
                                if (getTextByLineNumber(
                                        targetLineNumber + insertedLines,
                                        newDocumentText
                                    ) == lineWithItem.line.text
                                ) {
                                    addOrSubLine(lineWithItem, insertedLines)
                                    true
                                } else {
                                    removeLine(lineWithItem.line.id)
                                    showBeRemoveLineWithItemList.add(lineWithItem)
                                    true
                                }
                            } else {
                                removeLine(lineWithItem.line.id)
                                showBeRemoveLineWithItemList.add(lineWithItem)
                                true
                            }
                        } else {
                            if (hasChangeAfter) {
                                if (getTextByLineNumber(targetLineNumber, newDocumentText) != lineWithItem.line.text) {
                                    removeLine(lineWithItem.line.id)
                                    showBeRemoveLineWithItemList.add(lineWithItem)
                                    hasChange = true
                                }
                            } else {
                                if (getTextByLineNumber(targetLineNumber, newDocumentText) != lineWithItem.line.text) {
                                    removeLine(lineWithItem.line.id)
                                    showBeRemoveLineWithItemList.add(lineWithItem)
                                    hasChange = true
                                }

                            }
//                                if (getTextByLineNumber(targetLineNumber + insertedLines, newDocumentText) != lineWithItem.line.text) {
//                                    removeLine(lineWithItem.line.id)
//                                    showBeRemoveLineWithItemList.add(lineWithItem)
//                                }

                        }
                    } else {
                        if (getTextByLineNumber(targetLineNumber, newDocumentText).replace(Regex("[\\s\\t]+"), "") != lineWithItem.line.text.replace(Regex("[\\s\\t]+"), "")
                        ) {
                            removeLine(lineWithItem.line.id)
                            showBeRemoveLineWithItemList.add(lineWithItem)
                            hasChange = true
                        }
                    }

                }
                if (showBeRemoveLineWithItemList.size > 0) {
                    lineWithItemList.removeAll(showBeRemoveLineWithItemList.toSet())
                    myProjectService.saveLineStack()
                    hasChange = true
                }
                if (hasChange) {
                    updateStackStateInEditorFile(myProjectService, fileRelationPath, editor)
                }

            }

        }

        private fun textHasChangeAfterLine(targetLine: Int, oldText: String, newText: String): Boolean {
// 将文本按行拆分成数组
            val oldLines = oldText.split("\n")
            val newLines = newText.split("\n")

            val oldLinesSize = oldLines.size
            val newLineSize = newLines.size

            // 检查行数是否合法
            if (targetLine < 0 || targetLine >= oldLinesSize) {
                throw IllegalArgumentException("目标行数无效")
            }
            val totalLine = oldLinesSize - targetLine - 1
            var i = 1
            while (i < totalLine + 1) {
                if (oldLines[oldLinesSize - i] != newLines[newLineSize - i]) {
                    return true
                }
                i++
            }
            return false // 所有行都相同，返回 false
        }

        private fun textHasChangeBeforeLine(targetLine: Int, oldText: String, newText: String): Boolean {
// 将文本按行拆分成数组
            val oldLines = oldText.split("\n")
            val newLines = newText.split("\n")
            val newLinesSize = newLines.size
            // 检查行数是否合法
            if (targetLine < 0 || targetLine >= oldLines.size) {
                throw IllegalArgumentException("目标行数无效")
            }
            if (targetLine == newLinesSize) {
                return true
            }
            var i = 0
            while (i < targetLine) {
                if (i >= newLinesSize) {
                    return true
                }
                if (oldLines[i] != newLines[i]) {
                    return true
                }
                i++
            }
            return false // 所有行都相同，返回 false
        }

        private fun getTextByLineNumber(targetLine: Int, text: String): String {
            val textLines = text.split("\n")
            return textLines[targetLine]
        }

        private fun removeLine(lineId: String) {
            myProjectService.deleteLine(lineId)
        }

        private fun addOrSubLine(lineWithItem: LineWithItem, insertedLines: Int) {
            lineWithItem.line.selectionLine += insertedLines
            myProjectService.updateTree()
        }

    }


}