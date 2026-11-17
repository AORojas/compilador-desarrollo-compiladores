.intel_syntax noprefix

.data
    _a: .quad 0
    _b: .quad 0
    _cte3: .quad 1
    _cte4: .quad 10
    _cte5: .quad 0
    _buf_str: .space 256
    fmt_int: .asciz "%lld\n"
    fmt_str: .asciz "%s\n"

.text
    .global main
    .extern printf
    .extern strcpy
    .extern strcmp
    .extern strcat
    .extern ExitProcess


main:
    sub rsp, 40
    push qword ptr [_cte3]
    push qword ptr [_a]
    pop rbx
    pop rax
    mov [_a], rax
    push qword ptr [_cte4]
    push qword ptr [_b]
    pop rbx
    pop rax
    mov [_b], rax
    push qword ptr [_a]
    push qword ptr [_cte5]
    pop rbx
    pop rax
    cmp rax, rbx
    jle etq_22
    jmp etq_13
etq_13:
    push qword ptr [_b]
    push qword ptr [_cte5]
    pop rbx
    pop rax
    cmp rax, rbx
    jle etq_22
    jmp etq_20
etq_20:
    push qword ptr [_b]
    pop rdx
    lea rcx, [fmt_int]
    call printf
etq_22:
    xor ecx, ecx
    call ExitProcess
