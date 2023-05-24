import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class CodeGenerator {
    PrintWriter writer;
    String code;
    int labelCount;
    
    public static final int NULL = 0;
    public static final int PUSH = 1;
    public static final int POP = 2;
    
    public static final int CONSTANT = 3;
    public static final int STATIC = 4;
    public static final int LOCAL = 5;
    public static final int ARGUMENT = 6;
    public static final int TEMP = 15;
    
    public static final int ADD = 7;
    public static final int SUB = 8;
    public static final int NEG = 9;
    public static final int EQ = 10;
    public static final int GT = 11;
    public static final int LT = 12;
    public static final int NOT = 13;
    
    public static final int RETURN = 14;
    
    Map<Integer, String> table;
    
    
    public CodeGenerator(String fileName) {
        try {
            writer = new PrintWriter(new FileWriter(fileName + ".vm"));
            code = "";
            table = new HashMap<>();
            initTable();
            labelCount = 0;
        } catch (IOException e) {
            System.out.println("Cannot write to file: " + fileName);
        }
    }
    
    private void initTable() {
        table.put(PUSH, "push");
        table.put(POP, "pop");
        table.put(CONSTANT, "constant");
        table.put(STATIC, "static");
        table.put(LOCAL, "local");
        table.put(ARGUMENT, "argument");
        table.put(TEMP, "temp");
        table.put(ADD, "add");
        table.put(SUB, "sub");
        table.put(NEG, "neg");
        table.put(EQ, "eq");
        table.put(GT, "gt");
        table.put(LT, "lt");
        table.put(NOT, "not");
        table.put(RETURN, "return");
    }
    
    public void generatePushPop(int popOrPush, int segment, int offset) {
        code += "\t" + table.get(popOrPush) + " "
                + table.get(segment) + " "
                + offset 
                + "\n";
    }
    
    public void generateMathOrLogic(int opCode) {
        code += "\t" + table.get(opCode)
                + "\n";
    }
    
    public void generateLabel(String label) {
        code += "label" + " " + label
                + "\n";
    }

    public void generateGoto(String label) {
        code += "\t" + "goto" + " " + label
                + "\n";
    }
    
    public void generateIfGoto(String label) {
        code += "\t" + "if-goto" + " " + label
                + "\n";
    }
    
    public void generateCall(String name, int numArgs) {
        code += "\t" + "call" + " " + name + " "
                + numArgs 
                + "\n";                
    }
    
    public void generateFunctionHeader(String name, int numParams) {
        code += "function" + " " + name + " "
                + numParams 
                + "\n";                
    }
    
    public String createLabel() {
        String s = "label" + this.labelCount;
        labelCount++;
        return s;
    }
    
    public void writeFile() {
        code += "function Main.mult 2\npush constant 0\npop local 0\npush constant 0\npop local 1\nlabel label0\npush local 0\npush argument 1\nlt\nnot\nif-goto label1\npush local 0\npush constant 1\nadd\npop local 0\npush local 1\npush argument 0\nadd\npop local 1\ngoto label0\nlabel label1\npush local 1\nreturn";
        writer.print(code);
        writer.close();
        System.out.println("Generated code: ");
        System.out.println(code);
    }
}