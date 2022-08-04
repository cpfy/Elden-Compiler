package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;


//    GetElementPtrInst
//	: "getelementptr" OptInBounds Type "," Type Value OptCommaSepMetadataAttachmentList
//	| "getelementptr" OptInBounds Type "," Type Value "," CommaSepTypeValueList OptCommaSepMetadataAttachmentList

public class GetElementPtrInst extends Instr {
    private Type type1;
    private Type type2;
    private Value v;

    // "call"
    public GetElementPtrInst(String instrname, Type t1, Type t2, Value v) {
        super(instrname);
        this.type1 = t1;
        this.type2 = t2;
        this.v = v;
    }

    public void setValue(Value i) {
        this.v = i;
    }

    public Value getV() {
        return this.v;
    }
}
