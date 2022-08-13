package llvm;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class IRScanner {
    private String curToken;
    private ArrayList<Token> tokenList;
    private final IRDictionary tokenDictionary;

    private int curRows = 1;
    private boolean readingString = false;
    private boolean readingNumber = false;
    private boolean readingIdent = false;   // 指%，@开头
    private boolean readingComments = false;

    private final String INPUT_DIR = "llvm.txt";
    private final String OUTPUT_DIR = "output.txt";

    public IRScanner() {
        this.curToken = "";
        this.tokenList = new ArrayList<>();
        this.tokenDictionary = new IRDictionary();
    }

    public ArrayList<Token> getTokens(int output) {
        try {
            scanfile(INPUT_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (output == 1) {
            try {
                writefile(OUTPUT_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tokenList;
    }

    public ArrayList<Token> scanfile(String dir) throws IOException {
        File file = new File(dir);
        FileReader reader = new FileReader(file);

        int c;
        while ((c = reader.read()) != -1) {
            char cc = (char) c;
            scanchar(cc);
        }
        for (Token t : tokenList) {
//            System.out.println(t.tostring());
        }
        return tokenList;
    }

    public void writefile(String dir) throws IOException {
        File file = new File(dir);
        FileWriter writer = new FileWriter(file);
        for (Token t : tokenList) {
            writer.write(t.tostring() + "\n");
//            System.out.println(t.tostring());
        }
        writer.flush();
        writer.close();
    }

    private void scanchar(char c) {
        IRDictionary.TYPE type = tokenDictionary.queryCharType(String.valueOf(c));
        switch (type) {
            case LETTER:
                if (readingComments) {
                    curToken = "";
                    break;
                }

                if (curToken.isEmpty()) {
                    if(!readingIdent){
                        readingString = true;
                    }

                }
                curToken += c;
                break;
            case DIGIT:
                if (readingComments) {
                    curToken = "";
                    break;

                } else if (readingString) {
                    // 为了处理i32，i1这种，如果没有readingIdent则分开读，否则继续、连在一起
                    endOfWord();
                }


                if (curToken.isEmpty()) {
                    readingNumber = true;
                }
                curToken += c;

                // 处理i32
//                if(curToken == "i32"){
//                    createToken("I32TK");
//                    resetStatus();
//                }

                break;
            case OPERATOR:
                if (readingComments) {
                    curToken = "";
                    break;
                } else if (readingIdent) {
                    endOfWord();
                    handleOperator(c);

                } else if (readingString) {
                    endOfWord();
                    handleOperator(c);

                } else if (readingNumber) {
                    createToken("INTCON");
                    readingNumber = false;

                    handleOperator(c);

                } else {
                    handleOperator(c);
                }
                break;
            case SPACE:
                // newline, \n居然算SPACE
                if (c == '\n') {
                    if (readingString || readingIdent) {
                        endOfWord();

                    } else if (readingNumber) {
                        createToken("INTCON");
                    }
                    curRows++;
                    resetStatus();
                }
                if (readingComments) {
                    curToken = "";
                    break;
                } else if (readingIdent || readingString) {
                    endOfWord();

                } else if (readingNumber) {
                    createToken("INTCON");
                    readingNumber = false;

                } else {
                    if (c == Character.toChars(10)[0]) {
                        curRows++;
                        resetStatus();
                    }
                }
                break;
            case OTHERS:
                break;
            default:
                System.err.println("Unhandled char scanned!");
        }

        // System.out.println(c);
    }

    private void endOfWord() {
        if (tokenDictionary.queryIfKeyword(curToken)) {
            createToken(tokenDictionary.queryKeywordCode(curToken));

        } else {
            if (curToken.length() > 0) {
                createToken("IDENFR");
                readingIdent = false;
            }
        }
        resetStatus();
    }

    private String endOfOp() {
        if (tokenDictionary.queryIfOpCode(curToken)) {
            createToken(tokenDictionary.queryOpCode(curToken));
        } else {
            System.err.println("Unhandled endOfOp!");
        }
        resetStatus();
        return curToken;
    }

    private String handleOperator(char c) {
        // 注释
        if (c == ';') {
            readingComments = true;
            curToken = "";

        } else if (c == '@' || c == '%') {
            endOfWord();
            curToken = String.valueOf(c);
            createToken(tokenDictionary.queryOpCode(String.valueOf(c)));
            readingIdent = true;

        } else if (tokenDictionary.queryIfOpCode(String.valueOf(c))) {
            endOfWord();
            curToken = String.valueOf(c);
            createToken(tokenDictionary.queryOpCode(String.valueOf(c)));

        } else {
            System.err.println("Unhandled operator scanned!：" + c);
        }
        return curToken;
    }

    private void createToken(String tokenCode) {
        Token t = new Token(tokenCode, curToken, curRows);
        tokenList.add(t);
        curToken = "";
    }

    private void resetStatus() {
        readingString = false;
        readingNumber = false;
        readingComments = false;
        readingIdent = false;
    }

}
