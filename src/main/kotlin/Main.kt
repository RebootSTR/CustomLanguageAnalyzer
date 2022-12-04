import analyzer.SyntaxResult
import dictionary.Dictionary
import exceptions.LexicalException
import exceptions.SyntaxException
import analyzer.lexical.LexicalAnalyzer
import analyzer.lexical.LexicalFileProvider
import analyzer.lexical.exceptions.CharNotAllowedException
import analyzer.lexical.exceptions.LexicalInternalException
import analyzer.lexical.exceptions.SignFormatException
import analyzer.lexical.exceptions.UnknownSymbolException
import analyzer.semantic.SemanticAnalyzer
import analyzer.semantic.SemanticFileProvider
import analyzer.semantic.exceptions.MultiInitException
import analyzer.semantic.exceptions.VariableNotInitException
import analyzer.semantic.exceptions.VariableNotInitYetException
import analyzer.semantic.exceptions.VariableTypeCompatibilityException
import logger.Console
import logger.Logger
import analyzer.syntax.SyntaxAnalyzer
import analyzer.syntax.SyntaxFileProvider
import analyzer.syntax.exceptions.SyntaxInternalException
import exceptions.SemanticException

class Main {

    companion object {


        @JvmStatic
        fun main(args: Array<String>) {
            val infoLogger = Console(Logger.Level.INFO)
            val debugLogger = Console(Logger.Level.DEBUG)
            val fileProvider = FileProvider(infoLogger).apply {
                initFiles()
            }

            infoLogger.info(Dictionary.author())

            try {
                runLexicalAnalyze(infoLogger, fileProvider)
                infoLogger.info(Dictionary.divider())
                val syntaxResult = runSyntaxAnalyze(infoLogger, fileProvider)
                infoLogger.info(Dictionary.divider())
                runSemanticAnalyzer(infoLogger, syntaxResult, fileProvider)
            } catch (ex: LexicalException) {
                infoLogger.info(Dictionary.lexicalError())
            } catch (ex: SyntaxException) {
                infoLogger.info(Dictionary.syntaxError(ex.line))
            } catch (ex: SemanticException) {
                infoLogger.info(Dictionary.semanticError())
            }
        }

        private fun runSemanticAnalyzer(logger: Logger, syntaxResult: SyntaxResult, provider: SemanticFileProvider) {
            try {
                SemanticAnalyzer(logger, provider).run(syntaxResult)
            } catch (ex: MultiInitException) {
                logger.info(Dictionary.multiInitError(ex.variables))
                throw ex
            } catch (ex: VariableNotInitException) {
                logger.info(Dictionary.variableNotInitError(ex.identifier))
                throw ex
            } catch (ex: VariableTypeCompatibilityException) {
                logger.info(Dictionary.variableTypeCompatibilityError())
                throw ex
            } catch (ex: VariableNotInitYetException) {
                logger.info(Dictionary.variableNotInitYetError(ex.identifier))
                throw ex
            }
        }

        private fun runLexicalAnalyze(logger: Logger, provider: LexicalFileProvider) {
            try {
                LexicalAnalyzer(logger, provider).run()
            } catch (ex: LexicalInternalException) {
                logger.info(Dictionary.internalError(ex.message))
                throw ex
            } catch (ex: CharNotAllowedException) {
                logger.info(Dictionary.error(ex.message))
                throw ex
            } catch (ex: NumberFormatException) {
                logger.info(Dictionary.error(ex.message))
                throw ex
            } catch (ex: SignFormatException) {
                logger.info(Dictionary.error(ex.message))
                throw ex
            } catch (ex: UnknownSymbolException) {
                logger.info(Dictionary.error(ex.message))
                throw ex
            }
        }

        private fun runSyntaxAnalyze(logger: Logger, provider: SyntaxFileProvider): SyntaxResult {
            try {
                return SyntaxAnalyzer(logger, provider).run()
            } catch (ex: SyntaxInternalException) {
                logger.info(Dictionary.internalError(ex.message))
                throw ex
            }
        }
    }
}