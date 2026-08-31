//Copyright 2026 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting.icons

import com.intellij.ui.IconManager
import javax.swing.Icon

object JustKittingIcons {
  private fun load(path: String): Icon {
    return IconManager.getInstance().getIcon(path, JustKittingIcons::class.java.classLoader)
  }

  val AppSelected: Icon = load("icons/app_check.svg")
  val ProjectSelected: Icon = load("icons/project_check.svg")
}