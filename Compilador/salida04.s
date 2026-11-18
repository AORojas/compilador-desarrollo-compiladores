.intel_syntax noprefix

.data
    _a: .quad 0
    _cte3: .quad 1
    _str4: .asciz "bucle -- hasta(a > 0)"
    _cte5: .quad 5
    _str6: .asciz "Estamos en la mitad"
    _cte7: .quad 1
    _cte8: .quad 0
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
    lea rax, [_str4]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
etq_5:
    push qword ptr [_a]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    push qword ptr [_a]
    push qword ptr [_cte5]
    pop rbx
    pop rax
    cmp rax, rbx
    jne etq_16
    jmp etq_14
etq_14:
    lea rax, [_str6]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
etq_16:
    push qword ptr [_a]
    push qword ptr [_cte3]
    pop rbx
    pop rax
    add rax, rbx
    push rax
    push qword ptr [_a]
    pop rbx
    pop rax
    mov [_a], rax
    push qword ptr [_a]
    push qword ptr [_cte8]
    pop rbx
    pop rax
    cmp rax, rbx
    jle etq_5
    jmp etq_28
etq_28:
    xor ecx, ecx
    call ExitProcess
