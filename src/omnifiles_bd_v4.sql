-- ============================================================
-- OmniFiles — Script completo de base de datos
-- MariaDB 11.8.6
-- Actualizado: Mayo 2026
-- ============================================================

CREATE DATABASE IF NOT EXISTS omnifiles CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE omnifiles;

-- ── Tablas de catálogo ──────────────────────────────────────

CREATE TABLE IF NOT EXISTS rol (
                                   id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   nombre      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS estado_documento (
                                                id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                nombre VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS estado_tarea (
                                            id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            nombre VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS tipo_accion (
                                           id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
    );

-- ── Usuarios ────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS usuario (
                                       id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nombre     VARCHAR(100),
    email      VARCHAR(150) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    estado     BOOLEAN DEFAULT TRUE,
    telefono   VARCHAR(20),
    cargo      VARCHAR(100),
    rol_id     BIGINT,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
    );

-- ── Tipos documentales ──────────────────────────────────────

CREATE TABLE IF NOT EXISTS tipo_documento (
                                              id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              nombre            VARCHAR(100) NOT NULL UNIQUE,
    descripcion       VARCHAR(255),
    estado            BOOLEAN DEFAULT TRUE,
    flujo_configurado BOOLEAN DEFAULT FALSE
    );

-- ── Flujos y etapas ─────────────────────────────────────────

CREATE TABLE IF NOT EXISTS flujo (
                                     id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     tipo_documento_id BIGINT NULL,
                                     nombre            VARCHAR(150) NOT NULL,
    CONSTRAINT fk_flujo_tipo FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id)
    );

CREATE TABLE IF NOT EXISTS etapa_flujo (
                                           id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           flujo_id   BIGINT NOT NULL,
                                           orden      INT    NOT NULL,
                                           nombre     VARCHAR(100) NOT NULL,
    rol_id     BIGINT,
    usuario_id BIGINT,
    CONSTRAINT fk_etapa_flujo   FOREIGN KEY (flujo_id)   REFERENCES flujo(id),
    CONSTRAINT fk_etapa_rol     FOREIGN KEY (rol_id)     REFERENCES rol(id),
    CONSTRAINT fk_etapa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    UNIQUE KEY uk_etapa_orden (flujo_id, orden)
    );

-- ── Documentos ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS documento (
                                         id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         nombre              VARCHAR(255) NOT NULL,
    ruta_archivo        VARCHAR(500),
    estado_id           BIGINT,
    eliminado           BOOLEAN DEFAULT FALSE,
    usuario_id          BIGINT,
    tipo_documento_id   BIGINT,
    flujo_id            BIGINT NULL,
    fecha_creacion      DATETIME,
    fecha_actualizacion DATETIME,
    CONSTRAINT fk_doc_estado    FOREIGN KEY (estado_id)         REFERENCES estado_documento(id),
    CONSTRAINT fk_doc_usuario   FOREIGN KEY (usuario_id)        REFERENCES usuario(id),
    CONSTRAINT fk_doc_tipo      FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id),
    CONSTRAINT fk_doc_flujo     FOREIGN KEY (flujo_id)          REFERENCES flujo(id)
    );

-- ── Historial de documentos ─────────────────────────────────

CREATE TABLE IF NOT EXISTS historial_documento (
                                                   id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   documento_id  BIGINT,
                                                   usuario_id    BIGINT,
                                                   estado_id     BIGINT,
                                                   accion_id     BIGINT,
                                                   descripcion   VARCHAR(255),
    observaciones TEXT,
    fecha_cambio  DATETIME,
    CONSTRAINT fk_hist_doc     FOREIGN KEY (documento_id) REFERENCES documento(id),
    CONSTRAINT fk_hist_usuario FOREIGN KEY (usuario_id)   REFERENCES usuario(id),
    CONSTRAINT fk_hist_estado  FOREIGN KEY (estado_id)    REFERENCES estado_documento(id),
    CONSTRAINT fk_hist_accion  FOREIGN KEY (accion_id)    REFERENCES tipo_accion(id)
    );

-- ── Tareas ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tarea (
                                     id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     documento_id        BIGINT,
                                     etapa_flujo_id      BIGINT,
                                     usuario_asignado_id BIGINT,
                                     estado_id           BIGINT,
                                     observaciones       TEXT,
                                     fecha_asignacion    DATETIME,
                                     fecha_resolucion    DATETIME,
                                     CONSTRAINT fk_tarea_doc     FOREIGN KEY (documento_id)        REFERENCES documento(id),
    CONSTRAINT fk_tarea_etapa   FOREIGN KEY (etapa_flujo_id)      REFERENCES etapa_flujo(id),
    CONSTRAINT fk_tarea_usuario FOREIGN KEY (usuario_asignado_id) REFERENCES usuario(id),
    CONSTRAINT fk_tarea_estado  FOREIGN KEY (estado_id)           REFERENCES estado_tarea(id)
    );

-- ============================================================
-- DATOS INICIALES
-- ============================================================

-- Roles
INSERT INTO rol (nombre, descripcion) VALUES
                                          ('ADMIN',     'Administrador del sistema'),
                                          ('CREADOR',   'Crea y gestiona documentos'),
                                          ('REVISOR',   'Revisa documentos en primera etapa'),
                                          ('APROBADOR', 'Aprueba documentos en segunda etapa'),
                                          ('FIRMANTE',  'Firma y cierra documentos en etapa final');

-- Estados de documento
INSERT INTO estado_documento (nombre) VALUES
                                          ('CREADO'),
                                          ('EN_REVISION'),
                                          ('APROBADO'),
                                          ('RECHAZADO');

-- Estados de tarea
INSERT INTO estado_tarea (nombre) VALUES
                                      ('PENDIENTE'),
                                      ('APROBADO'),
                                      ('RECHAZADO'),
                                      ('CORRECCION');

-- Tipos de acción
INSERT INTO tipo_accion (nombre, descripcion) VALUES
                                                  ('CREACION',              'Documento creado en el sistema'),
                                                  ('ACTUALIZACION',         'Metadatos del documento modificados'),
                                                  ('CAMBIO_ESTADO',         'Estado del documento modificado'),
                                                  ('ELIMINACION',           'Documento enviado a papelera o eliminado permanentemente'),
                                                  ('RESTAURACION',          'Documento recuperado desde la papelera'),
                                                  ('DESCARGA',              'Archivo del documento descargado'),
                                                  ('ETAPA_APROBADA',        'Una etapa del flujo fue aprobada'),
                                                  ('ETAPA_RECHAZADA',       'Una etapa del flujo fue rechazada'),
                                                  ('FLUJO_COMPLETO',        'Todas las etapas del flujo fueron completadas'),
                                                  ('CORRECCION_SOLICITADA', 'Se solicitó corrección del documento'),
                                                  ('REENVIO_FLUJO',         'Documento reenviado al flujo tras corrección');

-- Tipos documentales
INSERT INTO tipo_documento (nombre, descripcion, estado, flujo_configurado) VALUES
                                                                                ('Recibo de Pago',     'Recibos y comprobantes de pago',             TRUE, TRUE),
                                                                                ('Contrato',           'Contratos comerciales e institucionales',     TRUE, TRUE),
                                                                                ('Informe de Gestión', 'Informes internos de gestión administrativa', TRUE, TRUE),
                                                                                ('Solicitud Interna',  'Solicitudes formales entre áreas',            TRUE, TRUE),
                                                                                ('Circular',           'Circulares y comunicados institucionales',    TRUE, TRUE);

-- Usuarios (contraseña: 1234 hasheada con BCrypt)
INSERT INTO usuario (nombre, email, contrasena, estado, telefono, cargo, rol_id) VALUES
                                                                                     ('Administrador', 'admin@omnifiles.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVgUs.7Rzy', TRUE, NULL,    'Administrador del sistema', 1),
                                                                                     ('Juan',          'juan@omnifiles.com',         '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVgUs.7Rzy', TRUE, '12345', 'Analista',                  4),
                                                                                     ('Pablo',         'pablo@omnifiles.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVgUs.7Rzy', TRUE, '4321',  'Revision',                  3),
                                                                                     ('Alejandro',     'alejo@omnifiles.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVgUs.7Rzy', TRUE, '321',   'Firmante de docs',          5),
                                                                                     ('yeliel',        'lopezmarinyeliel@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVgUs.7Rzy', TRUE, '123',   'analista',                  1);

-- Flujos (uno por cada tipo documental)
INSERT INTO flujo (tipo_documento_id, nombre) VALUES
                                                  (1, 'Flujo de aprobación de recibos'),
                                                  (2, 'Flujo de aprobación de contratos'),
                                                  (3, 'Flujo de aprobación de informes'),
                                                  (4, 'Flujo de aprobación de solicitudes'),
                                                  (5, 'Flujo de aprobación de circulares');

-- Etapas de flujo — Flujo 1: Recibo de Pago
INSERT INTO etapa_flujo (flujo_id, orden, nombre, rol_id, usuario_id) VALUES
                                                                          (1, 1, 'Revisión inicial',  3, 3),
                                                                          (1, 2, 'Aprobación formal', 4, 2),
                                                                          (1, 3, 'Firma y cierre',    5, 4);

-- Etapas de flujo — Flujo 2: Contrato
INSERT INTO etapa_flujo (flujo_id, orden, nombre, rol_id, usuario_id) VALUES
                                                                          (2, 1, 'Revisión inicial',  3, 3),
                                                                          (2, 2, 'Aprobación formal', 4, 2),
                                                                          (2, 3, 'Firma y cierre',    5, 4);

-- Etapas de flujo — Flujo 3: Informe de Gestión
INSERT INTO etapa_flujo (flujo_id, orden, nombre, rol_id, usuario_id) VALUES
                                                                          (3, 1, 'Revisión inicial',  3, 3),
                                                                          (3, 2, 'Aprobación formal', 4, 2),
                                                                          (3, 3, 'Firma y cierre',    5, 4);

-- Etapas de flujo — Flujo 4: Solicitud Interna
INSERT INTO etapa_flujo (flujo_id, orden, nombre, rol_id, usuario_id) VALUES
                                                                          (4, 1, 'Revisión inicial',  3, 3),
                                                                          (4, 2, 'Aprobación formal', 4, 2),
                                                                          (4, 3, 'Firma y cierre',    5, 4);

-- Etapas de flujo — Flujo 5: Circular
INSERT INTO etapa_flujo (flujo_id, orden, nombre, rol_id, usuario_id) VALUES
                                                                          (5, 1, 'Revisión inicial',  3, 3),
                                                                          (5, 2, 'Aprobación formal', 4, 2),
                                                                          (5, 3, 'Firma y cierre',    5, 4);