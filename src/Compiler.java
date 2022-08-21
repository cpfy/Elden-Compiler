import backend.ArmGenerator;
import backend.Reg.RegisterOld;
import llvm.Block;
import llvm.Function;
import llvm.IRParser;
import llvm.IRScanner;
import llvm.Instr.Instr;
import llvm.Token;
import pass.PassManager;
import tool.OutputControl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Compiler {

    private static ArrayList<Function> allb = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        OutputControl.setOutput(false);
        String testFile = args[3];
        String outputFile = args[2];
        boolean optimize = false;
        if (args.length == 5) {
            optimize = true;
            return;
        }
        optimize = true;
        setOptimize(optimize);
        frontend(testFile);
        midend(optimize);
        backend(outputFile);
    }

    private static void setOptimize(boolean optimize) {
        PassManager.optimize = optimize;
        ArmGenerator.enableOptimize = optimize;
        RegisterOld.optimize = optimize;
    }

    private static void frontend(String inputName) throws FileNotFoundException {
        Lexer lexer = new Lexer(inputName);
        Parser parser = new Parser(lexer.getRawWords());
    }

    private static void midend(boolean optimize) {
        //todo 添加中端相关处理，若optimize为true，则启动优化

        IRScanner irs = new IRScanner();
        try {
            ArrayList<Token> i = irs.scanfile("llvmir.ll");
            IRParser ip = new IRParser(i);
            allb = ip.parseFunc(0);
            new PassManager(ip.getAllfunctionlist());
            //增print函数，代替原来输入方法
            ip.printllvmOutputs();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void backend(String outputFile) {
        //todo 添加相关后端相关处理

        if (allb.size() == 0) {
            OutputControl.printMessage("Err null allb.");

        } else {
            ArmGenerator ag = new ArmGenerator(allb, outputFile);
            ag.convertarm();
        }

        OutputControl.printMessage("Complete compile.");
    }
}
