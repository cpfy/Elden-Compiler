package llvm;

import java.util.ArrayList;
import java.util.HashMap;

public class IRDictionary {
    private static final ArrayList<String> KEYWORDS_LIST;
    private static final HashMap<String, String> KEYWORDS_DICT;
    private static final HashMap<String, String> OPERATORS_DICT;
    private static final HashMap<String, TYPE> TYPE_DICT;

    enum TYPE {
        LETTER, DIGIT, OPERATOR, SPACE, OTHERS
    }

    static {
        KEYWORDS_LIST = new ArrayList<>();
        KEYWORDS_LIST.add("add");
        KEYWORDS_LIST.add("sub");
        KEYWORDS_LIST.add("mul");
        KEYWORDS_LIST.add("sdiv");
        KEYWORDS_LIST.add("icmp");
        KEYWORDS_LIST.add("and");
        KEYWORDS_LIST.add("or");
        KEYWORDS_LIST.add("call");
        KEYWORDS_LIST.add("alloca");
        KEYWORDS_LIST.add("load");
        KEYWORDS_LIST.add("store");
        KEYWORDS_LIST.add("br");
        KEYWORDS_LIST.add("ret");
        // 全局相关
        KEYWORDS_LIST.add("getelementptr");
        KEYWORDS_LIST.add("memset");
        KEYWORDS_LIST.add("define");
        KEYWORDS_LIST.add("global");
        KEYWORDS_LIST.add("label");
//        KEYWORDS_LIST.add("i32");
        KEYWORDS_LIST.add("float");
        KEYWORDS_LIST.add("void");

        // Some Key
        KEYWORDS_LIST.add("dso_local");
        KEYWORDS_LIST.add("nsw");
        KEYWORDS_LIST.add("align");
        KEYWORDS_LIST.add("zeroinitializer");
        KEYWORDS_LIST.add("inbounds");
        KEYWORDS_LIST.add("declare");
        KEYWORDS_LIST.add("constant");

        //其它指令
        KEYWORDS_LIST.add("zext");


        // cmp比较与跳转指令
        KEYWORDS_LIST.add("eq");
        KEYWORDS_LIST.add("ne");
        KEYWORDS_LIST.add("ugt");
        KEYWORDS_LIST.add("uge");
        KEYWORDS_LIST.add("ult");
        KEYWORDS_LIST.add("ule");
        KEYWORDS_LIST.add("sgt");
        KEYWORDS_LIST.add("sge");
        KEYWORDS_LIST.add("slt");
        KEYWORDS_LIST.add("sle");
    }

    static {
        KEYWORDS_DICT = new HashMap<>();
        for (String keyword : KEYWORDS_LIST) {
            KEYWORDS_DICT.put(keyword, String.format("%sTK", keyword.toUpperCase()));
        }
    }

    static {
        // 应该无用
        OPERATORS_DICT = new HashMap<>();

        // 加减乘除都删了；不行，负号，乘号都必须要！
        OPERATORS_DICT.put("+", "PLUS");
        OPERATORS_DICT.put("-", "MINUS");   // 负号
        OPERATORS_DICT.put("*", "MULT");    // pointer指针
        OPERATORS_DICT.put("%", "PERC");    // 取percent意
        OPERATORS_DICT.put("@", "AT");
        OPERATORS_DICT.put("#", "POUND");
        OPERATORS_DICT.put(":", "COLON");
        OPERATORS_DICT.put(";", "SEMICN");
        OPERATORS_DICT.put(".", "POINT");   // float小数点使用
//        OPERATORS_DICT.put("_", "UNDERLINE");    // 统一用作ident的首字符（废弃，用%,@判别）

        OPERATORS_DICT.put("=", "ASSIGN");  // 有用，不能删
        OPERATORS_DICT.put(",", "COMMA");
        OPERATORS_DICT.put("(", "LPARENT");
        OPERATORS_DICT.put(")", "RPARENT");
        OPERATORS_DICT.put("[", "LBRACK");
        OPERATORS_DICT.put("]", "RBRACK");
        OPERATORS_DICT.put("{", "LBRACE");
        OPERATORS_DICT.put("}", "RBRACE");
    }

    static {
        TYPE_DICT = new HashMap<>();

        //space: ASCII(1-32)
        for (int i = 1; i < 33; i++) {
            String c = String.valueOf(Character.toChars(i)[0]);
            TYPE_DICT.put(c, TYPE.SPACE);
        }

        //digit: ASCII(48-57)
        for (int i = 48; i < 58; i++) {
            String c = String.valueOf(Character.toChars(i)[0]);
            TYPE_DICT.put(c, TYPE.DIGIT);
        }

        //letter: ASCII(A-Z, 65-90; a-z, 97-122)
        for (int i = 65; i < 91; i++) {
            String c = String.valueOf(Character.toChars(i)[0]);
            TYPE_DICT.put(c, TYPE.LETTER);
            TYPE_DICT.put(c.toLowerCase(), TYPE.LETTER);
        }

        //下划线也算入Letter
        final int UNDERLINE = 95;
        TYPE_DICT.put(String.valueOf(Character.toChars(UNDERLINE)[0]), TYPE.LETTER);

        //operator
        for (String key : OPERATORS_DICT.keySet()) {
            TYPE_DICT.put(key, TYPE.OPERATOR);
        }
    }

    public TYPE queryCharType(String value) {
        if (TYPE_DICT.containsKey(value)) {
            return TYPE_DICT.get(value);
        }
        return TYPE.OTHERS;
    }

    public boolean queryIfOpCode(String value) {
        return OPERATORS_DICT.containsKey(value);
    }

    public String queryOpCode(String value) {
        return OPERATORS_DICT.get(value);
    }

    public boolean queryIfKeyword(String word) {
        //System.out.println("word = " + word);
        return KEYWORDS_DICT.containsKey(word);
    }

    public String queryKeywordCode(String word) {
        return KEYWORDS_DICT.get(word);
    }
}
