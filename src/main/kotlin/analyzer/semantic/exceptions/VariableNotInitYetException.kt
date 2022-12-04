package analyzer.semantic.exceptions

import exceptions.SemanticException

class VariableNotInitYetException(val identifier: String): SemanticException()