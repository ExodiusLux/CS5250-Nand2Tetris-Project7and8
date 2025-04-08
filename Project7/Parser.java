package Project7;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class Parser {
    private Scanner commands;
    private String current_cmd;
    public static final int ARITHMETIC = 0;
    public static final int PUSH = 1;
    public static final int POP = 2;
    public static final int LABEL = 3;
    public static final int GOTO = 4;
    public static final int IF = 5;
    public static final int FUNCTION = 6;
    public static final int RETURN = 7;
    public static final int CALL = 8;
    private int Type_arg;
    private String arg1;
    private int arg2;
    public static final ArrayList<String> Arithmetic = new ArrayList<String>();

    static {
        Arithmetic.add("add");
        Arithmetic.add("sub");
        Arithmetic.add("neg");
        Arithmetic.add("eq");
        Arithmetic.add("gt");
        Arithmetic.add("lt");
        Arithmetic.add("and");
        Arithmetic.add("or");
        Arithmetic.add("not");
    }

    public Parser(File fileIn) {
        Type_arg = -1;
        arg1 = "";
        arg2 = -1;
        try {
            commands = new Scanner(fileIn);
            String pre_processed = "";
            String curr_line = "";
            while(commands.hasNext()){
                curr_line = TrimComment(commands.nextLine()).trim();
                if (curr_line.length() > 0) {
                    pre_processed += curr_line + "\n";
                }
            }
            commands = new Scanner(pre_processed.trim());
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }

    }

    public boolean contains_commands(){
       return commands.hasNextLine();
    }

    public void read(){
        current_cmd = commands.nextLine();
        arg1 = "";
        arg2 = -1;
        String[] command_split = current_cmd.split(" ");

        if (command_split.length > 3){
            throw new IllegalArgumentException("argument total exceeded");
        }
        if (Arithmetic.contains(command_split[0])){
            Type_arg = ARITHMETIC;
            arg1 = command_split[0];
        }
        else if (command_split[0].equals("return")) {
            Type_arg = RETURN;
            arg1 = command_split[0];
        }
        else {
            arg1 = command_split[1];
            if(command_split[0].equals("push")){
                Type_arg = PUSH;
            }
            else if(command_split[0].equals("pop")){
                Type_arg = POP;
            }
            else if(command_split[0].equals("label")){
                Type_arg = LABEL;
            }
            else if(command_split[0].equals("if")){
                Type_arg = IF;
            }
            else if (command_split[0].equals("goto")){
                Type_arg = GOTO;
            }
            else if (command_split[0].equals("function")){
                Type_arg = FUNCTION;
            }
            else if (command_split[0].equals("call")){
                Type_arg = CALL;
            }
            else {
                throw new IllegalArgumentException("command type not known");
            }
            if (Type_arg == PUSH || Type_arg == POP || Type_arg == FUNCTION || Type_arg == CALL){
                try {
                    arg2 = Integer.parseInt(command_split[2]);
                }catch (Exception e){
                    throw new IllegalArgumentException(e);
                }
            }
        }

    }

    public int commandType(){
        if (Type_arg != -1) {
            return Type_arg;
        }
        else {
            throw new IllegalStateException("No command");
        }
    }

    public String arg1(){
        if (commandType() != RETURN){
            return arg1;
        }
        else {
            throw new IllegalStateException("Can not get arg1 from a RETURN type command!");
        }
    }

    public int arg2(){
        if (commandType() == PUSH || commandType() == POP || commandType() == FUNCTION || commandType() == CALL){
            return arg2;
        }
        else {
            throw new IllegalStateException("Can not get arg2!");
        }
    }

    public static String TrimComment(String strIn){
        int position = strIn.indexOf("//");
        if (position != -1){
            strIn = strIn.substring(0, position);
        }
        return strIn;
    }

    public static String removeSpace(String strIn){
        String final_result = "";
        if (strIn.length() != 0){
            String[] segs = strIn.split(" ");
            for (String s: segs){
                final_result += s;
            }
        }
        return final_result;
    }

    public static String getExt(String fileName){
        int index = fileName.lastIndexOf('.');
        if (index != -1){
            return fileName.substring(index);
        }
        else {
            return "";
        }
    }
}