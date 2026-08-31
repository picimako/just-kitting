//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.properties

import com.intellij.icons.AllIcons
import com.intellij.ide.setToolTipText
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.actionButton
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.dsl.listCellRenderer.listCellRenderer
import com.picimako.justkitting.ServiceLevelDecider.ServiceLevel
import com.picimako.justkitting.icons.JustKittingIcons
import com.picimako.justkitting.resources.JustKittingBundle.message
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JTextField

/**
 * ```
 * Scope -------------------------------------
 *   [App] [Proj] [Refresh] Project: [______v]
 *
 * Key - Value -------------------------------
 *   [_____v] [______________________________]
 *   [_______________________________________]
 *
 * Execution ---------------------------------
 *   [Run] () Query () Set () Delete
 *   |                                       |
 *   |                                       |
 * ```
 *
 * Most field values and selections are automatically saved into [JkPropertiesComponentState], so users can pick up
 * where they left off when starting a new IDE session.
 */
class PropertiesComponentPanel {

    companion object {
        fun create(project: Project): JComponent {
            lateinit var scope: Scope
            lateinit var resultField: Cell<JBTextArea>

            val dialogPanel: DialogPanel = panel {
                val settings = project.service<PropertiesComponentSettings>()
                group(message("pc.scope")) {
                    row {
                        scope = Scope(settings).apply {
                            val isProjectLevel = settings.level == ServiceLevel.PROJECT

                            appLevelButton = anActionButton(
                                message("service.level.display.name.app"),
                                message("pc.scope.app.level.description"),
                                getAppIcon()
                            ) { scope.enableApplication() }.gap(RightGap.SMALL)

                            projectLevelButton = anActionButton(
                                message("service.level.display.name.project"),
                                message("pc.scope.project.level.description"),
                                getProjectIcon()
                            ) { scope.enableProject() }

                            label(message("pc.scope.project.dropdown"))

                            reloadProjectsButton = anActionButton(
                                message("pc.scope.reload.projects.text"),
                                message("pc.scope.reload.projects.description"),
                                AllIcons.Actions.Refresh
                            ) { openProjects.component.initOrReload() }.align(AlignX.RIGHT).enabled(isProjectLevel)

                            openProjects = cell(OpenProjectsCombobox())
                                .align(AlignX.FILL).enabled(isProjectLevel)
                        }
                    }
                }

                val context = OperationPerformer.Context(settings, scope.selectedProject())
                group(message("pc.key.value")) {
                    row {
                        cell(ValueTypeCombobox(settings))
                            .onChanged { settings.valueType = it.selectedItem() }

                        context.keyField = textField()
                            .text(settings.key)
                            .apply { component.emptyText.text = message("pc.key.value.key.empty.text") }
                            .align(AlignX.FILL)
                            .onChanged { settings.key = it.text }
                            .installEnterListener(context)
                    }
                    row {
                        textArea()
                            .text(settings.value)
                            .apply { component.emptyText.text = message("pc.key.value.value.empty.text") }
                            .align(AlignX.FILL)
                            .comment(message("pc.key.value.value.list.comment"))
                            .onChanged { settings.value = it.text }
                    }
                }

                group(message("pc.execution")) {
                    buttonsGroup {
                        row {
                            anActionButton(
                                message("pc.execution.run.text"),
                                message("pc.execution.run.description"),
                                AllIcons.RunConfigurations.TestState.Run
                            ) { resultField.text(OperationPerformer.perform(context)) }

                            operationButton(
                                Operation.QUERY,
                                settings,
                                message("pc.execution.run.query.tooltip")
                            )
                            operationButton(
                                Operation.SET,
                                settings,
                                message("pc.execution.run.set.tooltip")
                            )
                            operationButton(Operation.DELETE, settings, message("pc.execution.run.delete.tooltip"))
                        }
                    }.bind(settings::operation)

                    row {
                        resultField = textArea()
                            .align(Align.FILL)
                            .comment(message("pc.execution.result.comment"))
                            .apply {
                                context.resultField = this
                                component.isEditable = false
                            }
                    }
                }
            }
            scope.parentPanel = dialogPanel
            return dialogPanel
        }
    }
}

private fun Row.operationButton(operation: Operation, settings: PropertiesComponentSettings, tooltip: String) {
    radioButton(operation.displayName, operation)
        .apply { component.setToolTipText(HtmlChunk.text(tooltip)) }
        .onChanged { if (it.isSelected) settings.operation = operation }
}

private fun Row.anActionButton(
    text: String,
    description: String,
    icon: Icon,
    action: () -> Unit
): Cell<ActionButton> =
    actionButton(object : AnAction(text, description, icon) {
        override fun actionPerformed(e: AnActionEvent) = action()
    })

/**
 * Installs a listener on `context.resultField`, so that upon hitting Enter performs the operation.
 */
private fun Cell<JTextField>.installEnterListener(context: OperationPerformer.Context): Cell<JTextField> {
    component.addKeyListener(object : KeyAdapter() {
        override fun keyTyped(e: KeyEvent?) {
            if ((e?.keyChar?.code) == KeyEvent.VK_ENTER) {
                context.resultField.text(OperationPerformer.perform(context))
            }
        }
    })

    return this
}

/**
 * Wrapper type for all components in the **Scope** section.
 */
private class Scope(private val settings: PropertiesComponentSettings) {
    lateinit var parentPanel: DialogPanel
    lateinit var projectLevelButton: Cell<ActionButton>
    lateinit var appLevelButton: Cell<ActionButton>
    lateinit var openProjects: Cell<OpenProjectsCombobox>
    lateinit var reloadProjectsButton: Cell<ActionButton>

    fun getProjectIcon(): Icon =
        if (settings.level == ServiceLevel.PROJECT) JustKittingIcons.ProjectSelected else AllIcons.Nodes.Folder

    fun getAppIcon(): Icon =
        if (settings.level == ServiceLevel.APP) JustKittingIcons.AppSelected else AllIcons.RunConfigurations.Application

    fun selectedProject(): Project? = ProjectManager.getInstance()
        .openProjects.firstOrNull { it.name == openProjects.component.selectedItem as String }

    fun enableProject() {
        appLevelButton.component.icon = AllIcons.RunConfigurations.Application
        projectLevelButton.component.icon = JustKittingIcons.ProjectSelected

        repaintParentPanel()
        setProjectsState(isEnabled = true)

        settings.level = ServiceLevel.PROJECT
    }

    fun enableApplication() {
        appLevelButton.component.icon = JustKittingIcons.AppSelected
        projectLevelButton.component.icon = AllIcons.Nodes.Folder

        repaintParentPanel()
        setProjectsState(isEnabled = false)

        settings.level = ServiceLevel.APP
    }

    private fun repaintParentPanel() {
        parentPanel.revalidate()
        parentPanel.repaint()
    }

    /**
     * Necessary, otherwise the application/project icons stays the previous icon, until another component is however
     * overed or is clicked.
     */
    private fun setProjectsState(isEnabled: Boolean) {
        openProjects.enabled(isEnabled)
        reloadProjectsButton.enabled(isEnabled)
    }
}

/**
 * Displays all currently open projects.
 *
 * Its contents can be reloaded by clicking on the `Reload Projects` button.
 */
private class OpenProjectsCombobox : ComboBox<String>() {

    init {
        initOrReload()
        renderer = listCellRenderer<String> { text(value) }
        isSwingPopup = false //Enables JBPopup implementation and speed search
    }

    fun initOrReload() {
        val openProjectNames = ProjectManager.getInstance().openProjects.map { it.name }
        model = CollectionComboBoxModel(openProjectNames)
    }
}

/**
 * Displays all `PropertiesComponent` [ValueType]s.
 */
private class ValueTypeCombobox(settings: PropertiesComponentSettings) : ComboBox<ValueType>() {

    init {
        model = CollectionComboBoxModel(ValueType.entries)
        selectedItem = settings.valueType
        renderer = listCellRenderer<ValueType> { text(value.displayName) }
        isSwingPopup = false //Enables JBPopup implementation and speed search
    }

    fun selectedItem(): ValueType = selectedItem as ValueType
}

/**
 * Type of the value to perform the operation as. Essentially, `PropertiesComponent`
 * stores values either as `String`s or `List<String>`, and those are stored separately.
 */
enum class ValueType(val displayName: String) {
    STRING(message("pc.key.value.type.string")),
    STRING_LIST(message("pc.key.value.type.string.list"))
}

/**
 * Performs the actual invocation of `PropertiesComponent` with additional precondition checks.
 */
private object OperationPerformer {
    //The operation should be fast, but just in case someone would really want to spam execution
    private var isOngoing = false

    fun perform(context: Context): String {
        if (isOngoing) return message("pc.execution.operation.ongoing")
        if (context.project == null) return message("pc.execution.operation.no.target.project")
        if (!context.project.isOpen) return message("pc.execution.operation.project.not.open")
        if (context.keyField.component.text.isBlank()) return message("pc.execution.operation.no.key")

        isOngoing = true
        val result = when (context.settings.level) {
            ServiceLevel.PROJECT -> ProjectPropertiesComponent(context.project).call()
            ServiceLevel.APP -> ApplicationPropertiesComponent(context.project).call()
            //This branch should never happen
            else -> ""
        }
        isOngoing = false
        return result
    }

    class Context(val settings: PropertiesComponentSettings, val project: Project?) {
        lateinit var keyField: Cell<JTextField>
        lateinit var resultField: Cell<JBTextArea>
    }
}

/**
 * The type of operations that can be performed on `PropertiesComponent`.
 */
enum class Operation(val displayName: String) {
    QUERY(message("pc.execution.operation.query")),
    SET(message("pc.execution.operation.set")),
    DELETE(message("pc.execution.operation.delete"))
}