package analyzer

class SyntaxResult(
    val newIdentifiers: List<Int>,
    val usedIdentifiers: List<Int>,
    val identifierTypes: Map<Int, String>,
    val setTypes: Map<Int, List<String>>,
)