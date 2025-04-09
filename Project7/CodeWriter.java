package Project7;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeWriter {

    private int Jump_flag;
    private PrintWriter printer_out;
    private static final Pattern labelReg = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");
    private static int label_count = 0;
    private static String file_name = "";

    public CodeWriter(File file_out) {
        try {
            file_name = file_out.getName();
            printer_out = new PrintWriter(file_out);
            Jump_flag = 0;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void writeArithmetic(String command){
        if (command.equals("add")){
            printer_out.print(ArithTemp1() + "M=D+M\n");
        }
        else if (command.equals("sub")){
            printer_out.print(ArithTemp1() + "M=M-D\n");
        }
        else if (command.equals("and")){
            printer_out.print(ArithTemp1() + "M=D&M\n");
        }
        else if (command.equals("or")){
            printer_out.print(ArithTemp1() + "M=D|M\n");
        }
        else if (command.equals("gt")){
            printer_out.print(ArithTemp2("JLE"));
            Jump_flag++;
        }
        else if (command.equals("lt")){
            printer_out.print(ArithTemp2("JGE"));
            Jump_flag++;
        }
        else if (command.equals("eq")){
            printer_out.print(ArithTemp2("JNE"));
            Jump_flag++;
        }
        else if (command.equals("not")){
            printer_out.print("@SP\nA=M-1\nM=!M\n");
        }
        else if (command.equals("neg")){
            printer_out.print("D=0\n@SP\nA=M-1\nM=D-M\n");
        }
        else {
            throw new IllegalArgumentException("Called writeArithmetic() for a non-arithmetic command");
        }

    }

    public void writePushPop(int command, String segment, int index){
        if (command == Parser.PUSH){
            if (segment.equals("constant")){
                printer_out.print("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
            }
            else if (segment.equals("local")){
                printer_out.print(pushTemp("LCL",index,false));
            }
            else if (segment.equals("argument")){
                printer_out.print(pushTemp("ARG",index,false));
            }
            else if (segment.equals("this")){
                printer_out.print(pushTemp("THIS",index,false));
            }
            else if (segment.equals("that")){
                printer_out.print(pushTemp("THAT",index,false));
            }
            else if (segment.equals("temp")){
                printer_out.print(pushTemp("R5", index + 5,false));
            }
            else if (segment.equals("pointer") && index == 0){
                printer_out.print(pushTemp("THIS",index,true));
            }
            else if (segment.equals("pointer") && index == 1){
                printer_out.print(pushTemp("THAT",index,true));
            }
            else if (segment.equals("static")){
                printer_out.print("@" + file_name + index + "\n" + "D=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
            }
        }
        else if(command == Parser.POP){
            if (segment.equals("local")){
                printer_out.print(popTemp("LCL",index,false));
            }
            else if (segment.equals("argument")){
                printer_out.print(popTemp("ARG",index,false));
            }
            else if (segment.equals("this")){
                printer_out.print(popTemp("THIS",index,false));
            }
            else if (segment.equals("that")){
                printer_out.print(popTemp("THAT",index,false));
            }
            else if (segment.equals("temp")){
                printer_out.print(popTemp("R5", index + 5,false));
            }
            else if (segment.equals("pointer") && index == 0){
                printer_out.print(popTemp("THIS",index,true));
            }
            else if (segment.equals("pointer") && index == 1){
                printer_out.print(popTemp("THAT",index,true));
            }
            else if (segment.equals("static")){
                printer_out.print("@" + file_name + index + "\nD=A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n");
            }
        }else {
            throw new IllegalArgumentException("Call writePushPop() for a non-pushpop command");
        }

    }

    public void writeLabel(String label){
        Matcher m = labelReg.matcher(label);
        if (m.find()){
            printer_out.print("(" + label +")\n");
        }
        else {
            throw new IllegalArgumentException("label format incorrect");
        }
    }

    public void writeGoto(String label){
        Matcher match = labelReg.matcher(label);
        if (match.find()){
            printer_out.print("@" + label +"\n0;JMP\n");
        }
        else {
            throw new IllegalArgumentException("label format incorrect");
        }

    }

    public void writeIf(String label){
        Matcher match = labelReg.matcher(label);
        if (match.find()){
            printer_out.print(ArithTemp1() + "@" + label +"\nD;JNE\n");
        }
        else {
            throw new IllegalArgumentException("label format incorrect");
        }

    }
    
    public void writeInit(){
        printer_out.print("@256\n" + "D=A\n" + "@SP\n" + "M=D\n");
        writeCall("Sys.init",0);

    }

    public void writeCall(String func_name, int num_args){
        String new_label = "RETURN_LABEL" + (label_count++);

        printer_out.print("@" + new_label + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        printer_out.print(pushTemp("LCL",0,true));
        printer_out.print(pushTemp("ARG",0,true));
        printer_out.print(pushTemp("THIS",0,true));
        printer_out.print(pushTemp("THAT",0,true));
        printer_out.print("@SP\n" + "D=M\n" + "@5\n" + "D=D-A\n" + "@" + num_args + "\n" + "D=D-A\n" + "@ARG\n" + "M=D\n" + "@SP\n" + "D=M\n" + "@LCL\n" + "M=D\n" + "@" + func_name + "\n" + "0;JMP\n" + "(" + new_label + ")\n");
    }

    public void writeReturn(){
        printer_out.print(returnTemp());
    }

    public void writeFunction(String func_name, int locals_num){
        printer_out.print("(" + func_name +")\n");
        for (int i = 0; i < locals_num; i++){
            writePushPop(Parser.PUSH,"constant",0);
        }
    }

    public String FrameTemp(String pos){
        return "@R11\n" + "D=M-1\n" + "AM=D\n" + "D=M\n" + "@" + pos + "\n" + "M=D\n";
    }

    public String returnTemp(){
        return "@LCL\n" + "D=M\n" + "@R11\n" + "M=D\n" + "@5\n" + "A=D-A\n" + "D=M\n" + "@R12\n" + "M=D\n" + popTemp("ARG",0,false) + "@ARG\n" + "D=M\n" + "@SP\n" + "M=D+1\n" + FrameTemp("THAT") + FrameTemp("THIS") + FrameTemp("ARG") + FrameTemp("LCL") + "@R12\n" + "A=M\n" + "0;JMP\n";
    }

    public void setFileName(File file_out){
        file_name = file_out.getName();
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

    private String pushTemp(String segment, int index, boolean isDirect){
        String noPointerCode = (isDirect)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";
        return "@" + segment + "\n" + "D=M\n"+ noPointerCode + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n";
    }

    private String popTemp(String segment, int index, boolean isDirect){
        String noPointerCode = (isDirect)? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";
        return "@" + segment + "\n" + noPointerCode + "@R13\n" + "M=D\n" + "@SP\n" + "AM=M-1\n" + "D=M\n" + "@R13\n" + "A=M\n" + "M=D\n";
    }

}
