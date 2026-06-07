# Aplicación Móvil - Taller Mecánico

Esta es la aplicación móvil desarrollada en **Kotlin** y **Jetpack Compose** para el control y administración de un taller mecánico de vehículos. La aplicación se conecta a un backend desarrollado en **Django REST Framework** y base de datos relacional **PostgreSQL**, garantizando persistencia segura y control de roles.

---

## 1. Descripción de la Aplicación Móvil
La aplicación proporciona una interfaz interactiva y moderna para gestionar el flujo operativo completo del taller mecánico. Permite el control del personal (Administradores, Mecánicos, Clientes) y el ciclo de vida del mantenimiento vehicular, desde el registro de clientes y vehículos, asignación de órdenes de trabajo, seguimiento de estados, hasta la emisión de facturas y pasarela de registro de pagos parciales o totales.

---

## 2. Requisitos de Instalación
* **Sistema Operativo**: Android 8.0 (API Level 26) o superior.
* **Android Studio**: Android Studio Koala | 2024.1.1 o superior.
* **Kotlin**: SDK de Kotlin v1.9.0+.
* **JDK**: Java Development Kit 17.
* **Gradle**: Gradle Wrapper 8.0+.

---

## 3. Configuración de la URL Base del Backend
La URL base del backend está configurada de manera centralizada en el archivo `RetrofitInstance.kt`:
* **Ruta del archivo**: `app/src/main/java/com/example/tallermecanico/data/RetrofitInstance.kt`
* **URL de Producción**: `https://mazsorra-taller.uaeftt-ute.site/`

---

## 4. Usuarios de Prueba
Para validar los diferentes niveles de permisos establecidos en la rúbrica, utilice las siguientes credenciales:

### Usuario Administrador (Acceso Total: Lectura y Escritura)
* **Correo**: `admin@taller.com`
* **Contraseña**: `admin`
* *Permisos*: Crear, Leer, Actualizar y Eliminar Clientes, Vehículos, Servicios, Órdenes y Facturas.

### Usuario Mecánico (Acceso Operativo)
* **Correo**: `mecanico@taller.com`
* **Contraseña**: `mecanico1234`
* *Permisos*: Leer registros, registrar/editar vehículos y actualizar estados y observaciones de órdenes de trabajo. No puede eliminar registros.

### Usuario Cliente (Acceso de Consulta)
* **Correo**: `cliente@taller.com`
* **Contraseña**: `cliente1234`
* *Permisos*: Consultar información general (sus vehículos y estado de sus órdenes). No puede crear, actualizar ni eliminar ningún elemento.

---

## 5. Capturas de Pantalla
Las capturas de pantalla de la interfaz de usuario con diseño moderno y paleta de colores oscura se encuentran localizadas en el directorio `/screenshots` del proyecto o pueden observarse directamente durante la ejecución en el emulador:
* `01_login.png` - Pantalla de autenticación segura.
* `02_clientes.png` - Listado de clientes con scroll infinito y barra de búsqueda.
* `03_vehiculos.png` - Administración de vehículos.
* `04_ordenes.png` - Control de estado de órdenes de trabajo.
* `05_facturas.png` - KPI financiero, emisión de facturas y pasarela de pagos.

---

## 6. Explicación de las 7 Entidades Implementadas
1. **Usuarios (`usuarios`)**: Gestiona la autenticación y asignación de roles (`admin`, `mechanic`, `client`).
2. **Clientes (`clientes`)**: Registro de datos de contacto de los propietarios de vehículos (nombre, teléfono, correo, dirección).
3. **Vehículos (`vehiculos`)**: Información de vehículos vinculados a un cliente (marca, modelo, placa, año).
4. **Servicios (`servicios`)**: Catálogo de servicios disponibles en el taller con nombre, descripción y precio unitario.
5. **Órdenes de Trabajo (`ordenes`)**: Registro de mantenimiento que vincula un vehículo, un mecánico asignado, fecha de ingreso, observaciones y estados (`pendiente`, `en_proceso`, `terminado`).
6. **Facturas (`facturas`)**: Documento financiero emitido al terminar o procesar una orden de trabajo, calculando el importe total.
7. **Pagos (`pagos`)**: Registro de abonos monetarios asociados a una factura utilizando métodos de pago (efectivo, tarjeta, transferencia).

---

## 7. Listado de Pantallas
* **Login y Registro**: Formulario de entrada con guardado persistente y seguro del token JWT (`SessionManager`).
* **Órdenes**: Tab principal que muestra el listado de reparaciones filtradas por estado con diálogos interactivos de cambio de estado.
* **Facturas y Pagos**: Muestra balances KPI, lista de facturas pendientes/saldadas y registro ágil de abonos.
* **Clientes**: Módulo CRUD para dar de alta y editar propietarios.
* **Vehículos**: Listado paginado y formulario para registrar o actualizar vehículos.
* **Servicios**: Catálogo de mantenimiento (visible únicamente para administradores).

---

## 8. Ejemplos de Consumo de la API con Token
Todas las llamadas protegidas por token JWT añaden automáticamente la cabecera `Authorization: Bearer <Token>` mediante `AuthInterceptor.kt`.

### Ejemplo de Petición HTTP (`GET /api/clientes/?page=1&search=Juan`)
```http
GET /api/clientes/?page=1&search=Juan HTTP/1.1
Host: mazsorra-taller.uaeftt-ute.site
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Accept: application/json
```

### Respuesta del Servidor (JSON Pagina 1)
```json
{
  "count": 1,
  "next": null,
  "previous": null,
  "results": [
    {
      "id": 4,
      "nombre": "Juan Pérez",
      "telefono": "0998877665",
      "direccion": "Av. Central 456",
      "correo": "juan.perez@email.com"
    }
  ]
}
```

---

## 9. Instrucciones para Ejecutar la App
1. **Clonar e Importar**: Abre Android Studio e importa la carpeta `proyecto_mecanico`.
2. **Sincronizar Gradle**: Deja que Android Studio descargue las dependencias de Gradle y configure el entorno.
3. **Ejecutar**: Conecta un dispositivo físico con depuración USB activa o inicia un emulador Android (AVD) y haz clic en el botón **Run / Ejecutar (Shift + F10)**.
