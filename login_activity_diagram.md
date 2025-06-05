graph TD
    A[Inicio] --> B[Verificar Credenciales Guardadas]
    B -->|Sí| C[Iniciar Login Automático]
    B -->|No| D[Mostrar Pantalla Login]
    
    C -->|Éxito| E[Redirigir a Home]
    C -->|Error| D
    
    D --> F[Ingresar Email/Usuario]
    D --> G[Ingresar Contraseña]
    D --> H[Click en Iniciar Sesión]
    
    H --> I[Validar Campos]
    I -->|Campos Inválidos| J[Mostrar Errores]
    I -->|Campos Válidos| K[Mostrar Loading]
    
    K --> L[Enviar Datos al Servidor]
    L -->|Éxito| E
    L -->|Error| M[Mostrar Mensaje de Error]
    
    E --> N[Guardar Credenciales]
    E --> O[Actualizar UI]
