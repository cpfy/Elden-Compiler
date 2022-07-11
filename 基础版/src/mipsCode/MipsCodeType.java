package mipsCode;

public enum MipsCodeType {
    add,
    addi,
    sub,
    mult,
    divop,
    modop,
    mflo,
    mfhi,
    sll,
    beq,
    bne,
    seq,
    sne,
    sle,
    slt,
    slti,
    sge,
    sgt,
    j,
    jal,
    jr,
    lw,
    sw,
    syscall,
    li,
    la,
    moveop,
    dataSeg,  //.data
    textSeg,  //.text
    asciizSeg,  //.asciiz
    globlSeg,  //.globl
    label //产生标号
}
