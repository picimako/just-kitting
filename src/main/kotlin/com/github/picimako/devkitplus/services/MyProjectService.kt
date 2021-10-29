package com.github.picimako.devkitplus.services

import com.intellij.openapi.project.Project
import com.github.picimako.devkitplus.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
