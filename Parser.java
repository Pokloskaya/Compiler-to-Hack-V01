/**
 * Parser for ST0244 language
 *
 *
 * El lenguaje que se reconoce es el siguiente:
 *
 * <program> ::= program <staticVariables> <funDefinitionList> endprogram
 * <staticVariables> ::= <varDefList>
 * <funDefinitionList> ::= <funDefinition> <funDefinitionList>
 *      | epsilon
 * <funDefinition> ::= def variable lparen <varDefList> rparen
 *      <varDefList>
 *      <statementList>
 *      enddef
 * <varDefList> ::= <varDef> <varDefList>
 * | epsilon
 * <varDef> ::= int variable
 * <statementList> ::= <statement> <statementList>
 * | epsilon
 * <statment> ::= read variable 
 *      | print variable 
 *      | call variable lparen <argumentList> rparen
 *      | return <variable>
 *      | <assignment>
 *      | <while>
 *      | <print>
 * <assignment> ::= variable = <expr>
 * <expr> ::= <factor> <exprRest>
 * <exprRest> ::= + <factor> <exprRest>
 *      | - <factor> <exprRest>
 * <factor> ::= variable
 *      | constant
 *      | lparen <expr> rparen
 *      | callf variable lparen <argumentList> paren
 * <while> ::= while <condition> statementList endwhile
 * <print> ::= print lparen <expr> rparen
 * <condition> ::= lparen <expr> <compOp> <expr>  rparen
 * <compOp> ::= EQ | NEQ
 * <argumentList> ::= variable <argumentList>
 * <variableList> ::= variable <variableList>
 * | epsilon
 */
import java.io.FileNotFoundException;

public class Parser {

    Token token;
    Lexer lexer;
    CodeGenerator cg;
    SymbolTable staticVariables;
    SymbolTable argumentVariables;
    SymbolTable localVariables;
    String fileName;

    /**
     * Constructor The name of the file with the source code is received as
     * parameter
     *
     * @param fileName Name of file where the source code is be read
     */
    public Parser(String fileName) {
        try {
            this.fileName = fileName;
            lexer = new Lexer(fileName);
            token = lexer.nextToken();
            cg = new CodeGenerator(fileName);
            staticVariables = new SymbolTable();
        } catch (FileNotFoundException ex) {
            System.out.println("File not found " + fileName);
            System.exit(0);
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
            error("Expected: " + lexer.getTokenText(expected)
                    + " found: " + lexer.getTokenText(token.code));
            System.exit(0);
        }
    }

    /**
     * Check if the current token is a variable. Returns the name of the
     * variable. This will be needed when generating code.
     * 
     * @param text with the variable name
     */
    private String recognizeVariable() {
        String text;
        if (token.code == Lexer.VARIABLE) {
            text = token.text;
            // Generate code for the variable
            token = lexer.nextToken();
        } else {
            text = null;
            error("Expected: variable found: "
                    + lexer.getTokenText(token.code));
        }
        return text;
    }

    /**
     * Check if the current token is a constant. Returns the string with the
     * constant.
     *
     * @return text with the String containing the constant
     */
    private String recognizeConstant() {
        String text;
        if (token.code == Lexer.CONSTANT) {
            text = token.text;
            // Generate code for the variable
            token = lexer.nextToken();
        } else {
            text = null;
            error("Expected: constant, found: "
                    + lexer.getTokenText(token.code));
            System.exit(0);
        }
        return text;
    }

    /**
     * <program> ::= program <funDefinition> endprogram
     */
    public void program() {
        recognize(Lexer.PROGRAM);
        staticVariables();
        funDefinitionList();
        recognize(Lexer.ENDPROGRAM);
        if (token.code == Lexer.EOF) {
            System.out.println("No errors found");
            cg.writeFile();
        }
    }

    /**
     * Handles the definition of static variables
     * <staticVariables> ::= <varDefList>
     */
    public void staticVariables() {
        staticVariables = new SymbolTable();
        varDefList(staticVariables, 0);
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
        String text = recognizeVariable();
        recognize(Lexer.LPAREN);
        argumentVariables = new SymbolTable();
        int count = varDefList(argumentVariables, 0);
        recognize(Lexer.RPAREN);
        // if (text.equals("main")) {
        // cg.generateFunctionHeader(fileName + "." + text, 2);
        // } else {
        // cg.generateFunctionHeader(fileName + "." + text, count);
        // }
        // Variable definitions
        localVariables = new SymbolTable();
        count = varDefList(localVariables, 0);
        cg.generateFunctionHeader(fileName + "." + text, count);
        // Statements
        statementList();
        recognize(Lexer.ENDDEF);
        cg.generateMathOrLogic(CodeGenerator.RETURN);
        if (text.equals("main")) {
            cg.generateLabel("END_OF_PROGRAM");
            cg.generateGoto("END_OF_PROGRAM");
        }
    }

    /**
     * <varDefList> ::= <varDef> <varDefList>
     * | epsilon
     *
     * @param table table to insert the variable into
     * @param count number of defined variables
     */
    public int varDefList(SymbolTable table, int count) {
        if (token.code == Lexer.INT) {
            // all variable definitions start with "int"
            // Note that the token is not recoginzed here but in
            // varDef
            varDef(table);
            return varDefList(table, count + 1);
        } else {
            // nothing, epsilon
            return count;
        }
    }

    /**
     * <varDef> ::= int variable
     * 
     * @param table table to insert the variable into
     */
    public void varDef(SymbolTable table) {
        recognize(Lexer.INT);
        // Use the name of the variable to generate code
        String text = recognizeVariable();
        table.add(text);
    }

    /**
     * <statementList> ::= <statement> <statementList>
     * | epsilon
     *
     */
    public void statementList() {
        if (token.code == Lexer.CALL
                || token.code == Lexer.VARIABLE
                || token.code == Lexer.WHILE
                || token.code == Lexer.RETURN
                || token.code == Lexer.PRINT
                || token.code == Lexer.REPEAT
                || token.code == Lexer.IF) {
            statement();
            statementList();
        } else {
            // nothing, epsilon
        }
    }

    /**
     *
     * <statment> ::= read variable
     * | print variable
     * | call variable lparen
     * | return
     * | assignment
     * | while
     */
    public void statement() {
        String text = null;
        Pair pair = null;
        switch (token.code) {
            /*
             * case Lexer.READ:
             * recognize(Lexer.READ);
             * text = recognizeVariable();
             * break;
             * case Lexer.PRINT:
             * recognize(Lexer.PRINT);
             * text = recognizeVariable();
             * break;
             */
            case Lexer.CALL:
                recognize(Lexer.CALL);
                text = recognizeVariable();
                recognize(Lexer.LPAREN);
                int numArgs = exprList(0);
                recognize(Lexer.RPAREN);
                cg.generateCall(this.fileName + "." + text, numArgs);
                cg.generatePushPop(CodeGenerator.POP, CodeGenerator.TEMP, 0);
                break;
            case Lexer.RETURN:
                recognize(Lexer.RETURN);
                text = recognizeVariable();
                pair = findVariableType(text);
                if (pair == null) {
                    error("Undefined variable: " + text);
                }
                cg.generatePushPop(CodeGenerator.PUSH, pair.first, pair.second);
                cg.generateMathOrLogic(CodeGenerator.RETURN);
                break;
            case Lexer.VARIABLE:
                assignment();
                break;
            case Lexer.WHILE:
                recognize(Lexer.WHILE);
                String labelStart = cg.createLabel();
                String labelEnd = cg.createLabel();
                cg.generateLabel(labelStart);
                condition();
                cg.generateIfGoto(labelEnd);
                statementList();
                recognize(Lexer.ENDWHILE);
                cg.generateGoto(labelStart);
                cg.generateLabel(labelEnd);
                break;
            case Lexer.PRINT:
                recognize(Lexer.PRINT);
                recognize(Lexer.LPAREN);
                expr(); // expr leaves the evaluation in the stack
                cg.generateCall("Output.printInt", 1);
                cg.generatePushPop(CodeGenerator.POP, CodeGenerator.TEMP, 0);
                recognize(Lexer.RPAREN);
                break;
            case Lexer.REPEAT:
                recognize(Lexer.REPEAT);
                String labelRepeat = cg.createLabel();
                String labelEndRepeat = cg.createLabel();
                cg.generateLabel(labelRepeat);
                statementList();
                recognize(Lexer.UNTIL);
                condition();
                cg.generateMathOrLogic(CodeGenerator.NOT);
                cg.generateIfGoto(labelEndRepeat);
                cg.generateGoto(labelRepeat);
                cg.generateLabel(labelEndRepeat);
                break;
            case Lexer.IF:
                recognize(Lexer.IF);
                condition();
                String labelElse = cg.createLabel();
                String labelEndIF = cg.createLabel();
                cg.generateIfGoto(labelElse);
                statementList();
                cg.generateGoto(labelEndIF);
                recognize(Lexer.ELSE);
                cg.generateLabel(labelElse);
                statementList();
                recognize(Lexer.ENDIF);
                cg.generateLabel(labelEndIF);
                break;

            default:
                break;
        }
    }

    /**
     * Handles conditions that return boolean values
     */
    public void condition() {
        if (token.code == Lexer.LPAREN) {
            recognize(Lexer.LPAREN);
        } else {
            error("Expecting ( " + "found " + lexer.getTokenText(token.code));
        }
        expr();
        int code = compOp();
        expr();
        // Jums when the condition is NOT met
        if (code == Lexer.EQUALS) {
            cg.generateMathOrLogic(CodeGenerator.EQ);
            cg.generateMathOrLogic(CodeGenerator.NOT);

        } else if (code == Lexer.NEQ) {
            cg.generateMathOrLogic(CodeGenerator.EQ);

        } else if (code == Lexer.GT) {
            cg.generateMathOrLogic(CodeGenerator.GT);
            cg.generateMathOrLogic(CodeGenerator.NOT);

        } else if (code == Lexer.LT) {
            cg.generateMathOrLogic(CodeGenerator.LT);
            cg.generateMathOrLogic(CodeGenerator.NOT);

        } else if (code == Lexer.GE) {
            cg.generateMathOrLogic(CodeGenerator.LT);

        } else if (code == Lexer.LE) {
            cg.generateMathOrLogic(CodeGenerator.GT);
        }

        if (token.code == Lexer.RPAREN) {
            recognize(Lexer.RPAREN);
        } else {
            error("Expecting ) " + "found " + lexer.getTokenText(token.code));
        }
    }

    /**
     * Handles comparison operators
     * 
     * @return type of comparison operator
     */
    public int compOp() {
        switch (token.code) {
            case Lexer.EQUALS:
                recognize(Lexer.EQUALS);
                return Lexer.EQUALS;
            case Lexer.NEQ:
                recognize(Lexer.NEQ);
                return Lexer.NEQ;
            case Lexer.GT:
                recognize(Lexer.GT);
                return Lexer.GT;
            case Lexer.GE:
                recognize(Lexer.GE);
                return Lexer.GE;
            case Lexer.LT:
                recognize(Lexer.LT);
                return Lexer.LT;
            case Lexer.LE:
                recognize(Lexer.LE);
                return Lexer.LE;
    
            default:
                error("Expected Conditional Operator found  " + token.code);
        }
        return -1;
    }

    /**
     * Handles and assignment
     * <assignment> ::= variable = <expr>
     */
    private void assignment() {
        String text = recognizeVariable();
        Pair pair = findVariableType(text);
        if (pair == null) {
            error("Undefined variable: " + text);
        }
        recognize(Lexer.ASSIGN);
        expr(); // the reult of the expression evaluation is left in the stack
        cg.generatePushPop(CodeGenerator.POP, pair.first, pair.second);
    }

    /**
     * Recognizes an arithmetic expression
     * 
     */
    public void expr() {
        term();
        exprRest();
    }
    public void term(){
        factor();
        termRest();
    }
    /**
     * Handles the rest of an expression
     */
    public void exprRest() {
        if (token.code == Lexer.PLUS) {
            recognize(Lexer.PLUS);
            term();
            exprRest();
            cg.generateMathOrLogic(CodeGenerator.ADD);
        } else if (token.code == Lexer.MINUS) {
            recognize(Lexer.MINUS);
            term();
            exprRest();
            cg.generateMathOrLogic(CodeGenerator.SUB);
        } else {
            // nothing, epsilon
        }
    }
    public void termRest(){
    if (token.code == Lexer.MULT) {
        recognize(Lexer.MULT);
        factor();
        cg.generateCall(this.fileName + "." + "mult", 2);
        termRest();
        
    } else {
        // nothing, epsilon
    }
    }

    /**
     * recognizes a factor
     * The factor can be a variable, a constant, an expression in
     * parenthesis or a function call
     */
    public void factor() {
        String text;
        switch (token.code) {
            case Lexer.CONSTANT:
                text = recognizeConstant();
                int value = Integer.parseInt(text);
                cg.generatePushPop(CodeGenerator.PUSH, CodeGenerator.CONSTANT, value);
                break;
            case Lexer.VARIABLE:
                text = recognizeVariable();
                Pair pair = findVariableType(text);
                cg.generatePushPop(CodeGenerator.PUSH, pair.first, pair.second);
                break;
            case Lexer.CALLF:
                recognize(Lexer.CALLF);
                text = recognizeVariable();
                recognize(Lexer.LPAREN);
                int numArgs = exprList(0);
                recognize(Lexer.RPAREN);
                cg.generateCall(this.fileName + "." + text, numArgs);
                break;
            case Lexer.LPAREN:
                recognize(Lexer.LPAREN);
                expr();
                recognize(Lexer.RPAREN);
                break;
            default:
                error("Not a valid factor");
                break;
        }
    }

    /**
     * Finds which Symbol Table the variable should be added to
     * (static, argument, local) as well as the offset within the
     * table
     * 
     * @param varName variable name
     * @return pair consisting of the table and the offset
     */

    private Pair findVariableType(String varName) {
        int pos = staticVariables.find(varName);
        if (pos >= 0) {
            return new Pair(CodeGenerator.STATIC, pos);
        }
        pos = argumentVariables.find(varName);
        if (pos >= 0) {
            return new Pair(CodeGenerator.ARGUMENT, pos);
        }
        pos = localVariables.find(varName);
        if (pos >= 0) {
            return new Pair(CodeGenerator.LOCAL, pos);
        }
        return null;
    }

    /**
     * <variableList> ::= variable <variableList>
     * | epsilon
     */
    public void variableList() {
        if (token.code == Lexer.VARIABLE) {
            variableList();
        } else {
            // nothing, epsilon
        }
    }

    /**
     * Handles a list of expressions
     * 
     * @param count how many expressions have been parsed. This is a recursive call
     * @return
     */
    public int exprList(int count) {
        if (token.code == Lexer.CONSTANT
                || token.code == Lexer.VARIABLE
                || token.code == Lexer.LPAREN
                || token.code == Lexer.CALLF) {
            expr();
            return exprList(count + 1);
        } else {
            // nothing, epsilon
            return count;
        }
    }

    /**
     * Handles a list of arguments.
     * 
     * @param count How many arguments have been parsed so far. This is a recursive
     *              function.
     * @return Number of arguments
     */
    public int argumentListOld(int count) {
        if (token.code == Lexer.VARIABLE) {
            String text = recognizeVariable();
            Pair pair = findVariableType(text);
            if (pair == null) {
                error("Undefined variable: " + text);
            }
            cg.generatePushPop(CodeGenerator.PUSH, pair.first, pair.second);
            return argumentListOld(count + 1);
        } else {
            // nothing, epsilon
            return count;
        }
    }

    /**
     * Handles error messages
     * 
     * @param message
     */
    private void error(String message) {
        System.out.print("Line " + (lexer.lineNumber - 1) + ": ");
        System.out.println("Syntax Error");
        System.out.println(message);
        System.exit(0);
    }

}

/**
 * This class is used to return the variable type and the offset
 * within the segment
 * 
 */
class Pair {

    int first;
    int second;

    public Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }
}