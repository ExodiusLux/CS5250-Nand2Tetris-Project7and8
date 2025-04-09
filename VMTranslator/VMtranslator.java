package VMTranslator;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;


public class VMtranslator {
    public static ArrayList<File> getFiles(File directory) {
        ArrayList<File> final_result = new ArrayList<>();
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    final_result.addAll(getFiles(file));
                } else if (file.getName().endsWith(".vm")) {
                    final_result.add(file);
                }
            }
        }
    
        return final_result;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String ScanIn = scan.nextLine();

        File file_in = new File(ScanIn); //if doing multi-file vms only give path to directory, if single file, give path to file itself ex. multi-file = FunctionCalls\SimpleFunction ; single file = MemoryAccess\BasicTest\BasicTest.vm
        String file_out_path = "";
        File file_out;
        CodeWriter asm_writer;

        ArrayList<File> vm_files = new ArrayList<File>();
        scan.close();

        if (file_in.isFile()) {
            String path = file_in.getAbsolutePath();
            if (!Parser.getExt(path).equals(".vm")) {
                throw new IllegalArgumentException("file needs to be .vm file");
            }
            vm_files.add(file_in);
            file_out_path = file_in.getAbsolutePath().substring(0, file_in.getAbsolutePath().lastIndexOf(".")) + ".asm";
        } 
        else if (file_in.isDirectory()) {
            vm_files = getFiles(file_in);
            if (vm_files.size() == 0) {
                throw new IllegalArgumentException("No .vm files in the directory");
            }
            file_out_path = file_in.getAbsolutePath() + "/" +  file_in.getName() + ".asm";
        }

        file_out = new File(file_out_path);
        asm_writer = new CodeWriter(file_out);

        if (file_in.isDirectory()) {
            asm_writer.writeInit();
        }

        for (File file : vm_files) {
            asm_writer.setFileName(file);
            Parser parser = new Parser(file);
            int command_type = -1;

            while (parser.contains_commands()) {
                parser.read();
                command_type = parser.returnCommandType();
                if (command_type == Parser.ARITHMETIC) {
                    asm_writer.writeArithmetic(parser.arg1());
                } 
                else if (command_type == Parser.POP || command_type == Parser.PUSH) {
                    asm_writer.writePushPop(command_type, parser.arg1(), parser.arg2());
                } 
                else if (command_type == Parser.LABEL) {
                    asm_writer.writeLabel(parser.arg1());
                } 
                else if (command_type == Parser.GOTO) {
                    asm_writer.writeGoto(parser.arg1());
                } 
                else if (command_type == Parser.IF) {
                    asm_writer.writeIf(parser.arg1());
                } 
                else if (command_type == Parser.RETURN) {
                    asm_writer.writeReturn();
                } 
                else if (command_type == Parser.FUNCTION) {
                    asm_writer.writeFunction(parser.arg1(),parser.arg2());
                } 
                else if (command_type == Parser.CALL) {
                    asm_writer.writeCall(parser.arg1(),parser.arg2());
                }
            }
        }
        asm_writer.close();

        System.out.println("asm file created at: " + file_out_path);
    
    }
}