.intel_syntax noprefix

.data
    _a: .quad 0
    _b: .quad 0
    _c: .quad 0
    _d: .quad 0
    _cte6: .quad 2
    _cte7: .quad 10
    _str8: .asciz "c y d"
    _str9: .asciz "if sin else"
    _str10: .asciz "-----------"
    _str11: .asciz "si (c > d)   ---  si (d > c)"
    _str12: .asciz "c es mayor"
    _str13: .asciz "d es mayor"
    _str14: .asciz "c es menor -- si anidado"
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
    push qword ptr [_c]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    push qword ptr [_d]
    pop rdx
    lea rcx, [fmt_int]
    call printf
    lea rax, [_str9]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
    lea rax, [_str10]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
    lea rax, [_str11]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
    push qword ptr [_c]
    push qword ptr [_d]
    pop rbx
    pop rax
    cmp rax, rbx
    jle etq_37
    jmp etq_35
etq_35:
    lea rax, [_str12]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
etq_37:
    push qword ptr [_d]
    push qword ptr [_c]
    pop rbx
    pop rax
    cmp rax, rbx
    jle etq_55
    jmp etq_44
etq_44:
    lea rax, [_str13]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
    push qword ptr [_c]
    push qword ptr [_d]
    pop rbx
    pop rax
    cmp rax, rbx
    jge etq_55
    jmp etq_53
etq_53:
    lea rax, [_str14]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf
etq_55:
    xor ecx, ecx
    call ExitProcess
