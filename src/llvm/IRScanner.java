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
    private boolean readingBool = false;
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
            System.out.println(t.tostring());
        }
        return tokenList;
    }

    public void writefile(String dir) throws IOException {
        File file = new File(dir);
        FileWriter writer = new FileWriter(file);
        for (Token t : tokenList) {
            writer.write(t.tostring() + "\n");
            System.out.println(t.tostring());
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
//                else if(readingNumber){
//                    createToken("INTCON");
//                    readingNumber=false;
//                    curToken += c;
//                    System.err.println(curToken);
//
//                    break;
//                }

                else if (readingBool) {
                    curToken = endOfOp();
                }
                if (curToken.isEmpty()) {
                    readingString = true;
                }
                curToken += c;
                break;
            case DIGIT:
                if (readingComments) {
                    curToken = "";
                    break;
                } else if (readingBool) {
                    curToken = endOfOp();
                } else if (readingString) {
                    // 为了处理i32，i1这种
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
                } else if (readingString) {
                    curToken = endOfWord();
                    //readingString = false;

                    //curToken += c;
                    //createToken(tokenDictionary.queryOpCode(String.valueOf(c)));
                    handleOperator(c);

                } else if (readingNumber) {
                    createToken("INTCON");
                    readingNumber = false;
                    //curToken += c;
                    //createToken(tokenDictionary.queryOpCode(String.valueOf(c)));
                    handleOperator(c);

                } else if (readingBool) {
                    if ((curToken.equals("<") && (c == '=')) ||
                            (curToken.equals(">") && (c == '=')) ||
                            (curToken.equals("=") && (c == '=')) ||
                            (curToken.equals("!") && (c == '=')) ||
                            (curToken.equals("|") && (c == '|')) ||
                            (curToken.equals("&") && (c == '&'))
                    ) {
                        curToken += c;
                        createToken(tokenDictionary.queryOpCode(curToken));

                    } else if (curToken.equals("/")) {
//                        if (c == '/') {
//                            readingSingleComments = true;
//                            curToken = "";
//                        } else if (c == '*') {
//                            readingMultiComments = true;
//                            curToken = "";
//                        } else {
                        //div
                        createToken(tokenDictionary.queryOpCode(curToken));
                        handleOperator(c);

                    } else {
                        createToken(tokenDictionary.queryOpCode(curToken));
                        handleOperator(c);
                    }
                    readingBool = false;

                } else {
                    handleOperator(c);
                }
                break;
            case SPACE:
                if (c == '\n') {  //newline, \n居然算SPACE
                    if (readingString) {
                        curToken = endOfWord();

                    } else if (readingNumber) {
                        createToken("INTCON");
                    }
                    curRows++;
                    resetStatus();
                }
                if (readingComments) {
                    curToken = "";
                    break;
                } else if (readingBool) {
                    createToken(tokenDictionary.queryOpCode(curToken));
                    readingBool = false;

                } else if (readingString) {
                    curToken = endOfWord();

                } else if (readingNumber) {
                    createToken("INTCON");
                    readingNumber = false;

                } else {
//                    curToken = endOfWord();
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

    private String endOfWord() {
        if (tokenDictionary.queryIfKeyword(curToken)) {
            createToken(tokenDictionary.queryKeywordCode(curToken));
        } else {
            if (curToken.length() > 0) {
                createToken("IDENFR");
            }
        }
        resetStatus();
        return curToken;
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
        if (c == ';') {
//            curToken = endOfWord();
            readingComments = true;
            curToken = "";

        }else if (c == '<' || c == '>' || c == '!' || c == '=' || c == '/' || c == '|' || c == '&') {
            curToken = endOfWord();
            readingBool = true;
            curToken += c;

        } else if (tokenDictionary.queryIfOpCode(String.valueOf(c))) {
            curToken = endOfWord();
            curToken = String.valueOf(c);
            createToken(tokenDictionary.queryOpCode(String.valueOf(c)));

        } else {
            System.err.println("Unhandled operator scanned!");
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
        readingBool = false;
        readingComments = false;
    }

//    //    判断是否新的行/文法结束
//    private boolean newSymLine() {
//        if (sym.getRow() > getLastToken().getRow()) {
//            return true;
//        }
//        return false;
//    }
}
