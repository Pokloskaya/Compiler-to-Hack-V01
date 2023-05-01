

/**
 * Main program
 * @author Helmuth Trefftz
 */
public class Main {
    public static void main(String [] args) {
        Parser parser = new Parser("main.txt");
        // Call the method associated with the starting symbol of the grammar
        parser.program();
    }
}
