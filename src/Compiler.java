import backend.ArmGenerator;
import llvm.Block;
import llvm.Function;
import llvm.IRParser;
import llvm.IRScanner;
import llvm.Instr.Instr;
import llvm.Token;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) throws FileNotFoundException {
        String testFile = args[2];
        String outputFile = args[3];
        boolean optimize = false;
        if (args.length == 5) {
            optimize = true;
        }

        frontend(testFile);
        midend(optimize);
        backend(outputFile);
    }

    private static void frontend(String inputName) throws FileNotFoundException {
        Lexer lexer = new Lexer(inputName);
        Parser parser = new Parser(lexer.getRawWords());
    }

    private static void midend(boolean optimize) {
        //todo 添加中端相关处理，若optimize为true，则启动优化
    }

    private static void backend(String outputFile) {
        //todo 添加相关后端相关处理
    }
}
