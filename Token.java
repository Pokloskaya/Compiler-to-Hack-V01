/**
 * Class for storing a token.
 * It has two attributes: the numeric code and (possibly) the text
 * @author Helmuth Trefftz
 */
public class Token {
    int code;
    String text;
    
    /**
     * Constructor
     * @param code numerical code of the token
     * @param text Possibly: the text associated with the token
     */
    public Token(int code, String text) {
        this.code = code;
        this.text = text;
    }
    
    @Override
    public String toString() {
        return "" + code + " " + text;
    }
}
