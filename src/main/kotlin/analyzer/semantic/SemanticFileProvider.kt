package analyzer.semantic

interface SemanticFileProvider {
    fun getIdentifier(position: Int): String
}