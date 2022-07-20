### Grammar-lang

> BNF范式：https://lists.llvm.org/pipermail/llvm-dev/2018-June/123851.html

#### TopLevel

有用的应该就是下面列的几个

```
TopLevelEntity
	: SourceFilename
	| TargetDefinition
	| ModuleAsm
	| TypeDef
	| ComdatDef
	| GlobalDecl
	| GlobalDef
	| IndirectSymbolDef
	| FunctionDecl
	| FunctionDef
	| AttrGroupDef
	| NamedMetadataDef
	| MetadataDef
	| UseListOrder
	| UseListOrderBB
```

#### Global

##### GlobalDecl

```
GlobalDecl
	: GlobalIdent "=" ExternLinkage OptPreemptionSpecifier OptVisibility OptDLLStorageClass OptThreadLocal OptUnnamedAddr OptAddrSpace OptExternallyInitialized Immutable Type GlobalAttrs FuncAttrs
```

##### GlobalDef

```
GlobalDef
	: GlobalIdent "=" OptLinkage OptPreemptionSpecifier OptVisibility OptDLLStorageClass OptThreadLocal OptUnnamedAddr OptAddrSpace OptExternallyInitialized Immutable Type Constant GlobalAttrs FuncAttrs
```

##### GlobalIdent

```
GlobalIdent
	: global_ident
```

##### global_ident

```
global_ident
	: _global_name
	| _global_id
```

##### _global_name

```
_global_name
	: '@' ( _name | _quoted_name )
```

##### _global_id

```
_global_id
	: '@' _id
```

#### Func

##### FunctionDecl

```
FunctionDecl
	: "declare" MetadataAttachments OptExternLinkage FunctionHeader
```

##### FunctionDef

```
FunctionDef
	: "define" OptLinkage FunctionHeader MetadataAttachments FunctionBody
```

##### FunctionHeader

```
FunctionHeader
	: OptPreemptionSpecifier OptVisibility OptDLLStorageClass OptCallingConv ReturnAttrs Type GlobalIdent "(" Params ")" OptUnnamedAddr FuncAttrs OptSection OptComdat OptGC OptPrefix OptPrologue OptPersonality
```

##### FunctionBody

```
FunctionBody
	: "{" BasicBlockList UseListOrders "}"
```

##### Params系列

```
Params
	: empty
	| "..."
	| ParamList
	| ParamList "," "..."
;

ParamList
	: Param
	| ParamList "," Param
;

Param
	: Type ParamAttrs
	| Type ParamAttrs LocalIdent
;
```



#### Block

##### BasicBlockList

```
BasicBlockList
	: BasicBlock
	| BasicBlockList BasicBlock
```

##### BasicBlock

```
BasicBlock
	: OptLabelIdent Instructions Terminator
```

#### Instruction

##### Instructions

```
Instructions
	: empty
	| InstructionList
```

##### InstructionList

```
InstructionList
	: Instruction
	| InstructionList Instruction
```

##### Instruction

```
Instruction
	// Instructions not producing values.
	: StoreInst
	| FenceInst
	| CmpXchgInst
	| AtomicRMWInst
	// Instructions producing values.
	| LocalIdent "=" ValueInstruction
	| ValueInstruction
```

##### ValueInstruction

```
ValueInstruction
	// Binary instructions
	: AddInst
	| FAddInst
	| SubInst
	| FSubInst
	| MulInst
	| FMulInst
	| UDivInst
	| SDivInst
	| FDivInst
	| URemInst
	| SRemInst
	| FRemInst
	// Bitwise instructions
	| ShlInst
	| LShrInst
	| AShrInst
	| AndInst
	| OrInst
	| XorInst
	// Vector instructions
	| ExtractElementInst
	| InsertElementInst
	| ShuffleVectorInst
	// Aggregate instructions
	| ExtractValueInst
	| InsertValueInst
	// Memory instructions
	| AllocaInst
	| LoadInst
	| GetElementPtrInst
	// Conversion instructions
	| TruncInst
	| ZExtInst
	| SExtInst
	| FPTruncInst
	| FPExtInst
	| FPToUIInst
	| FPToSIInst
	| UIToFPInst
	| SIToFPInst
	| PtrToIntInst
	| IntToPtrInst
	| BitCastInst
	| AddrSpaceCastInst
	// Other instructions
	| ICmpInst
	| FCmpInst
	| PhiInst
	| SelectInst
	| CallInst
	| VAArgInst
	| LandingPadInst
	| CatchPadInst
	| CleanupPadInst
```

##### Binary类

```
AddInst
	: "add" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

FAddInst
	: "fadd" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

SubInst
	: "sub" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

FSubInst
	: "fsub" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

MulInst
	: "mul" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

FMulInst
	: "fmul" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

UDivInst（未用到）
	: "udiv" OptExact Type Value "," Value OptCommaSepMetadataAttachmentList
;

SDivInst
	: "sdiv" OptExact Type Value "," Value OptCommaSepMetadataAttachmentList
;

FDivInst
	: "fdiv" FastMathFlags Type Value "," Value OptCommaSepMetadataAttachmentList
;

```



##### StoreInst

```
StoreInst
	: "store" OptVolatile Type Value "," Type Value OptCommaSepMetadataAttachmentList
	| "store" OptVolatile Type Value "," Type Value "," Alignment OptCommaSepMetadataAttachmentList
	| "store" "atomic" OptVolatile Type Value "," Type Value OptSyncScope AtomicOrdering OptCommaSepMetadataAttachmentList
	| "store" "atomic" OptVolatile Type Value "," Type Value OptSyncScope AtomicOrdering "," Alignment OptCommaSepMetadataAttachmentList
;
```

##### LoadInst

```
LoadInst
	// Load.
	: "load" OptVolatile Type "," Type Value OptCommaSepMetadataAttachmentList
	| "load" OptVolatile Type "," Type Value "," Alignment OptCommaSepMetadataAttachmentList
	// Atomic load.
	| "load" "atomic" OptVolatile Type "," Type Value OptSyncScope AtomicOrdering OptCommaSepMetadataAttachmentList
	| "load" "atomic" OptVolatile Type "," Type Value OptSyncScope AtomicOrdering "," Alignment OptCommaSepMetadataAttachmentList
```

#### Terminator

```
Terminator
	: RetTerm
	| BrTerm
	| CondBrTerm
	| SwitchTerm
	| IndirectBrTerm
	| InvokeTerm
	| ResumeTerm
	| CatchSwitchTerm
	| CatchRetTerm
	| CleanupRetTerm
	| UnreachableTerm
;
```

##### 各种Term

```
RetTerm
	// Void return.
	: "ret" VoidType OptCommaSepMetadataAttachmentList
	// Value return.
	| "ret" ConcreteType Value OptCommaSepMetadataAttachmentList
;

BrTerm
	: "br" LabelType LocalIdent OptCommaSepMetadataAttachmentList
;

CondBrTerm
	: "br" IntType Value "," LabelType LocalIdent "," LabelType LocalIdent OptCommaSepMetadataAttachmentList
;
```



#### Float

##### float_lit

```
float_lit
	: _frac_lit
	| _sci_lit
	| _float_hex_lit
```

##### _frac_lit

```
_frac_lit
	: [ _sign ] _decimals '.' { _decimal_digit }
```

##### _sci_lit

```
_sci_lit
	: _frac_lit ( 'e' | 'E' ) [ _sign ] _decimals
```

##### _float_hex_lit

```
_float_hex_lit
	:  '0' 'x'      _hex_digit { _hex_digit }
	|  '0' 'x' 'K'  _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit _hex_digit 
```

#### Array

##### GetElementPtrInst

```
GetElementPtrInst
	: "getelementptr" OptInBounds Type "," Type Value OptCommaSepMetadataAttachmentList
	| "getelementptr" OptInBounds Type "," Type Value "," CommaSepTypeValueList OptCommaSepMetadataAttachmentList
```



##### GetElementPtrExpr

```
GetElementPtrExpr
	: "getelementptr" OptInBounds "(" Type "," Type Constant "," GEPConstIndices ")"
```

##### OptInBounds

无用

```
OptInBounds
	: empty
	| "inbounds"
```



##### GEP系列

```
GEPConstIndices
	: empty
	| GEPConstIndexList
;

GEPConstIndexList
	: GEPConstIndex
	| GEPConstIndexList "," GEPConstIndex
;

GEPConstIndex
	: OptInrange Type Constant
;

OptInrange
	: empty
	| "inrange"
```

### Basic

#### Type系列

```
Type
	: VoidType
	// Types '(' ArgTypeListI ')' OptFuncAttrs
	| FuncType
	| FirstClassType
;

FirstClassType
	: ConcreteType
	| MetadataType
;
```

##### ConcreteType

```
ConcreteType
	: IntType
	// Type ::= 'float' | 'void' (etc)
	| FloatType
	// Type ::= Type '*'
	// Type ::= Type 'addrspace' '(' uint32 ')' '*'
	| PointerType
	// Type ::= '<' ... '>'
	| VectorType
	| LabelType
	// Type ::= '[' ... ']'
	| ArrayType
	// Type ::= StructType
	| StructType
	// Type ::= %foo
	// Type ::= %4
	| NamedType
	| MMXType
	| TokenType
```

##### voidType

```
VoidType
	: "void"
```



#### Constants

##### Constant

```
Constant
	: BoolConst
	| IntConst
	| FloatConst
	| NullConst
	| NoneConst
	| StructConst
	| ArrayConst
	| CharArrayConst
	| VectorConst
	| ZeroInitializerConst
	// @42
	// @foo
	| GlobalIdent
	| UndefConst
	| BlockAddressConst
	| ConstantExpr
```

##### Other

```
IntConst
	: int_lit
;

IntLit
	: int_lit
;

FloatConst
	: float_lit
```

