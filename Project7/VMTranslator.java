package Project7;

import java.io.*;
import java.util.*;

public class VMTranslator {

    static int ALabelnum = 0;
    static String fileName = "";

    // Common assembly snippets
    static String popD = "@SP\nAM=M-1\nD=M\n";
    static String getM = "@SP\nA=M-1\n";
    static String diffTrue = "D=M-D\nM=-1\n";
    static String makeFalse = "@SP\nA=M-1\nM=0\n";
    static String push = "@SP\nA=M\nM=D\n@SP\nM=M+1\n";

    static Map<String, String> arithmeticOperators = Map.of(
        "sub", "-", "add", "+", "and", "&", "or", "|", "neg", "-", "not", "!"
    );

    static Map<String, String> segmentCode = Map.of(
        "argument", "ARG", "local", "LCL", "this", "THIS", "that", "THAT",
        "temp", "5", "pointer", "3"
    );

    public static String unaryArithmetic(String[] command) {
        String operator = arithmeticOperators.getOrDefault(command[0], command[0] + "not found");
        return getM + "M=" + operator + "M\n";
    }

    public static String binaryArithmetic(String[] command) {
        String operator = arithmeticOperators.getOrDefault(command[0], command[0] + "not found");
        return popD + getM + "M=M" + operator + "D\n";
    }

    public static String conditional(String[] command) {
        String name = "ALabel_" + ALabelnum++;
        String jumpType = switch (command[0]) {
            case "gt" -> "JGT";
            case "eq" -> "JEQ";
            case "lt" -> "JLT";
            default -> "JMP";
        };
        String test = "@" + name + "\nD;" + jumpType + "\n";
        String label = "(" + name + ")\n";
        return popD + getM + diffTrue + test + makeFalse + label;
    }

    public static String pushFunction(String[] command) {
        String segment = command[1];
        String index = command[2];

        if (segment.equals("constant")) {
            return "@" + index + "\nD=A\n" + push;
        } else if (segment.equals("static")) {
            return "@" + fileName + "." + index + "\nD=M\n" + push;
        } else {
            String tempp = (segment.equals("temp") || segment.equals("pointer")) ? "A" : "M";
            String pointer = segmentCode.getOrDefault(segment, "invalid segment: " + segment + "\n");
            return "@" + index + "\nD=A\n@" + pointer + "\nA=" + tempp + "+D\nD=M\n" + push;
        }
    }

    public static String popFunction(String[] command) {
        String segment = command[1];
        String index = command[2];

        if (segment.equals("constant")) {
            throw new IllegalArgumentException("You cannot pop into a constant");
        } else if (segment.equals("static")) {
            return popD + "@" + fileName + "." + index + "\nM=D\n";
        } else {
            String tempp = (segment.equals("temp") || segment.equals("pointer")) ? "A" : "M";
            String pointer = segmentCode.getOrDefault(segment, "invalid segment: " + segment + "\n");
            return "@" + index + "\nD=A\n@" + pointer + "\nD=" + tempp + "+D\n@R13\nM=D\n"
                 + popD + "@R13\nA=M\nM=D\n";
        }
    }

    public static String translate(String line) {
        String codeLine = line.split("//")[0].trim();
        if (codeLine.isEmpty()) return "";

        String[] command = codeLine.split(" ");
        return switch (command[0]) {
            case "add", "sub", "and", "or" -> binaryArithmetic(command);
            case "neg", "not" -> unaryArithmetic(command);
            case "eq", "gt", "lt" -> conditional(command);
            case "push" -> pushFunction(command);
            case "pop" -> popFunction(command);
            default -> "\n//Error: " + command[0] + " not found \n\n";
        };
    }

    public static void main(String[] args) throws IOException {
        
        Scanner scan = new Scanner(System.in);
        String arg = scan.nextLine();
        fileName = new File(arg).getName().replace(".vm", "");

        BufferedReader reader = new BufferedReader(new FileReader(arg + ".vm"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(arg + ".asm"));

        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(translate(line));
        }

        reader.close();
        writer.close();
    }
}