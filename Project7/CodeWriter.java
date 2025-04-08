package Project7;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {

    private int Jump_flag;
    private PrintWriter printer_out;

    public CodeWriter(File fileOut) {
        try {
            printer_out = new PrintWriter(fileOut);
            Jump_flag = 0;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void writeArithmetic(String command){
        if (command.equals("add")){
            printer_out.print(ArithTemp1() + "M=D+M\n");
        }else if (command.equals("sub")){
            printer_out.print(ArithTemp1() + "M=M-D\n");
        }else if (command.equals("and")){
            printer_out.print(ArithTemp1() + "M=M&D\n");
        }else if (command.equals("or")){
            printer_out.print(ArithTemp1() + "M=M|D\n");
        }else if (command.equals("gt")){
            printer_out.print(ArithTemp2("JLE"));
            Jump_flag++;
        }else if (command.equals("lt")){
            printer_out.print(ArithTemp2("JGE"));
            Jump_flag++;
        }else if (command.equals("eq")){
            printer_out.print(ArithTemp2("JNE"));
            Jump_flag++;
        }else if (command.equals("not")){
            printer_out.print("@SP\nA=M-1\nM=!M\n");
        }else if (command.equals("neg")){
            printer_out.print("D=0\n@SP\nA=M-1\nM=D-M\n");
        }else {
            throw new IllegalArgumentException("Call writeArithmetic() for a non-arithmetic command");
        }

    }

    public void writePushPop(int command, String segment, int index){
        if (command == Parser.PUSH){
            if (segment.equals("constant")){
                printer_out.print("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
            }else if (segment.equals("local")){
                printer_out.print(pushTemplate1("LCL",index,false));
            }else if (segment.equals("argument")){
                printer_out.print(pushTemplate1("ARG",index,false));
            }else if (segment.equals("this")){
                printer_out.print(pushTemplate1("THIS",index,false));
            }else if (segment.equals("that")){
                printer_out.print(pushTemplate1("THAT",index,false));
            }else if (segment.equals("temp")){
                printer_out.print(pushTemplate1("R5", index + 5,false));
            }else if (segment.equals("pointer") && index == 0){
                printer_out.print(pushTemplate1("THIS",index,true));
            }else if (segment.equals("pointer") && index == 1){
                printer_out.print(pushTemplate1("THAT",index,true));
            }else if (segment.equals("static")){
                printer_out.print(pushTemplate1(String.valueOf(16 + index),index,true));
            }
        }else if(command == Parser.POP){
            if (segment.equals("local")){
                printer_out.print(popTemplate1("LCL",index,false));
            }else if (segment.equals("argument")){
                printer_out.print(popTemplate1("ARG",index,false));
            }else if (segment.equals("this")){
                printer_out.print(popTemplate1("THIS",index,false));
            }else if (segment.equals("that")){
                printer_out.print(popTemplate1("THAT",index,false));
            }else if (segment.equals("temp")){
                printer_out.print(popTemplate1("R5", index + 5,false));
            }else if (segment.equals("pointer") && index == 0){
                printer_out.print(popTemplate1("THIS",index,true));
            }else if (segment.equals("pointer") && index == 1){
                printer_out.print(popTemplate1("THAT",index,true));
            }else if (segment.equals("static")){
                printer_out.print(popTemplate1(String.valueOf(16 + index),index,true));
            }
        }else {
            throw new IllegalArgumentException("Call writePushPop() for a non-pushpop command");
        }

    }

    public void close(){
        printer_out.close();
    }

    private String ArithTemp1(){
        return "@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n";
    }

    private String ArithTemp2(String type){
        return "@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n" + "D=M-D\n" + "@FALSE" + Jump_flag + "\n" + "D;" + type + "\n" + "@SP\n" + "A=M-1\n" + "M=-1\n" + "@CONTINUE" + Jump_flag + "\n" + "0;JMP\n" + "(FALSE" + Jump_flag + ")\n" + "@SP\n" + "A=M-1\n" + "M=0\n" + "(CONTINUE" + Jump_flag + ")\n";
    }

    private String pushTemplate1(String segment, int index, boolean isDirect){
        String noPointerCode = (isDirect)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";
        return "@" + segment + "\n" + "D=M\n"+ noPointerCode + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n";
    }

    private String popTemplate1(String segment, int index, boolean isDirect){
        String noPointerCode = (isDirect)? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";
        return "@" + segment + "\n" + noPointerCode + "@R13\n" + "M=D\n" + "@SP\n" + "AM=M-1\n" + "D=M\n" + "@R13\n" + "A=M\n" + "M=D\n";
    }

}
