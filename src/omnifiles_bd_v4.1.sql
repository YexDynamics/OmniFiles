-- ============================================================
-- OmniFiles — Script de Base de Datos v4.1
-- Sistema de Gestión Documental | EAM Institución Universitaria
-- Incluye: flujos automáticos por plantilla, múltiples roles,
-- etapas con rol y usuario responsable, trazabilidad completa
-- ============================================================

CREATE DATABASE IF NOT EXISTS omnifiles
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE omnifiles;

-- ============================================================
-- TABLAS DE CATÁLOGO
-- Leídas dinámicamente por el código — agregar valores aquí
-- no requiere cambios en el backend
-- ============================================================

CREATE TABLE rol (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estado_documento (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Clave Java: CREADO | EN_REVISION | APROBADO | RECHAZADO',
    descripcion VARCHAR(200)
);

CREATE TABLE estado_tarea (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Clave Java: PENDIENTE | APROBADO | RECHAZADO | CORRECCION',
    descripcion VARCHAR(200)
);

CREATE TABLE tipo_accion (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Clave Java: CREACION | ACTUALIZACION | CAMBIO_ESTADO | ELIMINACION | RESTAURACION | DESCARGA',
    descripcion VARCHAR(200)
);

-- ============================================================
-- TABLAS PRINCIPALES
-- ============================================================

CREATE TABLE usuario (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    contrasena  VARCHAR(255) NOT NULL               COMMENT 'BCrypt — el backend hashea automáticamente',
    estado      BOOLEAN      NOT NULL DEFAULT TRUE  COMMENT 'TRUE = activo',
    telefono    VARCHAR(20)  NULL,
    cargo       VARCHAR(100) NULL,
    rol_id      BIGINT          NOT NULL,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
);

CREATE TABLE tipo_documento (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL UNIQUE  COMMENT 'Nombre de la plantilla (ej: Recibo de pago, Contrato)',
    descripcion       TEXT,
    estado            BOOLEAN      NOT NULL DEFAULT TRUE  COMMENT 'TRUE = disponible para crear documentos',
    flujo_configurado BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'TRUE = tiene al menos una etapa definida'
);

-- Flujo de aprobación asociado a una plantilla
-- Una plantilla tiene exactamente un flujo
CREATE TABLE flujo (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tipo_documento_id BIGINT          NOT NULL UNIQUE  COMMENT 'Cada plantilla tiene un solo flujo',
    nombre            VARCHAR(100) NOT NULL          COMMENT 'Nombre descriptivo del flujo',
    CONSTRAINT fk_flujo_tipo FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id)
);

-- Etapas del flujo — cada fila es un paso que debe cumplirse en orden
-- El campo usuario_id es opcional: si viene, solo ese usuario puede resolver la tarea;
-- si es NULL, cualquier usuario con el rol indicado puede resolverla
CREATE TABLE etapa_flujo (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    flujo_id     BIGINT          NOT NULL,
    orden        INT          NOT NULL  COMMENT 'Secuencia de ejecución (1, 2, 3...)',
    nombre       VARCHAR(100) NOT NULL  COMMENT 'Nombre de la etapa (ej: Revisión, Firma, Aprobación final)',
    rol_id       BIGINT          NOT NULL  COMMENT 'Rol responsable de esta etapa',
    usuario_id   BIGINT          NULL      COMMENT 'Usuario específico (NULL = cualquiera con el rol)',
    CONSTRAINT fk_etapa_flujo    FOREIGN KEY (flujo_id)   REFERENCES flujo(id),
    CONSTRAINT fk_etapa_rol      FOREIGN KEY (rol_id)     REFERENCES rol(id),
    CONSTRAINT fk_etapa_usuario  FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    UNIQUE KEY uk_etapa_orden (flujo_id, orden)            COMMENT 'No puede haber dos etapas con el mismo orden en el mismo flujo'
);

CREATE TABLE documento (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    ruta_archivo        VARCHAR(500) NULL              COMMENT 'Ruta física del archivo subido',
    estado_id           BIGINT          NOT NULL,
    fecha_creacion      DATETIME     NOT NULL,
    fecha_actualizacion DATETIME,
    eliminado           BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'TRUE = papelera',
    usuario_id          BIGINT          NOT NULL,
    tipo_documento_id   BIGINT,
    CONSTRAINT fk_documento_estado  FOREIGN KEY (estado_id)         REFERENCES estado_documento(id),
    CONSTRAINT fk_documento_usuario FOREIGN KEY (usuario_id)        REFERENCES usuario(id),
    CONSTRAINT fk_documento_tipo    FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id)
);

-- Tarea: ahora incluye etapa_flujo_id para saber en qué paso del flujo está
-- y poder avanzar automáticamente al siguiente cuando se aprueba
CREATE TABLE tarea (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    documento_id        BIGINT          NOT NULL,
    usuario_asignado_id BIGINT          NOT NULL,
    etapa_flujo_id      BIGINT          NOT NULL  COMMENT 'Etapa del flujo a la que corresponde esta tarea',
    estado_id           BIGINT          NOT NULL,
    observaciones       VARCHAR(500),
    fecha_asignacion    DATETIME     NOT NULL,
    fecha_resolucion    DATETIME               COMMENT 'NULL mientras esté PENDIENTE',
    CONSTRAINT fk_tarea_documento FOREIGN KEY (documento_id)        REFERENCES documento(id),
    CONSTRAINT fk_tarea_usuario   FOREIGN KEY (usuario_asignado_id) REFERENCES usuario(id),
    CONSTRAINT fk_tarea_etapa     FOREIGN KEY (etapa_flujo_id)      REFERENCES etapa_flujo(id),
    CONSTRAINT fk_tarea_estado    FOREIGN KEY (estado_id)           REFERENCES estado_tarea(id)
);

CREATE TABLE historial_documento (
    id           BIGINT   AUTO_INCREMENT PRIMARY KEY,
    estado_id    BIGINT      NOT NULL    COMMENT 'Estado en que quedó el documento',
    accion_id    BIGINT      NOT NULL    COMMENT 'Tipo de acción realizada',
    descripcion  VARCHAR(300) NULL   COMMENT 'Detalle adicional (ej: etapa completada, quién aprobó)',
    fecha_cambio DATETIME NOT NULL,
    documento_id BIGINT      NOT NULL,
    usuario_id   BIGINT      NOT NULL,
    CONSTRAINT fk_historial_estado    FOREIGN KEY (estado_id)    REFERENCES estado_documento(id),
    CONSTRAINT fk_historial_accion    FOREIGN KEY (accion_id)    REFERENCES tipo_accion(id),
    CONSTRAINT fk_historial_documento FOREIGN KEY (documento_id) REFERENCES documento(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuario(id)
);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

-- Roles — agregar los que necesiten con INSERT, sin tocar el código
INSERT INTO rol (nombre) VALUES
    ('ADMIN'),       -- Administra usuarios y plantillas
    ('CREADOR'),     -- Crea y sube documentos
    ('REVISOR'),     -- Revisa documentos en primera instancia
    ('APROBADOR'),   -- Aprueba formalmente el documento
    ('FIRMANTE');    -- Firma y finaliza el documento

-- Estados del documento
INSERT INTO estado_documento (nombre, descripcion) VALUES
    ('CREADO',      'Documento recién creado o devuelto para correcciones'),
    ('EN_REVISION', 'Documento en alguna etapa del flujo de aprobación'),
    ('APROBADO',    'Documento aprobado en todas las etapas'),
    ('RECHAZADO',   'Documento rechazado — el flujo se detuvo');

-- Estados de la tarea
INSERT INTO estado_tarea (nombre, descripcion) VALUES
    ('PENDIENTE',  'Tarea asignada, esperando acción del responsable'),
    ('APROBADO',   'El responsable aprobó — avanza a la siguiente etapa'),
    ('RECHAZADO',  'El responsable rechazó — el flujo se detiene'),
    ('CORRECCION', 'Se solicitaron correcciones — vuelve al creador');

-- Tipos de acción para el historial
INSERT INTO tipo_accion (nombre, descripcion) VALUES
    ('CREACION',       'Documento creado en el sistema'),
    ('ACTUALIZACION',  'Metadatos del documento modificados'),
    ('CAMBIO_ESTADO',  'Estado del documento modificado'),
    ('ELIMINACION',    'Documento enviado a papelera o eliminado permanentemente'),
    ('RESTAURACION',   'Documento recuperado desde la papelera'),
    ('DESCARGA',       'Archivo del documento descargado'),
    ('ETAPA_APROBADA', 'Una etapa del flujo fue aprobada'),
    ('ETAPA_RECHAZADA','Una etapa del flujo fue rechazada'),
    ('FLUJO_COMPLETO', 'Todas las etapas del flujo fueron completadas');

-- Plantillas de documentos: el ADMIN las crea desde la aplicación
-- POST /api/v1/tipos-documentales

-- ============================================================
-- USUARIO ADMINISTRADOR INICIAL
-- Genera el hash en https://bcrypt-generator.com (rounds=10)
-- y reemplaza REEMPLAZAR_CON_HASH_BCRYPT antes de ejecutar
-- ============================================================
INSERT INTO usuario (nombre, email, contrasena, estado, telefono, cargo, rol_id)
VALUES (
    'Administrador',
    'admin@omnifiles.com',
    '$2a$10$KaXsDFatNiXVrp83ZW.mX.fIs0ser2tMo9O7cC8.fgjpQc7bASAUu',
    TRUE,
    NULL,
    'Administrador del sistema',
    1  -- rol ADMIN
);
