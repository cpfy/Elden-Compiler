

#### 示例1

递归

```
factorial:
    str lr, [sp,#-4]!  /* Push lr onto the top of the stack */
    str r4, [sp,#-4]!  /* Push r4 onto the top of the stack */
                       /* The stack is now 8 byte aligned */
    mov r4, r0         /* Keep a copy of the initial value of r0 in r4 */


    cmp r0, #0         /* compare r0 and 0 */
    bne is_nonzero     /* if r0 != 0 then branch */
    mov r0, #1         /* r0 ← 1. This is the return */
    b end
is_nonzero:
                       /* Prepare the call to factorial(n-1) */
    sub r0, r0, #1     /* r0 ← r0 - 1 */
    bl factorial
                       /* After the call r0 contains factorial(n-1) */
                       /* Load initial value of r0 (that we kept in r4) into r1 */
    mov r1, r4         /* r1 ← r4 */
    mul r0, r0, r1     /* r0 ← r0 * r1 */

end:
    ldr r4, [sp], #+4  /* Pop the top of the stack and put it in r4 */
    ldr lr, [sp], #+4  /* Pop the top of the stack and put it in lr */
    bx lr              /* Leave factorial */
```





#### 示例2

内存存取

```
/* -- store01.s */

/* -- Data section */
.data

/* Ensure variable is 4-byte aligned */
.balign 4
/* Define storage for myvar1 */
myvar1:
    /* Contents of myvar1 is just '3' */
    .word 0

/* Ensure variable is 4-byte aligned */
.balign 4
/* Define storage for myvar2 */
myvar2:
    /* Contents of myvar2 is just '3' */
    .word 0

/* -- Code section */
.text

/* Ensure function section starts 4 byte aligned */
.balign 4
.global main
main:
    ldr r1, addr_of_myvar1 /* r1 ← &myvar1 */
    mov r3, #3             /* r3 ← 3 */
    str r3, [r1]           /* *r1 ← r3 */
    ldr r2, addr_of_myvar2 /* r2 ← &myvar2 */
    mov r3, #4             /* r3 ← 4 */
    str r3, [r2]           /* *r2 ← r3 */

    /* Same instructions as above */
    ldr r1, addr_of_myvar1 /* r1 ← &myvar1 */
    ldr r1, [r1]           /* r1 ← *r1 */
    ldr r2, addr_of_myvar2 /* r2 ← &myvar2 */
    ldr r2, [r2]           /* r2 ← *r2 */
    add r0, r1, r2
    bx lr

/* Labels needed to access data */
addr_of_myvar1 : .word myvar1
addr_of_myvar2 : .word myvar2
```



#### 输入输出

```
/* -- printf02.s */
.data

/* First message */
.balign 4
message1: .asciz "Hey, type a number: "

/* Second message */
.balign 4
message2: .asciz "%d times 5 is %d\n"

/* Format pattern for scanf */
.balign 4
scan_pattern : .asciz "%d"

/* Where scanf will store the number read */
.balign 4
number_read: .word 0

.balign 4
return: .word 0

.balign 4
return2: .word 0

.text

/*
mult_by_5 function
*/
mult_by_5: 
    ldr r1, address_of_return2       /* r1 ← &address_of_return */
    str lr, [r1]                     /* *r1 ← lr */

    add r0, r0, r0, LSL #2           /* r0 ← r0 + 4*r0 */

    ldr lr, address_of_return2       /* lr ← &address_of_return */
    ldr lr, [lr]                     /* lr ← *lr */
    bx lr                            /* return from main using lr */
address_of_return2 : .word return2

.global main
main:
    ldr r1, address_of_return        /* r1 ← &address_of_return */
    str lr, [r1]                     /* *r1 ← lr */

    ldr r0, address_of_message1      /* r0 ← &message1 */
    bl printf                        /* call to printf */

    ldr r0, address_of_scan_pattern  /* r0 ← &scan_pattern */
    ldr r1, address_of_number_read   /* r1 ← &number_read */
    bl scanf                         /* call to scanf */

    ldr r0, address_of_number_read   /* r0 ← &number_read */
    ldr r0, [r0]                     /* r0 ← *r0 */
    bl mult_by_5

    mov r2, r0                       /* r2 ← r0 */
    ldr r1, address_of_number_read   /* r1 ← &number_read */
    ldr r1, [r1]                     /* r1 ← *r1 */
    ldr r0, address_of_message2      /* r0 ← &message2 */
    bl printf                        /* call to printf */

    ldr lr, address_of_return        /* lr ← &address_of_return */
    ldr lr, [lr]                     /* lr ← *lr */
    bx lr                            /* return from main using lr */
address_of_message1 : .word message1
address_of_message2 : .word message2
address_of_scan_pattern : .word scan_pattern
address_of_number_read : .word number_read
address_of_return : .word return

/* External */
.global printf
.global scanf
```





#### 寄存器详解

arm官方文档：

> https://developer.arm.com/documentation/dui0473/m/overview-of-the-arm-architecture/arm-registers

ARM processors provide general-purpose and special-purpose registers. Some additional registers are available in privileged execution modes.

In all ARM processors, the following registers are available and accessible in any processor mode:

* 13 general-purpose registers R0-R12.
* One Stack Pointer (SP).
* One Link Register (LR).
* One Program Counter (PC).
* One Application Program Status Register (APSR).





#### printf

示例

> https://stackoverflow.com/questions/40442133/how-to-use-printf-in-raspberry-pi-assembly-language



r0-r3

```
.data
.balign 4       
   string: .asciz "\n%d %d %d %d\n"        
.text
.global main
.extern printf

main:
    push    {ip, lr}        @ push return address + dummy register
                            @ for alignment
    ldr     r0, =string     @ get address of string into r0
    mov     r1, #11
    mov     r2, #22
    mov     r3, #33
    mov     r4, #444
    bl      printf          @ print string and pass params
                            @ into r1, r2, and r3
    pop     {ip, pc}        @ pop return address into pc
```

