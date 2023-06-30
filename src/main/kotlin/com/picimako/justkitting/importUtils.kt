//Copyright 2021 Tamás Balog. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.picimako.justkitting

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.ImportPath

/**
 * Imports the provided fully qualified name, if it is not already imported in the given Kotlin file.
 *
 * NOTE: this function doesn't take into account existing star imports.
 *
 * @param file the Kotlin file in which the import would happen
 * @param fqNameToImport the FQN of the class, function, etc. to import
 * @param factory the Kotlin PSI factory to use to create the import directive
 */
fun importIfNotAlreadyAdded(file: KtFile, fqNameToImport: String, factory: KtPsiFactory = KtPsiFactory(file.project, false)) {
    val fqName = FqName(fqNameToImport)
    val isAlreadyImported = file.importDirectives.mapNotNull { it.importPath }.any { it.fqName == fqName }
    if (!isAlreadyImported) {
        file.importList?.add(factory.createImportDirective(ImportPath.fromString(fqNameToImport)))
    }
}
