import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Compiler {
    public static void main(String[] args) {
        try {

            //要测的程序或方法
            long startTime=System.currentTimeMillis(); //获取结束时间

            PrintStream printStream = new PrintStream("txt/output.txt");
            Lexer lexer = new Lexer("txt/testfile.txt");

//            PrintStream printStream2 = new PrintStream("error.txt");
//            PrintStream printStream3 = new PrintStream("mips.txt");
//
//            parserE.output3(printStream2);
//
            lexer.output1(printStream);
            Parser parser = new Parser(lexer.getRawWords());

//
//            parser.output2(printStream);
            long endTime=System.currentTimeMillis(); //获取结束时间
            System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
//
//            MipsCodeGenerator mipsCodeGenerator = new MipsCodeGenerator(parser.getMidCodeList(), parser.getStrings());
//
//            mipsCodeGenerator.output(printStream3);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
