# MPAZ PRO 3.º

App Android de apoyo didáctico para estudiantes de 3.º básico de la Escuela Escritora Marcela Paz.
Organiza una unidad por asignatura (Lenguaje, Matemática, Ciencias Naturales e Historia) en torno a
objetivos de aprendizaje, con perfiles de estudiante, docente y jefatura UTP.

**Integrantes:** [Nombre Apellido] y [Nombre Apellido]
**Asignatura:** DSY1105 - Desarrollo de Aplicaciones Móviles, Duoc UC

## Funcionalidades

**Ingreso**
- Selección de perfil e inicio de sesión con RUT validado (dígito verificador).
- Estudiantes con PIN (día y mes de nacimiento); docentes y UTP con contraseña.
- Sesión guardada con DataStore.

**Estudiante**
- Asignaturas disponibles según la aprobación de UTP y la asignación del docente.
- Unidad con su objetivo de aprendizaje y un camino de 3 pasos: Aprende, Desafíos y Prueba final.
- Aprende: explicación, imagen de apoyo y ejemplo, con confirmación al terminar.
- Desafíos en 3 niveles de dificultad con retroalimentación inmediata.
- Prueba final con máximo de intentos y revisión de respuestas correctas e incorrectas.
- Acceso a las guías que sube el docente.

**Docente**
- Asignación de objetivos y máximo de intentos de la prueba final.
- Imagen de apoyo elegida desde la galería.
- Subida de guías (PDF, Word o imagen) desde los archivos del dispositivo.
- Informe pedagógico con logro por pregunta, fortalezas y aspectos a reforzar, validación docente
  y descarga en CSV. El informe no entrega diagnósticos ni decisiones automáticas.

**Jefatura UTP**
- Revisión de contenidos: aprobar o pedir cambios con observación obligatoria.

**General**
- Formularios validados con íconos y mensajes por campo.
- Animaciones en transiciones, tarjetas, botones de carga y respuestas.
- Diseño adaptable a celular y tablet, sin perder datos al girar la pantalla.

## Recursos nativos

| Recurso | Uso en la app |
|---|---|
| Internet | Detecta si hay conexión y muestra un aviso cuando se pierde |
| Galería | El docente elige la imagen de apoyo de cada contenido |
| Archivos (Storage Access Framework) | El docente sube guías y descarga el informe en CSV |

## Persistencia local

- **Room:** usuarios, objetivos, contenidos, preguntas, progreso, intentos, respuestas y guías.
- **DataStore:** sesión iniciada.

Se puede revisar con **App Inspection > Database Inspector** en Android Studio.

## Tecnologías

Kotlin, Jetpack Compose, Material 3, MVVM, Navigation Compose, Room, DataStore y corrutinas.

## Estructura

```
data/        Room (entidades y DAO), DataStore y repositorios
model/       Modelos de la interfaz (Rol, Sesion, LoginUiState)
navigation/  Rutas y navegación
ui/          Pantallas, componentes y tema
util/        Validaciones, archivos y conexión a internet
viewmodel/   ViewModels de cada pantalla
```

## Cómo ejecutar

1. Clonar el repositorio y abrir la carpeta en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o teléfono con Android 9 o superior.

Usuarios ficticios: estudiante `25418732-1` / PIN `0703`, docente `15842391-K` / `docente123`,
UTP `13276054-3` / `utp12345`.
