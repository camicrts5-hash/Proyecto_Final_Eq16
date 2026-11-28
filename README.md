# ENLÁSATE · Sistema de gestión de ISP

Proyecto final — Equipo 16
**Materias:** Taller de Bases de Datos · Programación Web
**Profesoras:** Maricarmen · Adelina
**Integrantes:** Camila · Karla · Erendira

---

## 1. URL del sistema en producción

| Recurso | URL |
|---|---|
| Aplicación web | https://enlasate.online |
| Con www | https://www.enlasate.online |
| API | https://enlasate.online/api |

> El sitio funciona **únicamente en el VPS** (no en local). El acceso `http://` redirige solo a `https://`.

---

## 2. Usuarios de prueba

Todos los usuarios de la aplicación usan la contraseña **`Enlasate2026!`**

| Rol | Correo | Qué puede hacer |
|---|---|---|
| Administrador | `admin@enlasate.mx` | Todo: clientes, planes, pagos, usuarios, soporte, zonas, reportes PDF |
| Empleado | `karen@enlasate.mx` | Clientes, cobros, tickets, zonas (sin gestión de usuarios) |
| Empleado | `erandi@enlasate.mx` | Ídem |
| Empleado | `karla@enlasate.mx` | Ídem |
| Cliente | `camila@correo.com` | Ver su plan, pagar, historial, estado de cuenta, reportar fallas |

> Clave del administrador: `Enlasate2026!` (se restablece con la migración V12).

---

## 3. Arquitectura

```
┌──────────────────────────────────────────────────────────────┐
│  Navegador                                                    │
└───────────────────────┬──────────────────────────────────────┘
                        │ HTTPS 443 (Certbot / Let's Encrypt)
┌───────────────────────▼──────────────────────────────────────┐
│  Nginx  ·  Amazon Linux 2023  ·  EC2 t3.micro                │
│  ├─ /            → React compilado (/var/www/enlasate)       │
│  ├─ /api/        → proxy a Spring Boot :8080                 │
│  ├─ /oauth2/authorization/ → proxy (login con Google)        │
│  └─ /login/oauth2/         → proxy (callback de Google)      │
└───────────────────────┬──────────────────────────────────────┘
                        │
┌───────────────────────▼──────────────────────────────────────┐
│  Spring Boot 4.2 · Java 21 · systemd (enlasate.service)      │
│  Puerto 8080 · -Xmx256m                                       │
└───────────────────────┬──────────────────────────────────────┘
                        │ JDBC 3307
┌───────────────────────▼──────────────────────────────────────┐
│  MySQL 8.4.13 Community (instalado desde tarball)            │
│  Base: enlasate_db · usuario: enlasate                       │
│  Puerto 3307 (MariaDB sigue en 3306 para otras prácticas)    │
└───────────────────────┬──────────────────────────────────────┘
                        │ SMTP 25 local
┌───────────────────────▼──────────────────────────────────────┐
│  Postfix → Amazon SES (587) → destinatario                   │
│  SPF y DKIM publicados en Namecheap                          │
└──────────────────────────────────────────────────────────────┘
```

---

## 4. Base de datos

- **Motor:** MySQL 8.4.13 Community
- **Base:** `enlasate_db` (utf8mb4 / utf8mb4_unicode_ci)
- **Migraciones:** Flyway, **17 versiones** (V1 … V17) aplicadas automáticamente al arrancar
- **Tablas:** 17 (cliente, usuario, rol, empleado, plan, pago_total, periodo_servicio, domicilio, estado, municipio, **colonia**, soporte_tecnico, notificacion, equipo, compra_equipo, renta_equipo, auditoria_pago)

### Reglas de negocio implementadas

| Regla | Dónde |
|---|---|
| El cliente **no puede** asignarse un plan solo | `PUT /api/clientes/mi-plan` es ADMIN/EMPLEADO |
| Pago con tarjeta → `Completado` | `PagoService` |
| Transferencia → `Pendiente` hasta que un empleado confirma | `POST /api/pagos/{id}/confirmar` |
| Efectivo/Depósito → solo personal | 403 para clientes |
| Código de activación `ENL-XXXX` válido 24 h | V6, V8, V9 |
| Clientes sin cuenta web (`id_usuario` NULL) | V10 |
| Domicilio obligatorio (catálogo estado → municipio → colonia) | V17 |
| No se puede borrar un estado/municipio en uso | `ON DELETE RESTRICT` |
| Tickets sin empleado asignado permitidos | V16 |

---

## 5. Funcionalidades por rol

### Cliente
- Inicio con planes disponibles (catálogo público)
- Elegir asesor por **WhatsApp** y capturar domicilio de instalación
- Activar la cuenta con el código que le da el asesor
- Pagar en línea (tarjeta / transferencia) y ver su historial
- Estado de cuenta mes por mes
- **Reportar fallas** y seguir sus tickets

### Empleado
- Alta de clientes (con o sin cuenta web) y generación del código
- Cobros en efectivo, depósito y confirmación de transferencias
- Gestión de tickets de soporte (crear, cambiar estado, eliminar)
- Catálogo de zonas (estado / municipio / colonia)
- Editar domicilios de clientes
- Reporte de ventas en PDF

### Administrador
Todo lo del empleado, más:
- CRUD de planes
- CRUD de empleados y usuarios
- Eliminar clientes (si no tienen pagos)
- Editar su propio perfil (nombre y correo)
- Reporte de ventas en PDF

---

## 6. Inicio de sesión

| Método | Cómo |
|---|---|
| Correo y contraseña | Formulario de login |
| **Google (OAuth2)** | Botón "Continuar con Google" en login y registro |
| Recuperación de contraseña | Enlace por correo (Postfix + SES), válido 30 min |

Al entrar con Google se crea automáticamente el **usuario** y su **ficha de cliente** en estado `Pendiente`, para que pueda elegir asesor y domicilio.

---

## 7. Correo electrónico

| Elemento | Valor |
|---|---|
| Remitente | `no-responder@enlasate.online` |
| Servidor local | Postfix (solo envío, `loopback-only`) |
| Relay | `email-smtp.us-east-2.amazonaws.com:587` (Amazon SES) |
| SPF | `v=spf1 include:amazonses.com ~all` |
| DKIM | 3 registros CNAME `*.dkim.amazonses.com` (verificados) |

Flujo: `Spring Mail → Postfix (localhost:25) → SES (587) → destinatario`

> **Nota:** la cuenta de SES está en modo sandbox, por lo que solo entrega a direcciones verificadas. Para enviar a cualquier destinatario hay que solicitar el paso a producción en la consola de AWS.

---

## 8. Seguridad

- **JWT HS512** con expiración de 24 h
- Contraseñas con **BCrypt**
- Autorización por rol con `@PreAuthorize`
- **HTTPS** obligatorio, certificado Let's Encrypt con renovación automática (Certbot)
- CORS restringido a `enlasate.online` y `localhost:5173`
- Los catálogos (planes, estados, municipios, colonias) son de lectura pública
- Variables sensibles fuera del repositorio (`systemd Environment=`)

---

## 9. Despliegue (resumen)

```bash
# Backend
./mvnw clean package -DskipTests
scp target/backend-0.0.1-SNAPSHOT.jar ec2-user@18.218.44.223:/home/ec2-user/enlasate/backend.jar
sudo systemctl stop enlasate && sudo systemctl start enlasate

# Frontend
npm run build
scp -r dist/* ec2-user@18.218.44.223:/home/ec2-user/dist/
sudo rm -rf /var/www/enlasate/*
sudo cp -r /home/ec2-user/dist/* /var/www/enlasate/
sudo chmod -R 755 /var/www/enlasate && sudo chown -R nginx:nginx /var/www/enlasate
```

**Servicios del VPS:**

| Servicio | Comando |
|---|---|
| Backend | `systemctl status enlasate` |
| MySQL (3307) | `/usr/local/mysql/bin/mysqld_safe` / puerto 3307 |
| Nginx | `systemctl status nginx` |
| Postfix | `systemctl status postfix` |
| Logs del backend | `tail -f /home/ec2-user/enlasate/app.log` |

---

## 10. Estructura del repositorio

```
Proyecto_Final_Eq16/
├── backend/                 Spring Boot 4.2 + Java 21
│   └── src/main/
│       ├── java/com/enlasate/backend/
│       │   ├── controller/  endpoints REST
│       │   ├── service/     lógica de negocio
│       │   ├── repository/  acceso a datos
│       │   ├── entity/      entidades JPA
│       │   ├── dto/         objetos de entrada/salida
│       │   ├── security/    JWT, CORS, OAuth2
│       │   └── exception/   manejo global de errores
│       └── resources/db/migration/   V1 … V17
├── frontend/                React 19 + Vite 8
│   └── src/pages/           pantallas por rol
└── docs/                    diagramas y documentación
```

---

## 11. Documentación adicional

| Documento | Contenido |
|---|---|
| `docs/modelo_er_persona.png` | Modelo entidad-relación (17 entidades, PERSONA) |
| `docs/modelo_relacional.png` | Modelo relacional (16 tablas) |
| `docs/Taller_BD_Enlasate.pdf` | Documento del Taller de Bases de Datos |

---

## 12. Créditos

Desarrollado para el proyecto final de las asignaturas **Taller de Bases de Datos** y **Programación Web** del Instituto Tecnológico de Oaxaca.

- **Equipo 16**
- Despliegue en AWS EC2 (us-east-2), dominio `enlasate.online`
