/**
 * Main program
 */
public class Main {
    public static void main(String [] args) {
        Parser parser = new Parser("Main");
        // Call the method associated with the starting symbol of the grammar
        parser.program();
    }
}
