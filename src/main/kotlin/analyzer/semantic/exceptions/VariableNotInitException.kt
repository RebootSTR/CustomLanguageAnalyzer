package analyzer.semantic.exceptions

import exceptions.SemanticException

class VariableNotInitException(val identifier: String): SemanticException()