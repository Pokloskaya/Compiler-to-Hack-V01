import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    int pos = 0;
    Map<String, Integer> table;
    
    public SymbolTable() {
        table = new HashMap<>();
    }
    
    public void add(String name) {
        table.put(name, pos);
        pos++;
    }
    
    public int find(String name) {
        Integer i = table.get(name);
        if(i == null) return -1;
        return i;
    }
    
    public static void main(String [] args) {
        SymbolTable table = new SymbolTable();
        table.add("var1");
        table.add("var2");
        System.out.println(table.find("var1"));
        System.out.println(table.find("var2"));
        System.out.println(table.find("var3"));
    }
}
