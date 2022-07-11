import midCode.MidCodeList;
import mipsCode.MipsCodeGenerator;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Compiler {
    public static void main(String[] args) {
        try {
            PrintStream printStream = new PrintStream("output.txt");
            Lexer lexer = new Lexer("testfile.txt");
//            PrintStream printStream2 = new PrintStream("error.txt");
//            PrintStream printStream3 = new PrintStream("mips.txt");
//
//            parserE.output3(printStream2);
//
            Parser parser = new Parser(lexer.getRawWords());
//            lexer.output1(printStream);
//
            parser.output2(printStream);
//
//            MipsCodeGenerator mipsCodeGenerator = new MipsCodeGenerator(parser.getMidCodeList(), parser.getStrings());
//
//            mipsCodeGenerator.output(printStream3);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
