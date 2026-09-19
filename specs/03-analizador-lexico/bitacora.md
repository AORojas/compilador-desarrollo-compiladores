# Bitácora — 01-diseno

| Fecha | Quién | Qué se hizo / decidió | Notas |
|-------|-------|-----------------------|-------|
| 12-09-2026 | CV | Primer `spec.md`. Con la misma se intenta generar el analizador léxico. | Chat: Con estas spec genera la estructura Java del analizador léxico, separa las matrices en archivos individuales. A continuación: Genera en main un código de ejemplo en GAUCHO, que se llame al analizador léxico y que imprima los tokens en pantalla. |
| 19-09-2026 | AOR | Segundo `spec.md`. Con la misma se intenta generar y ajustar el analizador léxico. | Se incorporó la prueba mediante un archivo `.gau` externo, cuya ruta se encuentra predefinida en el archivo `Main`. La ejecución del analizador se realiza desde la línea de comandos mediante los comandos indicados en el `README` del módulo léxico. La salida permite verificar para cada token su código, nombre, lexema y línea. |