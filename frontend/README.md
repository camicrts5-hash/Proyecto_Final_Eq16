# ENLÁSATE

Sistema web de gestión de servicios de internet que permite administrar clientes, planes de internet, contrataciones y pagos mediante una plataforma desarrollada con tecnologías frontend y backend.

## Descripción del proyecto

ENLÁSATE es una aplicación web Full Stack diseñada para facilitar la administración de una empresa proveedora de servicios de internet.

El sistema permite gestionar los planes disponibles, registrar clientes, administrar contrataciones y llevar el control de los pagos. También proporciona a los clientes un espacio donde pueden consultar la información de su servicio y conocer el estado de sus mensualidades.

## Objetivo general

Desarrollar un sistema web que permita gestionar de manera eficiente los servicios de internet, automatizando los procesos de administración de clientes, planes, contrataciones y pagos mediante una arquitectura Full Stack.

## Tecnologías utilizadas

### Frontend
- React
- Vite
- JavaScript
- HTML5
- CSS3
- React Router DOM
- Lucide React

### Backend
- Java 23
- Spring Boot 4.2.0
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JWT
- Spring Validation
- Lombok
- Maven

### Base de datos
- MySQL 9.2.0
- MySQL Connector/J
- Flyway para migraciones de base de datos

## Funcionalidades del sistema

### Administrador
- Inicio de sesión.
- Administración de usuarios.
- Gestión de clientes.
- Gestión de planes de internet.
- Administración de contrataciones.
- Consulta y administración de pagos.
- Visualización de información general del sistema.

### Empleado
- Inicio de sesión.
- Consulta de clientes.
- Gestión de información de clientes según sus permisos.
- Consulta de planes.
- Apoyo en la administración de servicios.

### Cliente
- Inicio de sesión.
- Consulta de su información personal.
- Visualización de planes de internet.
- Contratación de planes.
- Selección de método de pago.
- Consulta de su plan contratado.
- Consulta de mensualidades pendientes.
- Visualización de deuda acumulada.
- Historial de pagos realizados.
- Consulta del estado de sus pagos.

### Módulo de pagos
- Registro de pagos.
- Consulta de pagos realizados.
- Identificación de mensualidades pendientes.
- Control del estado de los pagos.
- Consulta de fechas límite de pago.

## Arquitectura del proyecto

El proyecto utiliza una arquitectura cliente-servidor dividida en dos aplicaciones principales:

- Frontend: interfaz gráfica desarrollada con React.
- Backend: API REST desarrollada con Spring Boot.
- Base de datos: almacenamiento de información mediante MySQL.

La comunicación entre frontend y backend se realiza mediante solicitudes HTTP utilizando JSON.

## Estructura del proyecto

```text
ENLASATE/
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── index.css
│   ├── package.json
│   └── vite.config.js
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/enlasate/backend/
│   │       │       ├── config/
│   │       │       ├── controller/
│   │       │       ├── dto/
│   │       │       ├── entity/
│   │       │       ├── repository/
│   │       │       ├── security/
│   │       │       └── service/
│   │       │
│   │       └── resources/
│   │           ├── db/
│   │           │   └── migration/
│   │           └── application.properties
│   │
│   └── pom.xml
│
└── README.md
```

## Base de datos

El sistema utiliza MySQL para almacenar la información de usuarios, clientes, planes, contrataciones y pagos.


