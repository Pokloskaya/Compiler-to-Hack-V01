
/**
 * Parser for ST0244 language
 *
 * @author Helmuth Trefftz
 *
 * El lenguaje que se reconoce es el siguiente:
 *
 * <program> ::= program <funDefinitionList> endprogram
 * <funDefinitionList> ::= <funDefinition> <funDefinitionList>
 *              | epsilon
 * <funDefinition> ::= def variable lparen <varDefList> rparen
 *              <varDefList>
 *              <statementList>
 *              enddef
 * <varDefList> ::= <varDef> <varDefList>
 *              | epsilon
 * <varDef> ::= int variable
 * <statementList> ::= <statement> <statementList>
 *              | epsilon
 * <statment> ::= read variable 
 *              | print variable 
 *              | call variable lparen <variableList> rparen
 * <variableList> ::= <variable> <variableList>
 *              | epsilon
 */
import java.beans.Expression;
import java.io.FileNotFoundException;
import java.util.function.Function;

public class Parser {

    Token token;
    Lexer lexer;

    /**
     * Constructor The name of the file with the source code is received as
     * parameter
     *
     * @param fileName Name of file where the source code is be read
     */
    public Parser(String fileName) {
        try {
            lexer = new Lexer(fileName);
            token = lexer.nextToken();
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
            System.exit(1);
        }
    }

    /**
     * Check if the current token is the same as expected, as per the derivation
     *
     * @param expected Expected token
     */
    private void recognize(int expected) {
        if (token.code == expected) {
            token = lexer.nextToken();
        } else {
            System.out.print("Syntax Error. ");
            System.out.println("Expected: " + lexer.getTokenText(expected)
                    + " found: " + lexer.getTokenText(token.code));
            System.exit(2);
        }
    }

    private void assigment() {
        //recognizeVariable();
        recognize(Lexer.VARIABLE);
        recognize(Lexer.ASSIGN);
        expression();
    }

    private void expression() {
        term();
        expressionRest();
    }

    private void term() {
        factor();
        termRest();
    }

    private void factor() {
        if (token.code == Lexer.VARIABLE) {
            recognizeVariable();
        } else if (token.code == Lexer.CONSTANT) {
            recognizeConstant();
        } else if (token.code != Lexer.SUM && token.code != Lexer.MULT) {
            recognize(Lexer.LPAREN);
            expression();
            recognize(Lexer.RPAREN);
        }
    }

    private void termRest() {
        if (token.code == Lexer.MULT) {
            recognize(Lexer.MULT);
            factor();
            termRest();
        } else {

        }
    }

    private void expressionRest() {
        if (token.code == Lexer.SUM) {
            recognize(Lexer.SUM);
            term();
            expressionRest();
        } else {

        }
    }

    /**
     * Check if the current token is a variable Returns the name of the
     * variable. This will be needed when generating code.
     */
    private String recognizeVariable() {
        String text;
        if (token.code == Lexer.VARIABLE) {
            text = token.text;
            // Generate code for the variable
            token = lexer.nextToken();
        } else {
            text = null;
            System.out.println("ERROR EN RECONOCER VARIABLE");
            System.out.print("Syntax Error. ");
            System.out.println("Expected: variable found: "
                    + lexer.getTokenText(token.code));
            System.exit(2);
        }
        return text;
    }

    private String recognizeConstant() {
        String text;
        if (token.code == Lexer.CONSTANT) {
            text = token.text;
            // Generate code for the variable
            token = lexer.nextToken();
        } else {
            text = null;
            System.out.println("ERROR EN RECONOCER CONSTANTE");
            System.out.print("Syntax Error. ");
            System.out.println("Expected: constant found: "
                    + lexer.getTokenText(token.code));
            System.exit(2);
        }
        return text;
    }

    /**
     * <program> ::= program <funDefinition> endprogram
     */
    public void program() {
        recognize(Lexer.PROGRAM);
        funDefinitionList();
        recognize(Lexer.ENDPROGRAM);
        if (token.code == Lexer.EOF) {
            System.out.println("No errors found");
        }
    }

    /**
     * <funDefinitionList> ::= <funDefinition> <funDefinitionList>
     * | epsilon
     */
    public void funDefinitionList() {
        if (token.code == Lexer.DEF) {
            funDefinition();
            funDefinitionList();
        } else {
            // nothing, epsilon
        }
    }

    /**
     * <funDefinition> ::= def variable lparen <varDefList> rparen
     * <varDefinitionList>
     * <satamentList>
     * enddef
     *
     */
    public void funDefinition() {
        // Header
        recognize(Lexer.DEF);
        recognizeVariable();
        recognize(Lexer.LPAREN);
        varDefList();
        recognize(Lexer.RPAREN);
        // Variable definitions
        varDefList();
        // Statements
        statementList();
        recognize(Lexer.ENDDEF);
    }

    /**
     * <varDefList> ::= <varDef> <varDefList>
     * | epsilon
     *
     */
    public void varDefList() {
        if (token.code == Lexer.INT) {
            // all variable definitions start with "int"
            // Not that the token is not recoginzed here but in
            // varDef
            varDef();
            varDefList();
        }
    }

    /**
     * <varDef> ::= int variable
     */
    public void varDef() {
        recognize(Lexer.INT);
        // Use the name of the variable to generate code
        String text = recognizeVariable();
    }

    /**
     * <statementList> ::= <statement> <statementList>
     * | epsilon
     *
     */
    public void statementList() {
        
        if (token.code == Lexer.READ
                || token.code == Lexer.PRINT
                || token.code == Lexer.CALL
                || token.code == Lexer.VARIABLE) {
            statement();
            statementList();
        } else {
            // nothinge, epsilon
        }
    }

    /**
     *
     * <statment> ::= read variable
     * | print variable
     * | call variable lparen
     */
    public void statement() {
        System.out.print("Token code: "); 
        System.out.println(token.code);
        String text = null;
        switch (token.code) {
            case Lexer.READ:
                recognize(Lexer.READ);
                text = recognizeVariable();
                break;
            case Lexer.PRINT:
                recognize(Lexer.PRINT);
                text = recognizeVariable();
                break;
            case Lexer.CALL: //when calling a function
                recognize(Lexer.CALL);
                text = recognizeVariable();
                recognize(Lexer.LPAREN);
                variableList();
                recognize(Lexer.RPAREN);
                break;
            case Lexer.VARIABLE:
                // recognize(Lexer.VARIABLE);
                // text = recognizeVariable();
                assigment();
                break;
            default:
                break;
        }
    }

    /**
     * <variableList> ::= variable <variableList>
     * | epsilon
     */
    public void variableList() {
        String text = null;
        if (token.code == Lexer.VARIABLE) {
            text = recognizeVariable();
            variableList();
        } else {
            // nothing, epsilon
        }
    }
}
