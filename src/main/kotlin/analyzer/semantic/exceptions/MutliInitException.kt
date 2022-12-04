package analyzer.semantic.exceptions

import exceptions.SemanticException

class MultiInitException(val variables: String): SemanticException()