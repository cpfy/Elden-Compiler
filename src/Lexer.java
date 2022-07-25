import word.RawWord;
import word.WordType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private String sourceCode = "";
    private ArrayList<RawWord> rawWords = new ArrayList<>();

    private ArrayList<String> errors = new ArrayList<>();

    private String token;

    private boolean endFlag = false;
    private HashMap<String, WordType> reservedWords = new HashMap<>();
    private int headPoint;
    private int line = 1;

    public Lexer(String inputpath) {
        initReservedWords();
        File input = new File(inputpath);
        StringBuilder str = new StringBuilder("");
        try {
            FileInputStream is = new FileInputStream(input);
            InputStreamReader isr =  new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);
            String line = null;
            while((line = in.readLine()) != null) {
                str.append(line).append('\r').append('\n');
            }
            in.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sourceCode = str.toString();
        sourceCode += "$";
        while (!endFlag) {
            getSys(sourceCode);
        }
    }

    private String toInt(String s) {
        String ans = s;
        if (s.length() == 1) {
            return s;
        }
        if (s.charAt(0) == '0') {
            if (s.charAt(1) == 'x' || s.charAt(1) == 'X') {
                ans = String.valueOf(Integer.valueOf(s.substring(2), 16));
            }
            else {
                ans = String.valueOf(Integer.valueOf(s, 8));
            }
        }
        return ans;
    }

    private String toFloat(String s) {
        //todo 浮点数常数
        String ans = null;

        return s;
    }
    public ArrayList<RawWord> getRawWords() {
        return rawWords;
    }

    private void error() {
        System.out.println("error at line: " + line);
    }

    private void initReservedWords() {
        reservedWords.put("main", WordType.MAINTK);
        reservedWords.put("const", WordType.CONSTTK);
        reservedWords.put("int", WordType.INTTK);
        reservedWords.put("break", WordType.BREAKTK);
        reservedWords.put("continue", WordType.CONTINUETK);
        reservedWords.put("if", WordType.IFTK);
        reservedWords.put("else", WordType.ELSETK);
        reservedWords.put("while", WordType.WHILETK);
        reservedWords.put("putf", WordType.PRINTFTK);
        reservedWords.put("return", WordType.RETURNTK);
        reservedWords.put("void", WordType.VOIDTK);
        reservedWords.put("float", WordType.FLOATTK);
    }

    private void clearToken() {
        token = "";
    }

    private boolean isLetter(char letter) {
        return letter >= 'a' && letter <= 'z'
                || letter >= 'A' && letter <= 'Z'
                || letter == '_';
    }

    private boolean isConstNum(char c) {
        return isDigit(c) || isDot(c) || isEorP(c)
                || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')
                || c == 'x' || c == 'X';
    }

    private boolean isEorP(char c) {
        return c == 'p' || c == 'P' || c == 'e' || c == 'E';
    }

    private boolean isDigit(char digit) {
        return digit >= '0' && digit <= '9';
    }

    private boolean isDot(char dot) {
        return dot == '.';
    }

    public void getSys(String sourceCode) {
        char ch;
        ch = sourceCode.charAt(headPoint);
        while (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') {
            if (ch == '\n') {
                line++;
            }
            headPoint++;
            ch = sourceCode.charAt(headPoint);
        }
        if (ch == '$') {
            endFlag = true;
            return;
        }
        clearToken();
        //单行注释
        if (ch == '/' && sourceCode.charAt(headPoint + 1) == '/') {
            headPoint += 2;
            ch = sourceCode.charAt(headPoint);
            while (!(ch == '\r' && sourceCode.charAt(headPoint + 1) == '\n')) {
                headPoint++;
                ch = sourceCode.charAt(headPoint);
            }

            line++;

            headPoint += 2;
            return;
        }
        //多行注释
        if (ch == '/' && sourceCode.charAt(headPoint + 1) == '*') {
            headPoint += 2;
            ch = sourceCode.charAt(headPoint);
            while (!(ch == '*' && sourceCode.charAt(headPoint + 1) == '/')) {
                if (ch == '\r' && sourceCode.charAt(headPoint + 1) == '\n') {
                    line++;
                }
                headPoint++;
                ch = sourceCode.charAt(headPoint);
            }
            headPoint += 2;
            return;
        }

        if (isLetter(ch)) {
            token += sourceCode.charAt(headPoint);
            headPoint++;
            while (isLetter(sourceCode.charAt(headPoint))
                    || isDigit(sourceCode.charAt(headPoint))) {
                token += sourceCode.charAt(headPoint);
                headPoint++;
            }
            rawWords.add(new RawWord(token, reservedWords.getOrDefault(token, WordType.IDENFR), line));
            return;
        }

        if (isDigit(ch) || isDot(ch)) {
            boolean isInt = true;
            while (isConstNum(sourceCode.charAt(headPoint))) {
                if (isDot(sourceCode.charAt(headPoint)) || isEorP(sourceCode.charAt(headPoint))) {
                    isInt = false;
                }
                if (isEorP(sourceCode.charAt(headPoint))) {
                    token += sourceCode.charAt(headPoint);
                    headPoint++;
                }
                token += sourceCode.charAt(headPoint);
                headPoint++;
            }
            if (isInt) {
                rawWords.add(new RawWord(toInt(token), WordType.INTCON, line));
            }
            else {
                rawWords.add(new RawWord(toFloat(token), WordType.FLOATCON, line));
            }
            return;
        }

        if (ch == '"') {
            token += sourceCode.charAt(headPoint);
            headPoint++;
            while (sourceCode.charAt(headPoint) != '"') {
                token += sourceCode.charAt(headPoint);
                headPoint++;
            }
            token += sourceCode.charAt(headPoint);
            headPoint++;
            rawWords.add(new RawWord(token, WordType.STRCON, line));
            return;
        }

        if (ch == '+') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.PLUS, line));
            return;
        }
        if (ch == '-') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.MINU, line));
            return;
        }
        if (ch == '*') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.MULT, line));
            return;
        }
        if (ch == '/') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.DIV, line));
            return;
        }
        if (ch == '%') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.MOD, line));
            return;
        }
        if (ch == ';') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.SEMICN, line));
            return;
        }
        if (ch == ',') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.COMMA, line));
            return;
        }
        if (ch == '(') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.LPARENT, line));
            return;
        }
        if (ch == ')') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.RPARENT, line));
            return;
        }
        if (ch == '[') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.LBRACK, line));
            return;
        }
        if (ch == ']') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.RBRACK, line));
            return;
        }
        if (ch == '{') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.LBRACE, line));
            return;
        }
        if (ch == '}') {
            token += ch;
            headPoint++;
            rawWords.add(new RawWord(token, WordType.RBRACE, line));
            return;
        }

        if (ch == '<') {
            token += ch;
            headPoint++;
            if (sourceCode.charAt(headPoint) == '=') {
                token += sourceCode.charAt(headPoint);
                headPoint++;
                rawWords.add(new RawWord(token, WordType.LEQ, line));
            }
            else {
                rawWords.add(new RawWord(token, WordType.LSS, line));
            }
            return;
        }

        if (ch == '>') {
            token += ch;
            headPoint++;
            if (sourceCode.charAt(headPoint) == '=') {
                token += sourceCode.charAt(headPoint);
                headPoint++;
                rawWords.add(new RawWord(token, WordType.GEQ, line));
            }
            else {
                rawWords.add(new RawWord(token, WordType.GRE, line));
            }
            return;
        }

        if (ch == '=') {
            token += ch;
            headPoint++;
            if (sourceCode.charAt(headPoint) == '=') {
                token += sourceCode.charAt(headPoint);
                headPoint++;
                rawWords.add(new RawWord(token, WordType.EQL, line));
            }
            else {
                rawWords.add(new RawWord(token, WordType.ASSIGN, line));
            }
            return;
        }

        if (ch == '!') {
            token += ch;
            headPoint++;
            if (sourceCode.charAt(headPoint) == '=') {
                token += sourceCode.charAt(headPoint);
                headPoint++;
                rawWords.add(new RawWord(token, WordType.NEQ, line));
            }
            else {
                rawWords.add(new RawWord(token, WordType.NOT, line));
            }
            return;
        }

        if (ch == '&') {
            token += ch;
            headPoint++;
            if (sourceCode.charAt(headPoint) == '&') {
                token += sourceCode.charAt(headPoint);
                headPoint++;
                rawWords.add(new RawWord(token, WordType.AND, line));
            }
            else {
                error();
            }
            return;
        }

        if (ch == '|') {
            token += ch;
            headPoint++;
            if (sourceCode.charAt(headPoint) == '|') {
                token += sourceCode.charAt(headPoint);
                headPoint++;
                rawWords.add(new RawWord(token, WordType.OR, line));
            }
            else {
                error();
            }
            return;
        }

        headPoint++;
        System.out.println(line);
        errors.add("error at: " + line);
    }

    public void output1(PrintStream printStream) {
        for (RawWord rawWord: rawWords) {
            printStream.println(rawWord.output1());
        }
        for (String s: errors) {
            printStream.println(s);
        }
    }

}
