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
    public static void main(String[] args) {
        boolean llvmtest = false;
        boolean armtest = false;
        ArrayList<Function> allb = new ArrayList<>();

        try {
            if (llvmtest) {
                IRScanner irs = new IRScanner();

                try {
                    ArrayList<Token> i = irs.scanfile("txt/llvmir.ll");
                    IRParser ip = new IRParser(i);
                    allb = ip.parseFunc(0);
                    System.out.println(allb.size());
                    for (Function function: allb) {
                        System.out.println("Func:\t" + function.getBlocklist().size());
                        for (Block block: function.getBlocklist()) {
                            System.out.println("Block:\t" + block.getInblocklist().size());
                            for (Instr instr: block.getInblocklist()) {
                                System.out.println(instr.toString());
                            }
                        }
                    }
                    System.out.println("LLVM End.");
                    if (armtest) {
                        if (allb.size() == 0) {
                            System.out.println("Err null allb.");

                        } else {
                            ArmGenerator ag = new ArmGenerator(allb);
                            ag.convertarm();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {

                //要测的程序或方法
                long startTime = System.currentTimeMillis(); //获取结束时间

                PrintStream printStream = new PrintStream("txt/output.txt");
                Lexer lexer = new Lexer("txt/testfile.sy");

                lexer.output1(printStream);
                Parser parser = new Parser(lexer.getRawWords());

                long endTime = System.currentTimeMillis(); //获取结束时间
                System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
