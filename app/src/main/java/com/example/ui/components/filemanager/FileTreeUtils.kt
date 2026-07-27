package com.example.ui.components.filemanager

import com.example.data.db.ProjectFileEntity

data class DisplayFileItem(
    val entity: ProjectFileEntity,
    val depth: Int
)

fun buildFlatTree(
    allFiles: List<ProjectFileEntity>,
    expandedFolders: Set<String>,
    parentPath: String = "/",
    depth: Int = 0
): List<DisplayFileItem> {
    val result = mutableListOf<DisplayFileItem>()
    val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath

    val children = allFiles.filter { file ->
        val p = if (file.parentPath.endsWith("/") && file.parentPath != "/") file.parentPath.dropLast(1) else file.parentPath
        p == cleanParent
    }.sortedWith(compareByDescending<ProjectFileEntity> { it.isDirectory }.thenBy { it.name.lowercase() })

    for (child in children) {
        result.add(DisplayFileItem(child, depth))
        if (child.isDirectory && expandedFolders.contains(child.path)) {
            result.addAll(buildFlatTree(allFiles, expandedFolders, child.path, depth + 1))
        }
    }

    // Safety fallback: if there are files whose parentPath doesn't match any directory, show them at depth 0
    if (depth == 0) {
        val listedPaths = result.map { it.entity.path }.toSet()
        val orphans = allFiles.filter { !listedPaths.contains(it.path) }
        for (orphan in orphans) {
            result.add(DisplayFileItem(orphan, 0))
        }
    }

    return result
}
