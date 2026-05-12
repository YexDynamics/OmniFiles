mysqlinformation_schema-- ============================================================
-- OmniFiles — Script de Base de Datos
-- Sistema de Gestión Documental | EAM Institución Universitaria
-- ============================================================

CREATE DATABASE IF NOT EXISTS omnifiles
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE omnifiles;

-- ============================================================
-- TABLAS DE CATÁLOGO (estados y acciones — hardcodeadas en BD)
-- ============================================================

CREATE TABLE rol (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estado_documento (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Nombre clave usado en el código (CREADO, EN_REVISION, APROBADO, RECHAZADO)',
    descripcion VARCHAR(200)
);

CREATE TABLE estado_tarea (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Nombre clave usado en el código (PENDIENTE, APROBADO, RECHAZADO, CORRECCION)',
    descripcion VARCHAR(200)
);

CREATE TABLE tipo_accion (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Nombre clave usado en el código (CREACION, ACTUALIZACION, etc.)',
    descripcion VARCHAR(200)
);

-- ============================================================
-- TABLAS PRINCIPALES
-- ============================================================

CREATE TABLE usuario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    contrasena  VARCHAR(255) NOT NULL               COMMENT 'Siempre cifrado con BCrypt — nunca texto plano',
    estado      BOOLEAN      NOT NULL DEFAULT TRUE  COMMENT 'TRUE = activo, FALSE = inactivo (no puede iniciar sesión)',
    rol_id      INT          NOT NULL,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
);

CREATE TABLE tipo_documento (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL UNIQUE,
    descripcion         TEXT,
    estado              BOOLEAN      NOT NULL DEFAULT TRUE  COMMENT 'TRUE = activo (disponible para crear documentos)',
    flujo_configurado   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'TRUE = el tipo tiene al menos una tarea configurada'
);

CREATE TABLE documento (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    ruta_archivo        VARCHAR(500)                        COMMENT 'Ruta física del archivo subido al servidor',
    estado_id           INT          NOT NULL,
    fecha_creacion      DATETIME     NOT NULL,
    fecha_actualizacion DATETIME,
    eliminado           BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'TRUE = en papelera (soft delete)',
    usuario_id          INT          NOT NULL,
    tipo_documento_id   INT,
    CONSTRAINT fk_documento_estado    FOREIGN KEY (estado_id)         REFERENCES estado_documento(id),
    CONSTRAINT fk_documento_usuario   FOREIGN KEY (usuario_id)        REFERENCES usuario(id),
    CONSTRAINT fk_documento_tipo      FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id)
);

CREATE TABLE historial_documento (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    estado_id   INT      NOT NULL     COMMENT 'Estado en que quedó el documento tras la acción',
    accion_id   INT      NOT NULL     COMMENT 'Tipo de acción realizada',
    fecha_cambio DATETIME NOT NULL,
    documento_id INT     NOT NULL,
    usuario_id  INT      NOT NULL     COMMENT 'Usuario que ejecutó la acción',
    CONSTRAINT fk_historial_estado    FOREIGN KEY (estado_id)   REFERENCES estado_documento(id),
    CONSTRAINT fk_historial_accion    FOREIGN KEY (accion_id)   REFERENCES tipo_accion(id),
    CONSTRAINT fk_historial_documento FOREIGN KEY (documento_id) REFERENCES documento(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario   FOREIGN KEY (usuario_id)  REFERENCES usuario(id)
);

CREATE TABLE tarea (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    documento_id         INT          NOT NULL,
    usuario_asignado_id  INT          NOT NULL,
    estado_id            INT          NOT NULL,
    observaciones        VARCHAR(500),
    fecha_asignacion     DATETIME     NOT NULL,
    fecha_resolucion     DATETIME                COMMENT 'NULL mientras esté PENDIENTE',
    CONSTRAINT fk_tarea_documento FOREIGN KEY (documento_id)        REFERENCES documento(id),
    CONSTRAINT fk_tarea_usuario   FOREIGN KEY (usuario_asignado_id) REFERENCES usuario(id),
    CONSTRAINT fk_tarea_estado    FOREIGN KEY (estado_id)           REFERENCES estado_tarea(id)
);

-- ============================================================
-- DATOS INICIALES — Catálogos hardcodeados en BD
-- (Los nombres DEBEN coincidir con las constantes del código)
-- ============================================================

INSERT INTO rol (nombre) VALUES
    ('ADMIN'),
    ('USER');

INSERT INTO estado_documento (nombre, descripcion) VALUES
    ('CREADO',      'Documento recién creado o devuelto para correcciones'),
    ('EN_REVISION', 'Documento enviado al flujo de revisión'),
    ('APROBADO',    'Documento aprobado en todas las etapas del flujo'),
    ('RECHAZADO',   'Documento rechazado por un revisor');

INSERT INTO estado_tarea (nombre, descripcion) VALUES
    ('PENDIENTE',   'Tarea asignada, esperando acción del responsable'),
    ('APROBADO',    'El responsable aprobó el documento'),
    ('RECHAZADO',   'El responsable rechazó el documento'),
    ('CORRECCION',  'El responsable solicitó correcciones al creador');

INSERT INTO tipo_accion (nombre, descripcion) VALUES
    ('CREACION',       'Documento creado en el sistema'),
    ('ACTUALIZACION',  'Metadatos del documento modificados'),
    ('CAMBIO_ESTADO',  'Estado del documento modificado'),
    ('ELIMINACION',    'Documento enviado a la papelera o eliminado permanentemente'),
    ('RESTAURACION',   'Documento recuperado desde la papelera'),
    ('DESCARGA',       'Archivo del documento descargado');

INSERT INTO tipo_documento (nombre, descripcion, estado, flujo_configurado) VALUES
    ('Contrato',   'Contratos comerciales e institucionales', TRUE, FALSE),
    ('Informe',    'Informes internos de gestión',           TRUE, FALSE),
    ('Solicitud',  'Solicitudes formales de usuarios',       TRUE, FALSE),
    ('Circular',   'Circulares y comunicados internos',      TRUE, FALSE);

