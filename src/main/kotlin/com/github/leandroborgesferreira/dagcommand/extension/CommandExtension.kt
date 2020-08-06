package com.github.leandroborgesferreira.dagcommand.extension

open class CommandExtension(
    var filter: String = "No name",
    var defaultBranch: String = "master",
    var outputType: String = "console",
    var printAdjacencyList: Boolean = true,
    var printDataFrame: Boolean = true
)
