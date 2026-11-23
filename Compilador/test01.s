.intel_syntax noprefix

.data
    _a: .quad 0
    _b: .quad 0
    _c: .quad 0
    _d: .quad 0
    _cte6: .quad 2
    _cte7: .quad 10
    _str8: .asciz "a y b:"
    _str9: .asciz "Sumando a y b:"
    _str10: .asciz "Multiplicando a y b:"
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
    push qword ptr [_cte6]
    push qword ptr [_a]
    pop rbx
    pop rax
    mov [_a], rax
    push qword ptr [_cte7]
    push qword ptr [_b]
    pop rbx
    pop rax
    mov [_b], rax
    push qword ptr [_a]
    push qword ptr [_b]
    pop rbx
    pop rax
    add rax, rbx
    push rax
    push qword ptr [_c]
    pop rbx
    pop rax
    mov [_c], rax
    push qword ptr [_a]
    push qword ptr [_b]
    pop rbx
    pop rax
    imul rax, rbx
    push rax
    push qword ptr [_d]
    pop rbx
    pop rax
    mov [_d], rax
    lea rax, [_str8]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
    push qword ptr [_a]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    push qword ptr [_b]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    lea rax, [_str9]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
    push qword ptr [_c]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    lea rax, [_str10]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
    push qword ptr [_d]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    xor ecx, ecx
    call ExitProcess
