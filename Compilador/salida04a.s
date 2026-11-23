.intel_syntax noprefix

.data
    _a: .quad 0
    _b: .quad 0
    _cte5: .quad 1
    _str6: .asciz "bucle -- hasta(a > 0)"
    _cte7: .quad 5
    _str8: .asciz "Estamos en la mitad"
    _cte9: .quad 1
    _cte10: .quad 10
    _numero: .quad 0
    _resultado: .quad 0
    _cte13: .quad 2
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
    push qword ptr [_cte5]
    push qword ptr [_a]
    pop rbx
    pop rax
    mov [_a], rax
    lea rax, [_str6]
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
    pop rcx
    call _func_numeroExterno
    push rax
    push qword ptr [_b]
    pop rbx
    pop rax
    mov [_b], rax
    push qword ptr [_b]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    push qword ptr [_a]
    push qword ptr [_cte7]
    pop rbx
    pop rax
    cmp rax, rbx
    jne etq_23
    jmp etq_21
etq_21:
    lea rax, [_str8]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
etq_23:
    push qword ptr [_a]
    push qword ptr [_cte5]
    pop rbx
    pop rax
    add rax, rbx
    push rax
    push qword ptr [_a]
    pop rbx
    pop rax
    mov [_a], rax
    push qword ptr [_a]
    push qword ptr [_cte10]
    pop rbx
    pop rax
    cmp rax, rbx
    jle etq_5
    jmp etq_35
etq_35:
    xor ecx, ecx
    call ExitProcess

_func_numeroExterno:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov [_numero], rcx
    push qword ptr [_numero]
    push qword ptr [_cte13]
    pop rbx
    pop rax
    imul rax, rbx
    push rax
    push qword ptr [_resultado]
    pop rbx
    pop rax
    mov [_resultado], rax
    push qword ptr [_resultado]
    pop rax
    add rsp, 32
    pop rbp
    ret
    xor eax, eax
    add rsp, 32
    pop rbp
    ret
