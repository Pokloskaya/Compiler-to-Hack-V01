
/**
 * Very simple Lexer, built on the Java Scanner.
 * Limitation: all the symbols have to be separted with spaces 
 * in the source file.
 */
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * This lexer returns tokens to the Parser
 */
public class Lexer {
    //Type of digits and letters
    public static final int[] DIGIT = {0,1,2,3,4,5,6,7,8,9};
    public static final char[] LETER = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    
    // Types of tokens
    public static final int PROGRAM = 1;
    public static final int ENDPROGRAM = 2;
    public static final int DEF = 3;
    public static final int ENDDEF = 4;
    public static final int IF = 5;
    public static final int ELSE = 6;
    public static final int ENDIF = 7;
    public static final int VARIABLE = 8;
    public static final int CONSTANT = 9;
    public static final int EQUALS = 10;
    public static final int ASSIGN = 11;
    public static final int LPAREN = 12;
    public static final int RPAREN = 13;
    public static final int INT = 14;
    public static final int READ = 15;
    public static final int PRINT = 16;
    public static final int CALL = 17;
    public static final int SUM = 18;
    public static final int MULT = 19;
    public static final int DIFFER = 20;
    public static final int ENDELSE = 21;
    public static final int WHILE = 22;
    public static final int ENDWHILE = 23;
    public static final int CALLF = 24;
    public static final int REPEAT = 25;
    public static final int UNTIL = 26;
    public static final int INVALIDTOKEN = 98;
    public static final int EOF = 99;
    
    ArrayList<Token> table;
    
    Scanner fileScanner = null;
    Scanner lineScanner = null;
    
    /**
     * Constructor
     * @param fileName Name of the input file
     * @throws FileNotFoundException 
     */
    public Lexer(String fileName) throws FileNotFoundException {
        fileScanner = new Scanner(new File(fileName));
        table = new ArrayList<>();
        table.add(new Token(PROGRAM, "program"));
        table.add(new Token(ENDPROGRAM, "endprogram"));
        table.add(new Token(DEF, "def"));
        table.add(new Token(ENDDEF, "enddef"));
        table.add(new Token(IF, "if")); 
        table.add(new Token(ELSE, "else"));
        table.add(new Token(WHILE, "while"));
        table.add(new Token(ENDWHILE, "endwhile"));
        table.add(new Token(ENDELSE, "endelse"));
        table.add(new Token(ENDIF, "endif"));
        table.add(new Token(ASSIGN, "="));
        table.add(new Token(EQUALS, "=="));
        table.add(new Token(DIFFER, "!="));
        table.add(new Token(LPAREN, "("));
        table.add(new Token(RPAREN, ")"));
        table.add(new Token(INT, "int"));
        table.add(new Token(READ, "read"));
        table.add(new Token(PRINT, "print"));
        table.add(new Token(SUM, "+"));
        table.add(new Token(MULT, "*"));
        table.add(new Token(CALL, "call"));
        table.add(new Token(CALLF, "callf"));
        table.add(new Token(REPEAT, "repeat"));
        table.add(new Token(UNTIL, "until"));
        table.add(new Token(EOF, "EOF"));

        for (char letra: LETER) {
            table.add(new Token(VARIABLE, String.valueOf(letra)));
        }
    }
    
    /**
     * Finds the next non-blank text in the input file.
     * If the file is finished, it returns the text "EOF"
     * @return Next non-blank text
     */
    public String nextText() {
        String text;
        do {
            text = null;
            // No current valid lineScanner
            if (lineScanner == null) {
                // about to read a new line
                if(fileScanner.hasNextLine()) {
                    // read next line an prepare line scanner
                    String line = fileScanner.nextLine();
                    lineScanner = new Scanner(line);
                } else {
                    // End Of File
                    text = "EOF";
                }
            } else {
                // reading from a current linescanner
                if(lineScanner.hasNext()) {
                    // There are more tokens in the current line
                    text = lineScanner.next();
                } else {
                    // End of the line, discard the lineScanner
                    lineScanner = null;
                }
            }
        } while (text == null);
        return text;
    }
    
    /**
     * Returns the next token (as code)
     * @return next token: code and text (if constant or variable)
     */
    public Token nextToken() {
        String text = nextText();
        // Check if text is a valid keyword
        for(Token t: table) {
            if(t.text.equals(text)) {
                return new Token(t.code, null);
            }
        }
        // Check if text is a valid variable name
        if(validVariableName(text)) return new Token(VARIABLE, text);
        // Check if text is a valid integer constant
        if(validIntConstant(text)) return new Token(CONSTANT, text);
        // If none of the above, return invalid token
        return new Token(INVALIDTOKEN, "");
    }
    
    /**
     * Returns true if the parameter is a valid variable name
     * (all characters are letters)
     * @param text text to be checked
     * @return true if text is a valid variable name, false otherwise
     */
    private boolean validVariableName(String text) {
        for(int i = 0; i < text.length(); i++) {
            if(!Character.isLetter(text.charAt(i))) return false;
        }
        return true;
    }
    
    /**
     * Returns true if the parameter is a valid integer constant
     * (all characters are digits>)
     * @param text text to be cheked
     * @return true if text is a valid integer constant, false otherwise
     */
    private boolean validIntConstant(String text) {
        for(int i = 0; i < text.length(); i++) {
            if(!Character.isDigit(text.charAt(i))) return false;
        }
        return true;
    }
    
    /**
     * Given the numerical code of a token, return the text associated
     * with the token.
     * Used to make the error reporting in the Parser more readable
     * @param code Numerical code of the token
     * @return Text associated with the token
     */
    public String getTokenText(int code) {
        for(Token t: table) {
            if(t.code == code) return t.text;
        }
        return "";
    }
    
    /**
     * Main program to test 
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String [] args) throws FileNotFoundException {
        Lexer lexer = new Lexer("main.txt");
        Token token = lexer.nextToken();
        while(token.code != EOF) {
            System.out.println(token);
            token = lexer.nextToken();
        }
    }
}
