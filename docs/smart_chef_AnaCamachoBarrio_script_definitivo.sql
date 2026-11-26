-- SMART CHEF - Ana Camacho Barrio

CREATE SCHEMA IF NOT EXISTS smart_chef;

SET search_path TO smart_chef;

-- borrado de las tablas por si tuvieramos que actualizar el script, lo dejamos comentado

/*DROP TABLE IF EXISTS alergias CASCADE;
DROP TABLE IF EXISTS usuario_preferencias CASCADE;
DROP TABLE IF EXISTS coleccion_receta CASCADE;
DROP TABLE IF EXISTS ingredientes_receta CASCADE;
DROP TABLE IF EXISTS despensa CASCADE;
DROP TABLE IF EXISTS cocina CASCADE;
DROP TABLE IF EXISTS lista_compra CASCADE;
DROP TABLE IF EXISTS coleccion CASCADE;
DROP TABLE IF EXISTS foto CASCADE;
DROP TABLE IF EXISTS paso CASCADE;
DROP TABLE IF EXISTS notificacion CASCADE;
DROP TABLE IF EXISTS ingredientes CASCADE;
DROP TABLE IF EXISTS preferencias CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS receta CASCADE;*/



--primero creamos las tablas padre

-- ===========================================================================
-- SMARTCHEF | CREAR 15 TABLAS + 68 RECETAS (7 DE PRUEBA) + FILTROS
-- ===========================================================================

CREATE TABLE receta(

    id SERIAL PRIMARY KEY,

    nombre varchar(150) NOT NULL UNIQUE,

    raciones INTEGER NOT NULL CHECK (raciones > 0),

    tiempo INTEGER NOT NULL CHECK (tiempo >= 0), -- minutos enteros

    calorias INTEGER CHECK (calorias >= 0),

    descripcion varchar(300),

    -- Enum Java ERecetaDificultad: 0=FACIL, 1=MEDIO, 2=DIFICIL

    dificultad INTEGER NOT NULL CHECK (dificultad BETWEEN 0 AND 2),
    retirada BOOLEAN NOT NULL DEFAULT FALSE -- baja lógica, útil en el catálogo

);



create table usuario(

    id SERIAL PRIMARY KEY,

    nombre varchar(150) NOT NULL UNIQUE,

    -- Enum Java EUsuarioRol actual: 0=ADMIN, 1=CHEF, 2=INVITADO

    rol INTEGER NOT NULL CHECK (rol BETWEEN 0 AND 2),

    -- El invitado (rol=2) no tiene contraseña; otros roles, sí.

    contraseña varchar(255), -- en un sistema real, hash BCrypt, NUNCA texto plano

    -- Sólo ADMIN y CHEF requieren contraseña; INVITADO no

    CONSTRAINT comprobacion_contraseña_invitado CHECK (

        (rol = 2 AND contraseña IS NULL) OR (rol IN (0,1) AND contraseña IS NOT NULL)

    )

);



create table preferencias(

    id SERIAL primary key,

    -- Enum Java: 0=VEGETARIANO,1=SINGLUTEN,2=RAPIDO,3=ECONOMICO

    tipo INTEGER NOT NULL UNIQUE CHECK (tipo BETWEEN 0 AND 3)

);



create table ingredientes(

    id SERIAL primary key,

    nombre varchar(300) NOT NULL UNIQUE,

    tipo_cantidad INTEGER NOT NULL CHECK (tipo_cantidad BETWEEN 0 AND 2), -- 0 g, 1 ml, 2 uds

    calorias INTEGER CHECK (calorias >= 0),

    precio NUMERIC(10,2) NOT NULL CHECK (precio >= 0) -- EUR/100g, EUR/100ml o EUR/unidad

);





--esta tabla no está relacionada con ninguna, ya que se podrán enviar miles de notificaciones en un dia. Seguramente se pasará como noSQL



CREATE TABLE notificacion(

    id SERIAL PRIMARY KEY,

    tipo INTEGER NOT NULL CHECK (tipo BETWEEN 0 AND 3),

    texto varchar(500) NOT NULL

);





--creación tablas hijas



create table paso(

    id SERIAL PRIMARY KEY,

    descripcion varchar(300) NOT NULL,

    numero int CHECK (numero > 0),

    id_receta int NOT NULL,

    CONSTRAINT paso_id_receta FOREIGN KEY (id_receta)

        REFERENCES receta (id)

        --on delete cascade para borrar los pasos automáticamente si se elimina la receta

        -- no tiene sentido guardar estos datos si ya la receta no existe

        ON DELETE CASCADE

);



create table foto(

    id SERIAL PRIMARY KEY,

    url varchar(500) NOT NULL,

    id_receta int NOT NULL,

    CONSTRAINT foto_id_receta FOREIGN KEY (id_receta)

        REFERENCES receta (id)

        --on delete cascade para eliminar las fotos automáticamente si se elimina la receta

        -- no tiene sentido guardar la foto si la receta no existe

        ON DELETE CASCADE

);



create table coleccion(

    id SERIAL PRIMARY KEY,

    nombre varchar(150) NOT NULL,

    id_usuario int NOT NULL,

    CONSTRAINT coleccion_id_usuario FOREIGN KEY (id_usuario)

        REFERENCES usuario (id)

        --on delete cascade para eliminar las colecciones del usuario si este se da de baja

        -- no tiene sentido guardar las colecciones del usuario si este ya no está registrado en la app

        ON DELETE CASCADE

);





--creación tablas N:M



CREATE TABLE lista_compra(

    id SERIAL PRIMARY KEY,

    id_usuario INT NOT NULL,

    id_ingredientes INT NOT NULL,

    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),

    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    estado VARCHAR(10) NOT NULL DEFAULT 'ABIERTA'
        CHECK (estado IN ('ABIERTA', 'CERRADA')),

    comprado BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT lista_compra_id_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id) ON DELETE CASCADE,

    CONSTRAINT lista_compra_id_ingredientes FOREIGN KEY (id_ingredientes)
        REFERENCES ingredientes (id) ON DELETE CASCADE

);

-- RN-05: un mismo ingrediente solo puede aparecer una vez en la lista ABIERTA de cada usuario.
-- Puede volver a aparecer en listas posteriores cuando las anteriores estén CERRADAS.
CREATE UNIQUE INDEX uk_lista_abierta
    ON lista_compra (id_usuario, id_ingredientes)
    WHERE estado = 'ABIERTA';



create table cocina(

    id SERIAL primary key,

    id_usuario int NOT NULL,

    id_receta int NOT NULL,

    -- Enum Java ECocinaTipo (ORDINAL): 0=DESAYUNO, 1=ALMUERZO, 2=CENA

    tipo int CHECK (tipo BETWEEN 0 AND 2),

    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Java LocalDateTime

    CONSTRAINT cocina_id_usuario FOREIGN KEY (id_usuario)

        REFERENCES usuario (id)

        --on delete cascade para limpiar el historial si se borra el usuario

        -- no tiene sentido guardar el historial si este ya no esta registrado en la app

        ON DELETE CASCADE,

    CONSTRAINT cocina_id_receta FOREIGN KEY (id_receta)

        REFERENCES receta (id)

        --on delete cascade para limpiar el registro si se elimina la receta

        -- no tiene sentido guardar el registro de la receta si esta ya no existe en la app

        ON DELETE CASCADE

);



create table despensa(

    id SERIAL primary key,

    id_usuario int NOT NULL,

    id_ingredientes int NOT NULL,

    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad >= 0),

    caducidad DATE, -- la fecha de consumo preferente no necesita horas; Java LocalDate

    CONSTRAINT despensa_id_usuario FOREIGN KEY (id_usuario)

        REFERENCES usuario (id)

        --on delete cascade para borrar los datos de despensa al eliminar el usuario

        -- no tiene sentido guardar los datos de la despensa del usuario si este ya no está registrado en la app

        ON DELETE CASCADE,

    CONSTRAINT despensa_id_ingredientes FOREIGN KEY (id_ingredientes)

        REFERENCES ingredientes (id)

        --on delete cascade para eliminar el registro si el ingrediente deja de existir

        -- no tiene sentido guardar el registro del ingrediente si este deja de existir

        ON DELETE CASCADE

);



create table ingredientes_receta(

    id SERIAL PRIMARY KEY,

    id_ingredientes int NOT NULL,

    id_receta int NOT NULL,

    cantidad NUMERIC(10,2) NOT NULL CHECK (cantidad >= 0),

    CONSTRAINT ingredientes_receta_id_ingredientes FOREIGN KEY (id_ingredientes)

        REFERENCES ingredientes (id)

        --on delete cascade para eliminar la relación si se borra el ingrediente

        ON DELETE CASCADE,

    CONSTRAINT ingredientes_receta_id_receta FOREIGN KEY (id_receta)

        REFERENCES receta (id)

        --on delete cascade para eliminar la relación si se borra la receta

        ON DELETE CASCADE

);



create table coleccion_receta(

    id_coleccion int NOT NULL,

    id_receta int NOT NULL,

    PRIMARY KEY (id_coleccion, id_receta),

    --Se añade el prefijo de la tabla 'coleccion_receta_' para no repetir el nombre de la FK 'id_receta' ya usado en otras tablas

    CONSTRAINT coleccion_receta_id_coleccion FOREIGN KEY (id_coleccion)

        REFERENCES coleccion (id)

        --on delete cascade para eliminar el enlace si se borra la colección

        ON DELETE CASCADE,

    CONSTRAINT coleccion_receta_id_receta FOREIGN KEY (id_receta)

        REFERENCES receta (id)

        --on delete cascade para quitar la receta de la colección si se borra la receta

        ON DELETE CASCADE

);



create table usuario_preferencias(

    id_usuario int NOT NULL,

    id_preferencias int NOT NULL,

    PRIMARY KEY (id_usuario, id_preferencias),

    CONSTRAINT usuario_preferencias_id_usuario FOREIGN KEY (id_usuario)

        REFERENCES usuario (id)

        --on delete cascade para limpiar las preferencias guardadas si se borra el usuario

        ON DELETE CASCADE,

    CONSTRAINT usuario_preferencias_id_preferencias FOREIGN KEY (id_preferencias)

        REFERENCES preferencias (id)

        --on delete cascade para borrar el vinculo si se elimina el tipo de preferencia

        ON DELETE CASCADE

);





CREATE TABLE alergias (

    id_ingredientes INT NOT NULL,

    id_preferencias INT NOT NULL,

    PRIMARY KEY (id_ingredientes, id_preferencias),

    --Se añade el prefijo 'alergias' en las FK para no repetir nombres de constraints con 'id_ingredientes' o 'id_preferencias'

    CONSTRAINT alergias_id_ingredientes FOREIGN KEY (id_ingredientes)

        REFERENCES ingredientes (id)

        --on delete cascade para borrar la asociación de alérgenos si se borra el ingrediente

        ON DELETE CASCADE,

    CONSTRAINT alergias_id_preferencias FOREIGN KEY (id_preferencias)

        REFERENCES preferencias (id)

        --on delete cascade para borrar la asociación si se elimina la preferencia/alérgeno

        ON DELETE CASCADE

);



--INSERTS



-- 1. TABLAS PADRE



INSERT INTO receta (nombre, raciones, tiempo, calorias, descripcion, dificultad) VALUES

('Tortilla de espinacas y queso', 2, 10, 310, 'Tortilla rápida y saludable rellena de espinacas frescas y queso fundido.', 0),

('Ensalada de garbanzos y aguacate', 1, 5, 280, 'Ensalada fresca y nutritiva a base de garbanzos cocidos y láminas de aguacate.', 0),

('Salmón a la Plancha con Verduras', 2, 5, 420, 'Lomo de salmón dorado a la plancha acompañado de salteado de verduras de temporada.', 0);



-- rol Java: 0=ADMIN, 1=CHEF, 2=INVITADO. El enum actual NO tiene USUARIO.

INSERT INTO usuario (nombre, rol, contraseña) VALUES

('ana_chef', 1, '123456'),

('juan_perez', 1, '123456'),

('invitado_01', 2, NULL),

('admin_pruebas', 0, 'solo_pruebas');



--preferencias Java: 0=vegetariano, 1=sin gluten, 2=rápido, 3=económico

INSERT INTO preferencias (tipo) VALUES

(0),

(1),

(2);





--tipo_cantidad Java: 0=gramos, 1=mililitros, 2=unidades

INSERT INTO ingredientes (nombre, tipo_cantidad, calorias, precio) VALUES

('Huevo', 2, 70, 2),

('Espinacas', 0, 23, 1),

('Queso', 0, 110, 2),

('Garbanzos', 0, 160, 1),

('Aguacate', 2, 160, 2),

('Lomo de Salmón', 2, 200, 6);



--notificacion Java: 0=caducidad, 1=resumen semanal, 2=coleccion, 3=lista compra

INSERT INTO notificacion (tipo, texto) VALUES

(0, 'Ingrediente a punto de caducar en tu despensa.'),

(1, 'Tu resumen semanal de recetas ya está disponible.'),

(2, 'Una receta de tu colección ha sido retirada.'),

(3, 'Lista de la compra actualizada correctamente.');





-- 2. TABLAS HIJAS



INSERT INTO paso (descripcion, numero, id_receta) VALUES

('Saltear las espinacas en una sartén con una gota de aceite.', 1, 1),

('Batir los huevos, añadir las espinacas y el queso rayado.', 2, 1),

('Cuajar la tortilla por ambos lados a fuego medio durante 10 minutos.', 3, 1),

('Lavar los garbanzos y mezclarlos con el aguacate troceado.', 1, 2),

('Marcar el lomo de salmón a fuego vivo por ambos lados durante 5 minutos.', 1, 3);



INSERT INTO foto (url, id_receta) VALUES

('https://smartchef.app/img/tortilla_espinacas.jpg', 1),

('https://smartchef.app/img/ensalada_garbanzos.jpg', 2),

('https://smartchef.app/img/salmon_plancha.jpg', 3);



INSERT INTO coleccion (nombre, id_usuario) VALUES

('Cenas Saludables', 2),

('Recetas Express 5 Minutos', 1);





-- 3. TABLAS INTERMEDIAS (N:M)



INSERT INTO lista_compra
    (id_usuario, id_ingredientes, cantidad, fecha, estado, comprado)
SELECT u.id, i.id, datos.cantidad, CURRENT_TIMESTAMP, 'ABIERTA', FALSE
FROM (VALUES
    ('juan_perez', 'Aguacate', 1.00::NUMERIC),
    ('juan_perez', 'Lomo de Salmón', 1.00::NUMERIC)
) AS datos(nombre_usuario, nombre_ingrediente, cantidad)
JOIN usuario u ON u.nombre = datos.nombre_usuario
JOIN ingredientes i ON i.nombre = datos.nombre_ingrediente;



--tipo cocina (ECocinaTipo): 0=desayuno, 1=almuerzo, 2=cena

INSERT INTO cocina (id_usuario, id_receta, tipo, fecha) VALUES

(2, 1, 1, '2026-09-20 14:30:00'),

(1, 3, 2, '2026-09-25 21:00:00');



INSERT INTO despensa (id_usuario, id_ingredientes, cantidad, caducidad) VALUES

(2, 1, 6, '2026-10-15'),

(2, 4, 400, '2027-01-01');



INSERT INTO ingredientes_receta (id_ingredientes, id_receta, cantidad) VALUES

(1, 1, 2),

(2, 1, 100),

(3, 1, 50),

(4, 2, 200),

(5, 2, 1),

(6, 3, 1);



INSERT INTO coleccion_receta (id_coleccion, id_receta) VALUES

(1, 1),

(2, 2),

(2, 3);



INSERT INTO usuario_preferencias (id_usuario, id_preferencias) VALUES

(2, 1),

(2, 2);

-- 4. Lote ampliado de recetas saludables, ingredientes y relaciones.
-- 1) Completar los cuatro tipos de preferencias sin duplicarlos.
INSERT INTO preferencias (tipo)
SELECT t.tipo FROM (VALUES (0), (1), (2), (3)) AS t(tipo)
WHERE NOT EXISTS (SELECT 1 FROM preferencias p WHERE p.tipo = t.tipo);

-- 2) 87 ingredientes adicionales. A continuación se asignan precios DECIMALES realistas para pruebas (orientativos).
INSERT INTO ingredientes (nombre, tipo_cantidad, calorias, precio)
SELECT v.nombre, v.tipo_cantidad, v.calorias, v.precio
FROM (VALUES
    ('Tomate', 0, 19, 1),
    ('Pepino', 0, 15, 1),
    ('Zanahoria', 0, 41, 1),
    ('Calabacín', 0, 17, 1),
    ('Berenjena', 0, 25, 2),
    ('Pimiento rojo', 0, 31, 1),
    ('Pimiento verde', 0, 20, 1),
    ('Cebolla', 0, 40, 1),
    ('Ajo', 0, 149, 1),
    ('Brócoli', 0, 34, 2),
    ('Coliflor', 0, 25, 2),
    ('Calabaza', 0, 26, 2),
    ('Champiñones', 0, 22, 2),
    ('Setas', 0, 25, 3),
    ('Espárragos', 0, 20, 3),
    ('Lechuga', 0, 15, 1),
    ('Rúcula', 0, 25, 2),
    ('Canónigos', 0, 20, 2),
    ('Remolacha cocida', 0, 43, 2),
    ('Patata', 0, 77, 1),
    ('Boniato', 0, 86, 2),
    ('Limón', 2, 17, 1),
    ('Naranja', 2, 47, 1),
    ('Plátano', 2, 105, 1),
    ('Manzana', 2, 95, 1),
    ('Kiwi', 2, 42, 1),
    ('Arándanos', 0, 57, 3),
    ('Fresas', 0, 33, 2),
    ('Frambuesas', 0, 52, 3),
    ('Arroz integral', 0, 360, 1),
    ('Quinoa', 0, 368, 3),
    ('Lentejas cocidas', 0, 116, 1),
    ('Alubias rojas cocidas', 0, 127, 1),
    ('Alubias blancas cocidas', 0, 114, 1),
    ('Maíz dulce', 0, 86, 1),
    ('Tofu natural', 0, 76, 2),
    ('Leche semidesnatada', 1, 47, 1),
    ('Yogur natural', 0, 61, 1),
    ('Yogur griego natural', 0, 97, 2),
    ('Bebida de almendras', 1, 24, 2),
    ('Leche de coco', 1, 180, 2),
    ('Aceite de oliva', 1, 884, 1),
    ('Semillas de chía', 0, 486, 3),
    ('Semillas de lino', 0, 534, 2),
    ('Semillas de calabaza', 0, 559, 2),
    ('Nueces', 0, 654, 3),
    ('Almendras', 0, 579, 3),
    ('Pipas de girasol', 0, 584, 2),
    ('Tortillas de maíz', 2, 100, 2),
    ('Harina de garbanzo', 0, 387, 2),
    ('Tomate triturado', 0, 29, 1),
    ('Hummus', 0, 170, 2),
    ('Pechuga de pollo', 0, 165, 3),
    ('Pechuga de pavo', 0, 135, 3),
    ('Merluza', 0, 82, 4),
    ('Atún fresco', 0, 132, 4),
    ('Salmón fresco', 0, 208, 5),
    ('Bacalao', 0, 82, 4),
    ('Ternera magra', 0, 170, 5),
    ('Tamari sin gluten', 1, 60, 3),
    ('Comino', 0, 375, 1),
    ('Pimentón dulce', 0, 282, 1),
    ('Curry suave', 0, 325, 1),
    ('Orégano', 0, 265, 1),
    ('Perejil fresco', 0, 36, 1),
    ('Cilantro fresco', 0, 23, 1),
    ('Albahaca fresca', 0, 23, 1),
    ('Canela', 0, 247, 1),
    ('Sal', 0, 0, 0),
    ('Pimienta negra', 0, 251, 1),
    ('Agua', 1, 0, 0),
    ('Miel', 0, 304, 2),
    ('Dátiles', 0, 282, 2),
    ('Jengibre', 0, 80, 2),
    ('Yogur de soja', 0, 55, 2),
    ('Tahini', 0, 595, 3),
    ('Avena en copos', 0, 389, 1),
    ('Bebida de avena', 1, 45, 2),
    ('Pan integral', 0, 250, 1),
    ('Tortillas integrales', 2, 140, 2),
    ('Pasta integral', 0, 350, 2),
    ('Cuscús integral', 0, 376, 2),
    ('Bulgur', 0, 342, 2),
    ('Harina integral', 0, 340, 1),
    ('Pan rallado', 0, 395, 1),
    ('Salsa de soja', 1, 53, 2),
    ('Puerro', 0, 61, 1)
) AS v(nombre, tipo_cantidad, calorias, precio)
WHERE NOT EXISTS (SELECT 1 FROM ingredientes i WHERE i.nombre = v.nombre);


-- Precios ORIENTATIVOS para test: EUR/100 g (tipo 0), EUR/100 ml (tipo 1)
-- o EUR/unidad (tipo 2). Nunca precio por kg o por receta sin conversión.
WITH precios(nombre, euros) AS (VALUES
    ('Huevo', 0.35),
    ('Espinacas', 0.45),
    ('Queso', 1.05),
    ('Garbanzos', 0.22),
    ('Aguacate', 1.10),
    ('Lomo de Salmón', 2.20),
    ('Tomate', 0.28),
    ('Pepino', 0.25),
    ('Zanahoria', 0.17),
    ('Calabacín', 0.25),
    ('Berenjena', 0.26),
    ('Pimiento rojo', 0.39),
    ('Pimiento verde', 0.28),
    ('Cebolla', 0.19),
    ('Ajo', 0.62),
    ('Brócoli', 0.41),
    ('Coliflor', 0.31),
    ('Calabaza', 0.24),
    ('Champiñones', 0.79),
    ('Setas', 1.35),
    ('Espárragos', 1.30),
    ('Lechuga', 0.29),
    ('Rúcula', 1.10),
    ('Canónigos', 1.10),
    ('Remolacha cocida', 0.29),
    ('Patata', 0.16),
    ('Boniato', 0.28),
    ('Limón', 0.35),
    ('Naranja', 0.29),
    ('Plátano', 0.28),
    ('Manzana', 0.36),
    ('Kiwi', 0.44),
    ('Arándanos', 1.70),
    ('Fresas', 0.74),
    ('Frambuesas', 2.45),
    ('Arroz integral', 0.24),
    ('Quinoa', 0.70),
    ('Lentejas cocidas', 0.29),
    ('Alubias rojas cocidas', 0.29),
    ('Alubias blancas cocidas', 0.25),
    ('Maíz dulce', 0.48),
    ('Tofu natural', 0.78),
    ('Leche semidesnatada', 0.10),
    ('Yogur natural', 0.28),
    ('Yogur griego natural', 0.46),
    ('Bebida de almendras', 0.19),
    ('Leche de coco', 0.28),
    ('Aceite de oliva', 1.20),
    ('Semillas de chía', 1.10),
    ('Semillas de lino', 0.80),
    ('Semillas de calabaza', 0.86),
    ('Nueces', 1.20),
    ('Almendras', 0.99),
    ('Pipas de girasol', 0.47),
    ('Tortillas de maíz', 0.42),
    ('Harina de garbanzo', 0.32),
    ('Tomate triturado', 0.19),
    ('Hummus', 0.65),
    ('Pechuga de pollo', 0.82),
    ('Pechuga de pavo', 1.05),
    ('Merluza', 1.40),
    ('Atún fresco', 2.00),
    ('Salmón fresco', 2.40),
    ('Bacalao', 1.66),
    ('Ternera magra', 1.65),
    ('Tamari sin gluten', 0.90),
    ('Comino', 1.85),
    ('Pimentón dulce', 1.20),
    ('Curry suave', 1.60),
    ('Orégano', 1.40),
    ('Perejil fresco', 0.80),
    ('Cilantro fresco', 0.80),
    ('Albahaca fresca', 1.20),
    ('Canela', 1.10),
    ('Sal', 0.12),
    ('Pimienta negra', 1.60),
    ('Agua', 0.02),
    ('Miel', 0.75),
    ('Dátiles', 0.65),
    ('Jengibre', 0.90),
    ('Yogur de soja', 0.44),
    ('Tahini', 1.30),
    ('Avena en copos', 0.25),
    ('Bebida de avena', 0.18),
    ('Pan integral', 0.42),
    ('Tortillas integrales', 0.60),
    ('Pasta integral', 0.28),
    ('Cuscús integral', 0.30),
    ('Bulgur', 0.34),
    ('Harina integral', 0.19),
    ('Pan rallado', 0.30),
    ('Salsa de soja', 0.50),
    ('Puerro', 0.42)
)
UPDATE ingredientes i SET precio = p.euros
FROM precios p WHERE i.nombre = p.nombre;

-- 3) 58 recetas nuevas y variadas (tiempos 5–50 min, distintas dificultades).
INSERT INTO receta (nombre, raciones, tiempo, calorias, descripcion, dificultad) VALUES
    ('Bowl de quinoa con verduras', 2, 20, 360, 'Quinoa con verduras crujientes y un toque de limón.', 0),
    ('Tostadas integrales con aguacate y tomate', 2, 10, 260, 'Desayuno con pan integral, aguacate y tomate natural.', 0),
    ('Porridge de avena con plátano', 2, 12, 310, 'Avena cremosa con fruta y nueces.', 0),
    ('Yogur con frutos rojos y chía', 1, 5, 220, 'Bol fresco de yogur, frutos rojos y semillas de chía.', 0),
    ('Crema de calabaza y zanahoria', 4, 35, 180, 'Crema suave de verduras ideal para una cena ligera.', 0),
    ('Lentejas estofadas con verduras', 4, 50, 380, 'Plato de cuchara de legumbres y hortalizas.', 1),
    ('Arroz integral con brócoli y tofu', 3, 30, 410, 'Arroz integral con proteína vegetal y brócoli.', 1),
    ('Ensalada mediterránea de garbanzos', 2, 15, 340, 'Garbanzos con tomate, pepino y aliño de limón.', 0),
    ('Ensalada de quinoa y pepino', 2, 20, 300, 'Ensalada de quinoa con hortalizas frescas.', 0),
    ('Ensalada de lentejas con pimientos', 2, 15, 330, 'Legumbres frías con pimiento y cebolla.', 0),
    ('Wrap integral de hummus y verduras', 2, 15, 350, 'Wrap de trigo integral con hummus y vegetales.', 0),
    ('Pasta integral con tomate y espinacas', 2, 25, 400, 'Pasta integral con salsa ligera de tomate y espinacas.', 0),
    ('Cuscús integral con verduras', 2, 20, 390, 'Cuscús de trigo con verduras salteadas.', 0),
    ('Salteado de tofu con verduras y arroz', 2, 30, 430, 'Wok vegetal con tofu y arroz integral.', 2),
    ('Berenjena al horno con tomate', 2, 40, 280, 'Berenjena asada con tomate y hierbas aromáticas.', 0),
    ('Calabacines rellenos de quinoa', 4, 45, 320, 'Calabacines horneados con quinoa y champiñones.', 2),
    ('Tortilla de champiñones y espinacas', 2, 15, 270, 'Tortilla con champiñones y hojas de espinaca.', 0),
    ('Revuelto de huevo con tomate', 1, 10, 210, 'Revuelto sencillo de huevo y tomate natural.', 0),
    ('Tortitas de avena y plátano', 2, 20, 300, 'Tortitas sin azúcar añadido con avena y fruta.', 0),
    ('Batido de frutos rojos y yogur', 1, 5, 210, 'Batido con fresas, arándanos y yogur natural.', 0),
    ('Sopa de verduras con arroz', 4, 30, 190, 'Sopa ligera de verduras de temporada y arroz integral.', 0),
    ('Pisto de verduras', 4, 35, 220, 'Pisto casero con tomate, berenjena y calabacín.', 0),
    ('Curry suave de garbanzos y coco', 3, 35, 420, 'Curry suave de garbanzos con verduras y coco.', 1),
    ('Chili de alubias rojas y maíz', 4, 40, 390, 'Guiso vegetal de alubias rojas y maíz dulce.', 1),
    ('Hummus casero con verduras', 4, 15, 230, 'Crema de garbanzos con bastones de zanahoria y pepino.', 0),
    ('Tabulé de quinoa y hierbas', 3, 20, 290, 'Versión del tabulé elaborada con quinoa sin trigo.', 0),
    ('Patata asada con brócoli y yogur', 2, 40, 340, 'Patatas asadas acompañadas de brócoli y salsa de yogur.', 0),
    ('Boniato al horno con garbanzos', 2, 40, 380, 'Boniato especiado con garbanzos crujientes.', 0),
    ('Ensalada de remolacha y naranja', 2, 15, 200, 'Ensalada colorida de remolacha, naranja y rúcula.', 0),
    ('Gazpacho casero', 4, 15, 120, 'Sopa fría de tomate, pepino y pimiento.', 0),
    ('Salmorejo ligero con huevo', 3, 20, 250, 'Crema de tomate con pan y huevo cocido.', 0),
    ('Pollo al limón con verduras', 2, 35, 430, 'Pollo asado con verduras y limón.', 1),
    ('Pavo a la plancha con quinoa', 2, 25, 420, 'Pavo a la plancha con quinoa y brócoli.', 0),
    ('Merluza al horno con patata', 2, 35, 360, 'Merluza horneada con patata y limón.', 0),
    ('Salmón al papillote con espárragos', 2, 30, 410, 'Salmón al papillote con espárragos verdes.', 2),
    ('Atún a la plancha con ensalada', 2, 15, 340, 'Atún fresco acompañado de ensalada verde.', 0),
    ('Arroz integral con pollo y verduras', 3, 35, 470, 'Arroz integral salteado con pollo y pimientos.', 1),
    ('Albóndigas de pavo con tomate', 4, 45, 400, 'Albóndigas de pavo al horno con salsa de tomate.', 2),
    ('Wok de ternera y verduras', 2, 25, 430, 'Wok de ternera magra, brócoli y salsa de soja.', 2),
    ('Poke de salmón y arroz integral', 2, 25, 450, 'Cuenco de arroz con salmón cocinado y aguacate.', 2),
    ('Tacos de pollo con tortillas de maíz', 3, 25, 410, 'Tacos de pollo con verduras y maíz.', 1),
    ('Ensalada de pollo y aguacate', 2, 15, 340, 'Ensalada fresca de pollo cocinado y aguacate.', 0),
    ('Fajitas integrales de pavo', 2, 25, 450, 'Fajitas con pavo, verduras y tortilla integral.', 1),
    ('Hamburguesas caseras de lentejas', 4, 35, 340, 'Hamburguesas vegetales de lentejas y avena.', 1),
    ('Pizza saludable de base integral', 2, 45, 460, 'Pizza de masa integral con tomate y hortalizas.', 2),
    ('Guiso de alubias blancas con espinacas', 4, 50, 370, 'Guiso suave de legumbres y espinacas.', 1),
    ('Bowl de yogur, kiwi y semillas', 1, 5, 240, 'Bol de yogur con kiwi y semillas variadas.', 0),
    ('Ensalada templada de setas y garbanzos', 2, 25, 350, 'Setas salteadas sobre garbanzos y canónigos.', 0),
    ('Bacalao con tomate al horno', 2, 35, 370, 'Bacalao al horno con tomate, ajo y perejil.', 1),
    ('Avena nocturna con manzana y canela', 1, 5, 300, 'Avena remojada durante la noche con fruta.', 0),
    ('Espárragos salteados con huevo', 2, 15, 260, 'Espárragos salteados acompañados de huevo.', 0),
    ('Ensalada de bulgur con hortalizas', 3, 20, 330, 'Bulgur de trigo con hortalizas de temporada.', 0),
    ('Curry de coliflor con garbanzos', 3, 40, 380, 'Coliflor y garbanzos con curry suave.', 2),
    ('Ensalada de espinacas con fresa y nueces', 2, 10, 230, 'Ensalada de hojas verdes con fresas y nueces.', 0),
    ('Pechuga de pavo con boniato asado', 2, 40, 440, 'Pavo horneado con boniato y pimientos.', 0),
    ('Crema de coliflor y puerro', 4, 35, 170, 'Crema ligera de coliflor y puerro.', 0),
    ('Tacos vegetarianos de alubias', 3, 25, 390, 'Tortillas de maíz rellenas de alubias y verduras.', 0),
    ('Buddha bowl de arroz y verduras', 2, 30, 420, 'Bowl de arroz integral con tofu y verduras variadas.', 1)
ON CONFLICT (nombre) DO NOTHING;

-- 4) Relaciones de ingredientes con preferencias alimentarias en la tabla existente alergias.
--    tipo=0: APTO VEGETARIANO; tipo=1: APTO SIN GLUTEN.
--    tipo=2 (RAPIDO) y tipo=3 (ECONOMICO) se calculan a nivel de receta;
--    no se asignan a ingredientes.
--    Datos SOLO educativos para pruebas; no son certificaciones de alérgenos.
-- Etiquetar ingredientes con la preferencia 0 usando tus tablas existentes.
INSERT INTO alergias (id_ingredientes, id_preferencias)
SELECT i.id, p.id
FROM ingredientes i
JOIN preferencias p ON p.tipo = 0
WHERE i.nombre IN (
    'Huevo',
    'Espinacas',
    'Queso',
    'Garbanzos',
    'Aguacate',
    'Tomate',
    'Pepino',
    'Zanahoria',
    'Calabacín',
    'Berenjena',
    'Pimiento rojo',
    'Pimiento verde',
    'Cebolla',
    'Ajo',
    'Brócoli',
    'Coliflor',
    'Calabaza',
    'Champiñones',
    'Setas',
    'Espárragos',
    'Lechuga',
    'Rúcula',
    'Canónigos',
    'Remolacha cocida',
    'Patata',
    'Boniato',
    'Limón',
    'Naranja',
    'Plátano',
    'Manzana',
    'Kiwi',
    'Arándanos',
    'Fresas',
    'Frambuesas',
    'Arroz integral',
    'Quinoa',
    'Lentejas cocidas',
    'Alubias rojas cocidas',
    'Alubias blancas cocidas',
    'Maíz dulce',
    'Tofu natural',
    'Leche semidesnatada',
    'Yogur natural',
    'Yogur griego natural',
    'Bebida de almendras',
    'Leche de coco',
    'Aceite de oliva',
    'Semillas de chía',
    'Semillas de lino',
    'Semillas de calabaza',
    'Nueces',
    'Almendras',
    'Pipas de girasol',
    'Tortillas de maíz',
    'Harina de garbanzo',
    'Tomate triturado',
    'Hummus',
    'Tamari sin gluten',
    'Comino',
    'Pimentón dulce',
    'Curry suave',
    'Orégano',
    'Perejil fresco',
    'Cilantro fresco',
    'Albahaca fresca',
    'Canela',
    'Sal',
    'Pimienta negra',
    'Agua',
    'Miel',
    'Dátiles',
    'Jengibre',
    'Yogur de soja',
    'Tahini',
    'Avena en copos',
    'Bebida de avena',
    'Pan integral',
    'Tortillas integrales',
    'Pasta integral',
    'Cuscús integral',
    'Bulgur',
    'Harina integral',
    'Pan rallado',
    'Salsa de soja',
    'Puerro'
)
ON CONFLICT (id_ingredientes, id_preferencias) DO NOTHING;

-- Etiquetar ingredientes con la preferencia 1 usando tus tablas existentes.
INSERT INTO alergias (id_ingredientes, id_preferencias)
SELECT i.id, p.id
FROM ingredientes i
JOIN preferencias p ON p.tipo = 1
WHERE i.nombre IN (
    'Huevo',
    'Espinacas',
    'Queso',
    'Garbanzos',
    'Aguacate',
    'Lomo de Salmón',
    'Tomate',
    'Pepino',
    'Zanahoria',
    'Calabacín',
    'Berenjena',
    'Pimiento rojo',
    'Pimiento verde',
    'Cebolla',
    'Ajo',
    'Brócoli',
    'Coliflor',
    'Calabaza',
    'Champiñones',
    'Setas',
    'Espárragos',
    'Lechuga',
    'Rúcula',
    'Canónigos',
    'Remolacha cocida',
    'Patata',
    'Boniato',
    'Limón',
    'Naranja',
    'Plátano',
    'Manzana',
    'Kiwi',
    'Arándanos',
    'Fresas',
    'Frambuesas',
    'Arroz integral',
    'Quinoa',
    'Lentejas cocidas',
    'Alubias rojas cocidas',
    'Alubias blancas cocidas',
    'Maíz dulce',
    'Tofu natural',
    'Leche semidesnatada',
    'Yogur natural',
    'Yogur griego natural',
    'Bebida de almendras',
    'Leche de coco',
    'Aceite de oliva',
    'Semillas de chía',
    'Semillas de lino',
    'Semillas de calabaza',
    'Nueces',
    'Almendras',
    'Pipas de girasol',
    'Tortillas de maíz',
    'Harina de garbanzo',
    'Tomate triturado',
    'Hummus',
    'Pechuga de pollo',
    'Pechuga de pavo',
    'Merluza',
    'Atún fresco',
    'Salmón fresco',
    'Bacalao',
    'Ternera magra',
    'Tamari sin gluten',
    'Comino',
    'Pimentón dulce',
    'Curry suave',
    'Orégano',
    'Perejil fresco',
    'Cilantro fresco',
    'Albahaca fresca',
    'Canela',
    'Sal',
    'Pimienta negra',
    'Agua',
    'Miel',
    'Dátiles',
    'Jengibre',
    'Yogur de soja',
    'Tahini',
    'Puerro'
)
ON CONFLICT (id_ingredientes, id_preferencias) DO NOTHING;

-- 5) 233 enlaces receta-ingrediente, con cantidad (sin repetirlos).
WITH detalle(nombre_receta, nombre_ingrediente, cantidad) AS (VALUES
    ('Bowl de quinoa con verduras', 'Quinoa', 140),
    ('Bowl de quinoa con verduras', 'Pepino', 120),
    ('Bowl de quinoa con verduras', 'Tomate', 160),
    ('Bowl de quinoa con verduras', 'Zanahoria', 100),
    ('Bowl de quinoa con verduras', 'Limón', 1),
    ('Tostadas integrales con aguacate y tomate', 'Pan integral', 120),
    ('Tostadas integrales con aguacate y tomate', 'Aguacate', 1),
    ('Tostadas integrales con aguacate y tomate', 'Tomate', 120),
    ('Porridge de avena con plátano', 'Avena en copos', 90),
    ('Porridge de avena con plátano', 'Leche semidesnatada', 300),
    ('Porridge de avena con plátano', 'Plátano', 1),
    ('Porridge de avena con plátano', 'Nueces', 20),
    ('Porridge de avena con plátano', 'Canela', 2),
    ('Yogur con frutos rojos y chía', 'Yogur natural', 180),
    ('Yogur con frutos rojos y chía', 'Arándanos', 50),
    ('Yogur con frutos rojos y chía', 'Fresas', 70),
    ('Yogur con frutos rojos y chía', 'Semillas de chía', 12),
    ('Crema de calabaza y zanahoria', 'Calabaza', 600),
    ('Crema de calabaza y zanahoria', 'Zanahoria', 250),
    ('Crema de calabaza y zanahoria', 'Cebolla', 100),
    ('Crema de calabaza y zanahoria', 'Agua', 650),
    ('Crema de calabaza y zanahoria', 'Aceite de oliva', 15),
    ('Lentejas estofadas con verduras', 'Lentejas cocidas', 650),
    ('Lentejas estofadas con verduras', 'Zanahoria', 150),
    ('Lentejas estofadas con verduras', 'Pimiento rojo', 120),
    ('Lentejas estofadas con verduras', 'Cebolla', 100),
    ('Lentejas estofadas con verduras', 'Tomate triturado', 160),
    ('Arroz integral con brócoli y tofu', 'Arroz integral', 210),
    ('Arroz integral con brócoli y tofu', 'Brócoli', 300),
    ('Arroz integral con brócoli y tofu', 'Tofu natural', 300),
    ('Arroz integral con brócoli y tofu', 'Tamari sin gluten', 20),
    ('Ensalada mediterránea de garbanzos', 'Garbanzos', 240),
    ('Ensalada mediterránea de garbanzos', 'Tomate', 160),
    ('Ensalada mediterránea de garbanzos', 'Pepino', 150),
    ('Ensalada mediterránea de garbanzos', 'Limón', 1),
    ('Ensalada mediterránea de garbanzos', 'Aceite de oliva', 10),
    ('Ensalada de quinoa y pepino', 'Quinoa', 140),
    ('Ensalada de quinoa y pepino', 'Pepino', 180),
    ('Ensalada de quinoa y pepino', 'Pimiento rojo', 100),
    ('Ensalada de quinoa y pepino', 'Perejil fresco', 12),
    ('Ensalada de lentejas con pimientos', 'Lentejas cocidas', 300),
    ('Ensalada de lentejas con pimientos', 'Pimiento verde', 90),
    ('Ensalada de lentejas con pimientos', 'Pimiento rojo', 90),
    ('Ensalada de lentejas con pimientos', 'Cebolla', 60),
    ('Wrap integral de hummus y verduras', 'Tortillas integrales', 2),
    ('Wrap integral de hummus y verduras', 'Hummus', 140),
    ('Wrap integral de hummus y verduras', 'Zanahoria', 100),
    ('Wrap integral de hummus y verduras', 'Lechuga', 80),
    ('Pasta integral con tomate y espinacas', 'Pasta integral', 170),
    ('Pasta integral con tomate y espinacas', 'Tomate triturado', 180),
    ('Pasta integral con tomate y espinacas', 'Espinacas', 120),
    ('Pasta integral con tomate y espinacas', 'Ajo', 10),
    ('Cuscús integral con verduras', 'Cuscús integral', 180),
    ('Cuscús integral con verduras', 'Calabacín', 150),
    ('Cuscús integral con verduras', 'Zanahoria', 90),
    ('Cuscús integral con verduras', 'Pimiento rojo', 90),
    ('Salteado de tofu con verduras y arroz', 'Tofu natural', 260),
    ('Salteado de tofu con verduras y arroz', 'Arroz integral', 150),
    ('Salteado de tofu con verduras y arroz', 'Brócoli', 160),
    ('Salteado de tofu con verduras y arroz', 'Pimiento verde', 120),
    ('Salteado de tofu con verduras y arroz', 'Tamari sin gluten', 15),
    ('Berenjena al horno con tomate', 'Berenjena', 400),
    ('Berenjena al horno con tomate', 'Tomate triturado', 180),
    ('Berenjena al horno con tomate', 'Queso', 60),
    ('Berenjena al horno con tomate', 'Orégano', 3),
    ('Calabacines rellenos de quinoa', 'Calabacín', 800),
    ('Calabacines rellenos de quinoa', 'Quinoa', 180),
    ('Calabacines rellenos de quinoa', 'Champiñones', 200),
    ('Calabacines rellenos de quinoa', 'Cebolla', 90),
    ('Tortilla de champiñones y espinacas', 'Huevo', 3),
    ('Tortilla de champiñones y espinacas', 'Champiñones', 180),
    ('Tortilla de champiñones y espinacas', 'Espinacas', 100),
    ('Revuelto de huevo con tomate', 'Huevo', 2),
    ('Revuelto de huevo con tomate', 'Tomate', 120),
    ('Revuelto de huevo con tomate', 'Aceite de oliva', 5),
    ('Tortitas de avena y plátano', 'Avena en copos', 100),
    ('Tortitas de avena y plátano', 'Plátano', 2),
    ('Tortitas de avena y plátano', 'Huevo', 2),
    ('Tortitas de avena y plátano', 'Canela', 2),
    ('Batido de frutos rojos y yogur', 'Fresas', 120),
    ('Batido de frutos rojos y yogur', 'Arándanos', 60),
    ('Batido de frutos rojos y yogur', 'Yogur natural', 180),
    ('Batido de frutos rojos y yogur', 'Bebida de almendras', 100),
    ('Sopa de verduras con arroz', 'Arroz integral', 140),
    ('Sopa de verduras con arroz', 'Zanahoria', 140),
    ('Sopa de verduras con arroz', 'Puerro', 150),
    ('Sopa de verduras con arroz', 'Agua', 1100),
    ('Pisto de verduras', 'Berenjena', 240),
    ('Pisto de verduras', 'Calabacín', 240),
    ('Pisto de verduras', 'Pimiento verde', 160),
    ('Pisto de verduras', 'Tomate triturado', 300),
    ('Curry suave de garbanzos y coco', 'Garbanzos', 360),
    ('Curry suave de garbanzos y coco', 'Leche de coco', 180),
    ('Curry suave de garbanzos y coco', 'Calabacín', 200),
    ('Curry suave de garbanzos y coco', 'Curry suave', 8),
    ('Chili de alubias rojas y maíz', 'Alubias rojas cocidas', 500),
    ('Chili de alubias rojas y maíz', 'Maíz dulce', 220),
    ('Chili de alubias rojas y maíz', 'Tomate triturado', 300),
    ('Chili de alubias rojas y maíz', 'Pimentón dulce', 5),
    ('Hummus casero con verduras', 'Garbanzos', 380),
    ('Hummus casero con verduras', 'Tahini', 35),
    ('Hummus casero con verduras', 'Limón', 1),
    ('Hummus casero con verduras', 'Zanahoria', 180),
    ('Hummus casero con verduras', 'Pepino', 200),
    ('Tabulé de quinoa y hierbas', 'Quinoa', 180),
    ('Tabulé de quinoa y hierbas', 'Tomate', 200),
    ('Tabulé de quinoa y hierbas', 'Pepino', 160),
    ('Tabulé de quinoa y hierbas', 'Perejil fresco', 25),
    ('Tabulé de quinoa y hierbas', 'Limón', 1),
    ('Patata asada con brócoli y yogur', 'Patata', 450),
    ('Patata asada con brócoli y yogur', 'Brócoli', 260),
    ('Patata asada con brócoli y yogur', 'Yogur natural', 120),
    ('Patata asada con brócoli y yogur', 'Limón', 1),
    ('Boniato al horno con garbanzos', 'Boniato', 400),
    ('Boniato al horno con garbanzos', 'Garbanzos', 240),
    ('Boniato al horno con garbanzos', 'Pimentón dulce', 4),
    ('Ensalada de remolacha y naranja', 'Remolacha cocida', 220),
    ('Ensalada de remolacha y naranja', 'Naranja', 2),
    ('Ensalada de remolacha y naranja', 'Rúcula', 70),
    ('Ensalada de remolacha y naranja', 'Nueces', 25),
    ('Gazpacho casero', 'Tomate', 700),
    ('Gazpacho casero', 'Pepino', 170),
    ('Gazpacho casero', 'Pimiento verde', 100),
    ('Gazpacho casero', 'Aceite de oliva', 25),
    ('Salmorejo ligero con huevo', 'Tomate', 550),
    ('Salmorejo ligero con huevo', 'Pan integral', 90),
    ('Salmorejo ligero con huevo', 'Huevo', 2),
    ('Salmorejo ligero con huevo', 'Aceite de oliva', 20),
    ('Pollo al limón con verduras', 'Pechuga de pollo', 320),
    ('Pollo al limón con verduras', 'Calabacín', 200),
    ('Pollo al limón con verduras', 'Zanahoria', 130),
    ('Pollo al limón con verduras', 'Limón', 1),
    ('Pavo a la plancha con quinoa', 'Pechuga de pavo', 300),
    ('Pavo a la plancha con quinoa', 'Quinoa', 140),
    ('Pavo a la plancha con quinoa', 'Brócoli', 240),
    ('Merluza al horno con patata', 'Merluza', 320),
    ('Merluza al horno con patata', 'Patata', 330),
    ('Merluza al horno con patata', 'Limón', 1),
    ('Merluza al horno con patata', 'Perejil fresco', 10),
    ('Salmón al papillote con espárragos', 'Salmón fresco', 300),
    ('Salmón al papillote con espárragos', 'Espárragos', 220),
    ('Salmón al papillote con espárragos', 'Limón', 1),
    ('Atún a la plancha con ensalada', 'Atún fresco', 300),
    ('Atún a la plancha con ensalada', 'Lechuga', 120),
    ('Atún a la plancha con ensalada', 'Tomate', 180),
    ('Atún a la plancha con ensalada', 'Pepino', 100),
    ('Arroz integral con pollo y verduras', 'Arroz integral', 220),
    ('Arroz integral con pollo y verduras', 'Pechuga de pollo', 300),
    ('Arroz integral con pollo y verduras', 'Pimiento rojo', 140),
    ('Arroz integral con pollo y verduras', 'Zanahoria', 110),
    ('Albóndigas de pavo con tomate', 'Pechuga de pavo', 520),
    ('Albóndigas de pavo con tomate', 'Huevo', 1),
    ('Albóndigas de pavo con tomate', 'Harina de garbanzo', 40),
    ('Albóndigas de pavo con tomate', 'Tomate triturado', 320),
    ('Wok de ternera y verduras', 'Ternera magra', 320),
    ('Wok de ternera y verduras', 'Brócoli', 220),
    ('Wok de ternera y verduras', 'Pimiento rojo', 140),
    ('Wok de ternera y verduras', 'Salsa de soja', 25),
    ('Poke de salmón y arroz integral', 'Salmón fresco', 260),
    ('Poke de salmón y arroz integral', 'Arroz integral', 170),
    ('Poke de salmón y arroz integral', 'Aguacate', 1),
    ('Poke de salmón y arroz integral', 'Pepino', 120),
    ('Tacos de pollo con tortillas de maíz', 'Tortillas de maíz', 6),
    ('Tacos de pollo con tortillas de maíz', 'Pechuga de pollo', 300),
    ('Tacos de pollo con tortillas de maíz', 'Tomate', 200),
    ('Tacos de pollo con tortillas de maíz', 'Lechuga', 100),
    ('Ensalada de pollo y aguacate', 'Pechuga de pollo', 240),
    ('Ensalada de pollo y aguacate', 'Aguacate', 1),
    ('Ensalada de pollo y aguacate', 'Lechuga', 130),
    ('Ensalada de pollo y aguacate', 'Tomate', 150),
    ('Fajitas integrales de pavo', 'Tortillas integrales', 4),
    ('Fajitas integrales de pavo', 'Pechuga de pavo', 270),
    ('Fajitas integrales de pavo', 'Pimiento rojo', 160),
    ('Fajitas integrales de pavo', 'Cebolla', 100),
    ('Hamburguesas caseras de lentejas', 'Lentejas cocidas', 480),
    ('Hamburguesas caseras de lentejas', 'Avena en copos', 110),
    ('Hamburguesas caseras de lentejas', 'Zanahoria', 150),
    ('Hamburguesas caseras de lentejas', 'Cebolla', 90),
    ('Pizza saludable de base integral', 'Harina integral', 220),
    ('Pizza saludable de base integral', 'Tomate triturado', 150),
    ('Pizza saludable de base integral', 'Champiñones', 150),
    ('Pizza saludable de base integral', 'Queso', 90),
    ('Pizza saludable de base integral', 'Pimiento rojo', 80),
    ('Guiso de alubias blancas con espinacas', 'Alubias blancas cocidas', 620),
    ('Guiso de alubias blancas con espinacas', 'Espinacas', 220),
    ('Guiso de alubias blancas con espinacas', 'Tomate triturado', 180),
    ('Guiso de alubias blancas con espinacas', 'Ajo', 10),
    ('Bowl de yogur, kiwi y semillas', 'Yogur natural', 200),
    ('Bowl de yogur, kiwi y semillas', 'Kiwi', 2),
    ('Bowl de yogur, kiwi y semillas', 'Semillas de calabaza', 15),
    ('Bowl de yogur, kiwi y semillas', 'Semillas de lino', 10),
    ('Ensalada templada de setas y garbanzos', 'Setas', 240),
    ('Ensalada templada de setas y garbanzos', 'Garbanzos', 250),
    ('Ensalada templada de setas y garbanzos', 'Canónigos', 90),
    ('Ensalada templada de setas y garbanzos', 'Ajo', 10),
    ('Bacalao con tomate al horno', 'Bacalao', 320),
    ('Bacalao con tomate al horno', 'Tomate triturado', 200),
    ('Bacalao con tomate al horno', 'Ajo', 8),
    ('Bacalao con tomate al horno', 'Perejil fresco', 12),
    ('Avena nocturna con manzana y canela', 'Avena en copos', 70),
    ('Avena nocturna con manzana y canela', 'Yogur natural', 140),
    ('Avena nocturna con manzana y canela', 'Manzana', 1),
    ('Avena nocturna con manzana y canela', 'Canela', 2),
    ('Espárragos salteados con huevo', 'Espárragos', 320),
    ('Espárragos salteados con huevo', 'Huevo', 3),
    ('Espárragos salteados con huevo', 'Aceite de oliva', 10),
    ('Ensalada de bulgur con hortalizas', 'Bulgur', 210),
    ('Ensalada de bulgur con hortalizas', 'Tomate', 160),
    ('Ensalada de bulgur con hortalizas', 'Pepino', 140),
    ('Ensalada de bulgur con hortalizas', 'Limón', 1),
    ('Curry de coliflor con garbanzos', 'Coliflor', 500),
    ('Curry de coliflor con garbanzos', 'Garbanzos', 350),
    ('Curry de coliflor con garbanzos', 'Leche de coco', 170),
    ('Curry de coliflor con garbanzos', 'Curry suave', 7),
    ('Ensalada de espinacas con fresa y nueces', 'Espinacas', 130),
    ('Ensalada de espinacas con fresa y nueces', 'Fresas', 150),
    ('Ensalada de espinacas con fresa y nueces', 'Nueces', 35),
    ('Ensalada de espinacas con fresa y nueces', 'Queso', 50),
    ('Pechuga de pavo con boniato asado', 'Pechuga de pavo', 320),
    ('Pechuga de pavo con boniato asado', 'Boniato', 320),
    ('Pechuga de pavo con boniato asado', 'Pimiento verde', 120),
    ('Crema de coliflor y puerro', 'Coliflor', 560),
    ('Crema de coliflor y puerro', 'Puerro', 220),
    ('Crema de coliflor y puerro', 'Patata', 160),
    ('Crema de coliflor y puerro', 'Agua', 750),
    ('Tacos vegetarianos de alubias', 'Tortillas de maíz', 6),
    ('Tacos vegetarianos de alubias', 'Alubias rojas cocidas', 360),
    ('Tacos vegetarianos de alubias', 'Pimiento verde', 120),
    ('Tacos vegetarianos de alubias', 'Tomate', 140),
    ('Buddha bowl de arroz y verduras', 'Arroz integral', 150),
    ('Buddha bowl de arroz y verduras', 'Tofu natural', 200),
    ('Buddha bowl de arroz y verduras', 'Brócoli', 180),
    ('Buddha bowl de arroz y verduras', 'Zanahoria', 130)
)
INSERT INTO ingredientes_receta (id_ingredientes, id_receta, cantidad)
SELECT i.id, r.id, d.cantidad
FROM detalle d
JOIN receta r ON r.nombre = d.nombre_receta
JOIN ingredientes i ON i.nombre = d.nombre_ingrediente
WHERE NOT EXISTS (
    SELECT 1 FROM ingredientes_receta ir
    WHERE ir.id_receta = r.id AND ir.id_ingredientes = i.id
);

-- 6) 174 pasos numerados consecutivamente para recetas nuevas.
WITH instrucciones(nombre_receta, numero, descripcion) AS (VALUES
    ('Bowl de quinoa con verduras', 1, 'Cuece la quinoa y deja que se temple.'),
    ('Bowl de quinoa con verduras', 2, 'Corta las verduras y mezcla con la quinoa.'),
    ('Bowl de quinoa con verduras', 3, 'Aliña con limón y sirve.'),
    ('Tostadas integrales con aguacate y tomate', 1, 'Tuesta el pan integral.'),
    ('Tostadas integrales con aguacate y tomate', 2, 'Machaca el aguacate y reparte sobre el pan.'),
    ('Tostadas integrales con aguacate y tomate', 3, 'Añade tomate picado y sirve.'),
    ('Porridge de avena con plátano', 1, 'Calienta la leche y añade la avena.'),
    ('Porridge de avena con plátano', 2, 'Cocina removiendo hasta conseguir textura cremosa.'),
    ('Porridge de avena con plátano', 3, 'Termina con plátano, nueces y canela.'),
    ('Yogur con frutos rojos y chía', 1, 'Lava los frutos rojos.'),
    ('Yogur con frutos rojos y chía', 2, 'Coloca el yogur en un cuenco.'),
    ('Yogur con frutos rojos y chía', 3, 'Añade la fruta y las semillas de chía.'),
    ('Crema de calabaza y zanahoria', 1, 'Trocea las verduras y rehoga brevemente la cebolla.'),
    ('Crema de calabaza y zanahoria', 2, 'Añade el agua y cocina hasta ablandar.'),
    ('Crema de calabaza y zanahoria', 3, 'Tritura hasta obtener una crema fina.'),
    ('Lentejas estofadas con verduras', 1, 'Sofríe las verduras troceadas.'),
    ('Lentejas estofadas con verduras', 2, 'Incorpora las lentejas y el tomate triturado.'),
    ('Lentejas estofadas con verduras', 3, 'Cocina a fuego suave para integrar sabores.'),
    ('Arroz integral con brócoli y tofu', 1, 'Cuece el arroz integral.'),
    ('Arroz integral con brócoli y tofu', 2, 'Saltea el tofu y el brócoli.'),
    ('Arroz integral con brócoli y tofu', 3, 'Mezcla con el arroz y añade tamari.'),
    ('Ensalada mediterránea de garbanzos', 1, 'Lava y escurre los garbanzos.'),
    ('Ensalada mediterránea de garbanzos', 2, 'Corta el tomate y el pepino.'),
    ('Ensalada mediterránea de garbanzos', 3, 'Mezcla y aliña con limón y aceite.'),
    ('Ensalada de quinoa y pepino', 1, 'Cuece la quinoa.'),
    ('Ensalada de quinoa y pepino', 2, 'Pica pepino, pimiento y perejil.'),
    ('Ensalada de quinoa y pepino', 3, 'Mezcla y deja templar antes de servir.'),
    ('Ensalada de lentejas con pimientos', 1, 'Escurre las lentejas cocidas.'),
    ('Ensalada de lentejas con pimientos', 2, 'Trocea las verduras en dados pequeños.'),
    ('Ensalada de lentejas con pimientos', 3, 'Mezcla bien y sirve fría.'),
    ('Wrap integral de hummus y verduras', 1, 'Unta cada tortilla con hummus.'),
    ('Wrap integral de hummus y verduras', 2, 'Añade zanahoria rallada y lechuga.'),
    ('Wrap integral de hummus y verduras', 3, 'Enrolla y corta los wraps.'),
    ('Pasta integral con tomate y espinacas', 1, 'Cuece la pasta integral.'),
    ('Pasta integral con tomate y espinacas', 2, 'Calienta el tomate con ajo y espinacas.'),
    ('Pasta integral con tomate y espinacas', 3, 'Mezcla la salsa con la pasta.'),
    ('Cuscús integral con verduras', 1, 'Hidrata el cuscús según las indicaciones.'),
    ('Cuscús integral con verduras', 2, 'Saltea las verduras troceadas.'),
    ('Cuscús integral con verduras', 3, 'Mezcla todo y sirve templado.'),
    ('Salteado de tofu con verduras y arroz', 1, 'Cuece el arroz por separado.'),
    ('Salteado de tofu con verduras y arroz', 2, 'Dora el tofu y saltea las verduras.'),
    ('Salteado de tofu con verduras y arroz', 3, 'Incorpora arroz y tamari sin gluten.'),
    ('Berenjena al horno con tomate', 1, 'Corta la berenjena en láminas y hornea.'),
    ('Berenjena al horno con tomate', 2, 'Cubre con tomate triturado y queso.'),
    ('Berenjena al horno con tomate', 3, 'Gratina hasta que esté dorada.'),
    ('Calabacines rellenos de quinoa', 1, 'Vacía los calabacines y reserva la pulpa.'),
    ('Calabacines rellenos de quinoa', 2, 'Saltea el relleno y mezcla con quinoa cocida.'),
    ('Calabacines rellenos de quinoa', 3, 'Rellena y hornea los calabacines.'),
    ('Tortilla de champiñones y espinacas', 1, 'Saltea los champiñones y las espinacas.'),
    ('Tortilla de champiñones y espinacas', 2, 'Bate los huevos y mezcla con las verduras.'),
    ('Tortilla de champiñones y espinacas', 3, 'Cuaja la tortilla por ambas caras.'),
    ('Revuelto de huevo con tomate', 1, 'Trocea el tomate.'),
    ('Revuelto de huevo con tomate', 2, 'Calienta el aceite y añade los huevos batidos.'),
    ('Revuelto de huevo con tomate', 3, 'Remueve suavemente y acompaña con tomate.'),
    ('Tortitas de avena y plátano', 1, 'Tritura avena, plátano y huevo.'),
    ('Tortitas de avena y plátano', 2, 'Forma tortitas pequeñas en una sartén.'),
    ('Tortitas de avena y plátano', 3, 'Cocínalas por ambos lados y sirve.'),
    ('Batido de frutos rojos y yogur', 1, 'Lava bien la fruta.'),
    ('Batido de frutos rojos y yogur', 2, 'Introduce los ingredientes en la batidora.'),
    ('Batido de frutos rojos y yogur', 3, 'Tritura hasta conseguir textura homogénea.'),
    ('Sopa de verduras con arroz', 1, 'Pica el puerro y la zanahoria.'),
    ('Sopa de verduras con arroz', 2, 'Cuece las verduras con el arroz en agua.'),
    ('Sopa de verduras con arroz', 3, 'Sirve la sopa caliente.'),
    ('Pisto de verduras', 1, 'Trocea todas las verduras.'),
    ('Pisto de verduras', 2, 'Cocina a fuego medio hasta que se ablanden.'),
    ('Pisto de verduras', 3, 'Añade tomate y deja reducir.'),
    ('Curry suave de garbanzos y coco', 1, 'Saltea el calabacín.'),
    ('Curry suave de garbanzos y coco', 2, 'Añade garbanzos, leche de coco y curry.'),
    ('Curry suave de garbanzos y coco', 3, 'Cocina hasta que la salsa espese.'),
    ('Chili de alubias rojas y maíz', 1, 'Calienta el tomate y el pimentón.'),
    ('Chili de alubias rojas y maíz', 2, 'Incorpora las alubias y el maíz.'),
    ('Chili de alubias rojas y maíz', 3, 'Cocina a fuego lento y sirve.'),
    ('Hummus casero con verduras', 1, 'Tritura garbanzos, tahini y limón.'),
    ('Hummus casero con verduras', 2, 'Corta las verduras en bastones.'),
    ('Hummus casero con verduras', 3, 'Sirve el hummus junto a las verduras.'),
    ('Tabulé de quinoa y hierbas', 1, 'Cuece y enfría la quinoa.'),
    ('Tabulé de quinoa y hierbas', 2, 'Pica tomate, pepino y perejil.'),
    ('Tabulé de quinoa y hierbas', 3, 'Mezcla y aliña con limón.'),
    ('Patata asada con brócoli y yogur', 1, 'Hornea las patatas cortadas.'),
    ('Patata asada con brócoli y yogur', 2, 'Cuece el brócoli al vapor.'),
    ('Patata asada con brócoli y yogur', 3, 'Mezcla yogur y limón y sirve como salsa.'),
    ('Boniato al horno con garbanzos', 1, 'Trocea el boniato y escurre los garbanzos.'),
    ('Boniato al horno con garbanzos', 2, 'Hornea juntos con pimentón hasta dorar.'),
    ('Boniato al horno con garbanzos', 3, 'Sirve en dos raciones.'),
    ('Ensalada de remolacha y naranja', 1, 'Corta la remolacha y pela la naranja.'),
    ('Ensalada de remolacha y naranja', 2, 'Distribuye sobre la rúcula.'),
    ('Ensalada de remolacha y naranja', 3, 'Añade nueces y sirve.'),
    ('Gazpacho casero', 1, 'Lava y trocea las verduras.'),
    ('Gazpacho casero', 2, 'Tritura con el aceite hasta emulsionar.'),
    ('Gazpacho casero', 3, 'Enfría antes de servir.'),
    ('Salmorejo ligero con huevo', 1, 'Tritura tomate y pan con aceite.'),
    ('Salmorejo ligero con huevo', 2, 'Cuece los huevos y córtalos.'),
    ('Salmorejo ligero con huevo', 3, 'Sirve el salmorejo con huevo.'),
    ('Pollo al limón con verduras', 1, 'Trocea las verduras y dispón el pollo en una bandeja.'),
    ('Pollo al limón con verduras', 2, 'Añade limón y hornea hasta cocinar el pollo.'),
    ('Pollo al limón con verduras', 3, 'Sirve con las verduras.'),
    ('Pavo a la plancha con quinoa', 1, 'Cuece la quinoa y el brócoli.'),
    ('Pavo a la plancha con quinoa', 2, 'Haz el pavo a la plancha hasta que esté bien cocinado.'),
    ('Pavo a la plancha con quinoa', 3, 'Sirve con la guarnición.'),
    ('Merluza al horno con patata', 1, 'Corta la patata fina y precocínala.'),
    ('Merluza al horno con patata', 2, 'Añade merluza y limón a la bandeja.'),
    ('Merluza al horno con patata', 3, 'Hornea hasta que el pescado esté listo.'),
    ('Salmón al papillote con espárragos', 1, 'Coloca salmón y espárragos sobre papel de horno.'),
    ('Salmón al papillote con espárragos', 2, 'Cierra el paquete y hornea.'),
    ('Salmón al papillote con espárragos', 3, 'Abre con cuidado y sirve con limón.'),
    ('Atún a la plancha con ensalada', 1, 'Prepara la ensalada con verduras frescas.'),
    ('Atún a la plancha con ensalada', 2, 'Cocina el atún a la plancha.'),
    ('Atún a la plancha con ensalada', 3, 'Sirve junto a la ensalada.'),
    ('Arroz integral con pollo y verduras', 1, 'Cuece el arroz integral.'),
    ('Arroz integral con pollo y verduras', 2, 'Saltea el pollo y las verduras.'),
    ('Arroz integral con pollo y verduras', 3, 'Mezcla el arroz cocido y reparte.'),
    ('Albóndigas de pavo con tomate', 1, 'Pica el pavo y mezcla con huevo y harina de garbanzo.'),
    ('Albóndigas de pavo con tomate', 2, 'Forma albóndigas y hornéalas.'),
    ('Albóndigas de pavo con tomate', 3, 'Añade salsa de tomate y termina la cocción.'),
    ('Wok de ternera y verduras', 1, 'Corta la ternera en tiras finas.'),
    ('Wok de ternera y verduras', 2, 'Saltea la ternera y el brócoli a fuego vivo.'),
    ('Wok de ternera y verduras', 3, 'Agrega la salsa de soja y sirve.'),
    ('Poke de salmón y arroz integral', 1, 'Cuece el arroz y cocina el salmón completamente.'),
    ('Poke de salmón y arroz integral', 2, 'Corta pepino y aguacate.'),
    ('Poke de salmón y arroz integral', 3, 'Monta el cuenco y sirve.'),
    ('Tacos de pollo con tortillas de maíz', 1, 'Cocina y desmenuza el pollo.'),
    ('Tacos de pollo con tortillas de maíz', 2, 'Corta tomate y lechuga.'),
    ('Tacos de pollo con tortillas de maíz', 3, 'Rellena y calienta las tortillas de maíz.'),
    ('Ensalada de pollo y aguacate', 1, 'Cocina el pollo y córtalo en tiras.'),
    ('Ensalada de pollo y aguacate', 2, 'Lava la lechuga y corta el tomate.'),
    ('Ensalada de pollo y aguacate', 3, 'Mezcla con aguacate en dados.'),
    ('Fajitas integrales de pavo', 1, 'Cocina el pavo y las verduras en tiras.'),
    ('Fajitas integrales de pavo', 2, 'Calienta las tortillas integrales.'),
    ('Fajitas integrales de pavo', 3, 'Rellena y sirve las fajitas.'),
    ('Hamburguesas caseras de lentejas', 1, 'Tritura parcialmente las lentejas con verduras.'),
    ('Hamburguesas caseras de lentejas', 2, 'Añade avena y forma hamburguesas.'),
    ('Hamburguesas caseras de lentejas', 3, 'Hornea hasta que estén firmes.'),
    ('Pizza saludable de base integral', 1, 'Prepara una masa fina con harina integral.'),
    ('Pizza saludable de base integral', 2, 'Extiende tomate y reparte los ingredientes.'),
    ('Pizza saludable de base integral', 3, 'Hornea hasta que la masa esté cocida.'),
    ('Guiso de alubias blancas con espinacas', 1, 'Rehoga el ajo y el tomate.'),
    ('Guiso de alubias blancas con espinacas', 2, 'Incorpora alubias y un poco de agua.'),
    ('Guiso de alubias blancas con espinacas', 3, 'Agrega espinacas al final y cuece brevemente.'),
    ('Bowl de yogur, kiwi y semillas', 1, 'Pela y corta los kiwis.'),
    ('Bowl de yogur, kiwi y semillas', 2, 'Sirve el yogur en un bol.'),
    ('Bowl de yogur, kiwi y semillas', 3, 'Termina con kiwi y semillas.'),
    ('Ensalada templada de setas y garbanzos', 1, 'Saltea las setas y el ajo.'),
    ('Ensalada templada de setas y garbanzos', 2, 'Calienta ligeramente los garbanzos.'),
    ('Ensalada templada de setas y garbanzos', 3, 'Coloca sobre canónigos y sirve templado.'),
    ('Bacalao con tomate al horno', 1, 'Coloca el bacalao sobre una base de tomate.'),
    ('Bacalao con tomate al horno', 2, 'Añade ajo y perejil.'),
    ('Bacalao con tomate al horno', 3, 'Hornea hasta que el pescado esté cocinado.'),
    ('Avena nocturna con manzana y canela', 1, 'Mezcla avena y yogur en un recipiente.'),
    ('Avena nocturna con manzana y canela', 2, 'Refrigera toda la noche.'),
    ('Avena nocturna con manzana y canela', 3, 'Añade manzana y canela al servir.'),
    ('Espárragos salteados con huevo', 1, 'Saltea los espárragos hasta que se ablanden.'),
    ('Espárragos salteados con huevo', 2, 'Añade huevos y cocina al punto deseado.'),
    ('Espárragos salteados con huevo', 3, 'Divide entre dos raciones.'),
    ('Ensalada de bulgur con hortalizas', 1, 'Cuece el bulgur y deja enfriar.'),
    ('Ensalada de bulgur con hortalizas', 2, 'Pica las hortalizas.'),
    ('Ensalada de bulgur con hortalizas', 3, 'Mezcla con limón y sirve.'),
    ('Curry de coliflor con garbanzos', 1, 'Trocea la coliflor y saltea ligeramente.'),
    ('Curry de coliflor con garbanzos', 2, 'Añade garbanzos, coco y curry.'),
    ('Curry de coliflor con garbanzos', 3, 'Cocina a fuego medio hasta ablandar.'),
    ('Ensalada de espinacas con fresa y nueces', 1, 'Lava espinacas y fresas.'),
    ('Ensalada de espinacas con fresa y nueces', 2, 'Corta la fruta y desmenuza el queso.'),
    ('Ensalada de espinacas con fresa y nueces', 3, 'Mezcla y termina con nueces.'),
    ('Pechuga de pavo con boniato asado', 1, 'Corta el boniato y el pimiento en tiras.'),
    ('Pechuga de pavo con boniato asado', 2, 'Hornea las verduras.'),
    ('Pechuga de pavo con boniato asado', 3, 'Cocina el pavo por completo y sirve.'),
    ('Crema de coliflor y puerro', 1, 'Pocha el puerro con un poco de agua.'),
    ('Crema de coliflor y puerro', 2, 'Añade coliflor y patata y cuece.'),
    ('Crema de coliflor y puerro', 3, 'Tritura y sirve caliente.'),
    ('Tacos vegetarianos de alubias', 1, 'Saltea las alubias con el pimiento.'),
    ('Tacos vegetarianos de alubias', 2, 'Pica tomate y calienta las tortillas.'),
    ('Tacos vegetarianos de alubias', 3, 'Reparte el relleno y sirve.'),
    ('Buddha bowl de arroz y verduras', 1, 'Cuece el arroz y cocina el brócoli.'),
    ('Buddha bowl de arroz y verduras', 2, 'Dora el tofu en dados.'),
    ('Buddha bowl de arroz y verduras', 3, 'Sirve con zanahoria rallada y mezcla.')
)
INSERT INTO paso (descripcion, numero, id_receta)
SELECT s.descripcion, s.numero, r.id
FROM instrucciones s JOIN receta r ON r.nombre = s.nombre_receta
WHERE NOT EXISTS (SELECT 1 FROM paso p WHERE p.id_receta = r.id AND p.numero = s.numero);

-- 7) Cinco usuarios ficticios para probar perfiles, colecciones, despensa e historial.
-- EUsuarioRol: 0=ADMIN, 1=CHEF, 2=INVITADO. NO existe USUARIO en el enum actual.
-- Las cuentas de prueba registradas figuran como CHEF para evitar guardar códigos incorrectos.
-- Contraseñas ficticias en texto plano SOLO PARA DESARROLLO. No usar como sistema real de autenticación.
INSERT INTO usuario (nombre, rol, contraseña)
SELECT u.nombre, u.rol, u.contrasena FROM (VALUES
    ('maria_salud', 1, 'solo_pruebas'),
    ('lucas_fit', 1, 'solo_pruebas'),
    ('sofia_cocina', 1, 'solo_pruebas'),
    ('pablo_activo', 1, 'solo_pruebas'),
    ('clara_nutricion', 1, 'solo_pruebas')
) AS u(nombre, rol, contrasena)
WHERE NOT EXISTS (SELECT 1 FROM usuario x WHERE x.nombre = u.nombre);

-- 8) Preferencias personales en usuario_preferencias (la búsqueda deberá interpretarlas después).
WITH gustos(nombre_usuario, tipo_preferencia) AS (VALUES
    ('juan_perez', 0),
    ('juan_perez', 1),
    ('maria_salud', 0),
    ('maria_salud', 1),
    ('lucas_fit', 2),
    ('sofia_cocina', 0),
    ('sofia_cocina', 3),
    ('pablo_activo', 1),
    ('clara_nutricion', 0)
)
INSERT INTO usuario_preferencias (id_usuario, id_preferencias)
SELECT u.id, p.id FROM gustos g JOIN usuario u ON u.nombre = g.nombre_usuario
JOIN preferencias p ON p.tipo = g.tipo_preferencia
WHERE TRUE
ON CONFLICT (id_usuario, id_preferencias) DO NOTHING;

-- 9) Colecciones, con Favoritas para las cuentas de usuario y chef.
WITH grupos(nombre_usuario, nombre_coleccion) AS (VALUES
    ('ana_chef', 'Favoritas'),
    ('juan_perez', 'Favoritas'),
    ('maria_salud', 'Favoritas'),
    ('lucas_fit', 'Favoritas'),
    ('sofia_cocina', 'Favoritas'),
    ('pablo_activo', 'Favoritas'),
    ('clara_nutricion', 'Favoritas'),
    ('maria_salud', 'Cenas saludables'),
    ('lucas_fit', 'Comidas rápidas'),
    ('pablo_activo', 'Proteínas y deporte'),
    ('sofia_cocina', 'Recetas vegetarianas'),
    ('clara_nutricion', 'Preparación semanal'),
    ('juan_perez', 'Ensaladas ligeras')
)
INSERT INTO coleccion (nombre, id_usuario)
SELECT g.nombre_coleccion, u.id FROM grupos g JOIN usuario u ON u.nombre = g.nombre_usuario
WHERE NOT EXISTS (SELECT 1 FROM coleccion c WHERE c.id_usuario = u.id AND c.nombre = g.nombre_coleccion);

-- 10) 20 asociaciones receta-colección para probar popularidad y Favoritas.
WITH enlaces(nombre_usuario, nombre_coleccion, nombre_receta) AS (VALUES
    ('maria_salud', 'Favoritas', 'Ensalada mediterránea de garbanzos'),
    ('maria_salud', 'Favoritas', 'Gazpacho casero'),
    ('maria_salud', 'Cenas saludables', 'Crema de calabaza y zanahoria'),
    ('maria_salud', 'Cenas saludables', 'Tortilla de champiñones y espinacas'),
    ('lucas_fit', 'Favoritas', 'Arroz integral con brócoli y tofu'),
    ('lucas_fit', 'Comidas rápidas', 'Yogur con frutos rojos y chía'),
    ('lucas_fit', 'Comidas rápidas', 'Batido de frutos rojos y yogur'),
    ('lucas_fit', 'Comidas rápidas', 'Tostadas integrales con aguacate y tomate'),
    ('sofia_cocina', 'Favoritas', 'Calabacines rellenos de quinoa'),
    ('sofia_cocina', 'Recetas vegetarianas', 'Curry de coliflor con garbanzos'),
    ('sofia_cocina', 'Recetas vegetarianas', 'Ensalada de lentejas con pimientos'),
    ('pablo_activo', 'Favoritas', 'Pavo a la plancha con quinoa'),
    ('pablo_activo', 'Proteínas y deporte', 'Pollo al limón con verduras'),
    ('pablo_activo', 'Proteínas y deporte', 'Atún a la plancha con ensalada'),
    ('clara_nutricion', 'Favoritas', 'Tabulé de quinoa y hierbas'),
    ('clara_nutricion', 'Preparación semanal', 'Lentejas estofadas con verduras'),
    ('clara_nutricion', 'Preparación semanal', 'Guiso de alubias blancas con espinacas'),
    ('juan_perez', 'Ensaladas ligeras', 'Ensalada de quinoa y pepino'),
    ('juan_perez', 'Ensaladas ligeras', 'Ensalada de remolacha y naranja'),
    ('ana_chef', 'Favoritas', 'Bowl de quinoa con verduras')
)
INSERT INTO coleccion_receta (id_coleccion, id_receta)
SELECT c.id, r.id FROM enlaces e JOIN usuario u ON u.nombre = e.nombre_usuario
JOIN coleccion c ON c.id_usuario=u.id AND c.nombre=e.nombre_coleccion
JOIN receta r ON r.nombre = e.nombre_receta
WHERE TRUE
ON CONFLICT (id_coleccion, id_receta) DO NOTHING;

-- 11) 24 registros de comidas con fechas concretas para pruebas semanales.
WITH registros(nombre_usuario, nombre_receta, momento, fecha) AS (VALUES
    ('juan_perez', 'Bowl de quinoa con verduras', 1, '2026-10-19 14:00:00'),
    ('juan_perez', 'Gazpacho casero', 2, '2026-10-19 21:00:00'),
    ('juan_perez', 'Pavo a la plancha con quinoa', 1, '2026-10-20 14:00:00'),
    ('juan_perez', 'Yogur con frutos rojos y chía', 0, '2026-10-21 08:30:00'),
    ('juan_perez', 'Tortilla de champiñones y espinacas', 2, '2026-10-21 21:00:00'),
    ('juan_perez', 'Ensalada mediterránea de garbanzos', 1, '2026-10-22 14:00:00'),
    ('maria_salud', 'Yogur con frutos rojos y chía', 0, '2026-10-19 08:00:00'),
    ('maria_salud', 'Ensalada mediterránea de garbanzos', 1, '2026-10-19 13:30:00'),
    ('maria_salud', 'Crema de calabaza y zanahoria', 2, '2026-10-19 20:00:00'),
    ('maria_salud', 'Avena nocturna con manzana y canela', 0, '2026-10-20 08:00:00'),
    ('maria_salud', 'Ensalada de quinoa y pepino', 1, '2026-10-20 13:30:00'),
    ('maria_salud', 'Gazpacho casero', 2, '2026-10-20 21:00:00'),
    ('lucas_fit', 'Porridge de avena con plátano', 0, '2026-10-19 08:00:00'),
    ('lucas_fit', 'Arroz integral con brócoli y tofu', 1, '2026-10-19 14:30:00'),
    ('lucas_fit', 'Pavo a la plancha con quinoa', 1, '2026-10-20 14:30:00'),
    ('lucas_fit', 'Atún a la plancha con ensalada', 2, '2026-10-20 21:00:00'),
    ('sofia_cocina', 'Calabacines rellenos de quinoa', 1, '2026-10-20 14:15:00'),
    ('sofia_cocina', 'Lentejas estofadas con verduras', 2, '2026-10-20 20:45:00'),
    ('pablo_activo', 'Pollo al limón con verduras', 1, '2026-10-19 14:00:00'),
    ('pablo_activo', 'Salmón al papillote con espárragos', 2, '2026-10-19 21:00:00'),
    ('pablo_activo', 'Arroz integral con pollo y verduras', 1, '2026-10-20 14:00:00'),
    ('pablo_activo', 'Ensalada de pollo y aguacate', 2, '2026-10-20 21:00:00'),
    ('clara_nutricion', 'Guiso de alubias blancas con espinacas', 1, '2026-10-21 14:00:00'),
    ('clara_nutricion', 'Tabulé de quinoa y hierbas', 2, '2026-10-21 20:45:00')
)
INSERT INTO cocina (id_usuario, id_receta, tipo, fecha)
SELECT u.id, r.id, h.momento, h.fecha::timestamp FROM registros h
JOIN usuario u ON u.nombre = h.nombre_usuario
JOIN receta r ON r.nombre = h.nombre_receta
WHERE NOT EXISTS (SELECT 1 FROM cocina c WHERE c.id_usuario=u.id AND c.id_receta=r.id AND c.tipo=h.momento AND c.fecha=h.fecha::timestamp);

-- 12) 22 artículos de despensa; caducidades relativas a la fecha de ejecución.
WITH existencias(nombre_usuario, nombre_ingrediente, cantidad, dias_hasta_caducar) AS (VALUES
    ('juan_perez', 'Huevo', 8, 2),
    ('juan_perez', 'Tomate', 650, 4),
    ('juan_perez', 'Garbanzos', 300, 30),
    ('juan_perez', 'Aguacate', 2, 1),
    ('maria_salud', 'Zanahoria', 400, 2),
    ('maria_salud', 'Pepino', 250, 3),
    ('maria_salud', 'Quinoa', 500, 120),
    ('maria_salud', 'Espinacas', 160, 1),
    ('maria_salud', 'Yogur natural', 400, 6),
    ('lucas_fit', 'Arroz integral', 1000, 180),
    ('lucas_fit', 'Tofu natural', 350, 5),
    ('lucas_fit', 'Brócoli', 270, 3),
    ('lucas_fit', 'Pechuga de pavo', 300, 1),
    ('sofia_cocina', 'Lentejas cocidas', 600, 10),
    ('sofia_cocina', 'Calabacín', 300, 2),
    ('sofia_cocina', 'Champiñones', 180, 1),
    ('pablo_activo', 'Pechuga de pollo', 500, 2),
    ('pablo_activo', 'Patata', 900, 40),
    ('pablo_activo', 'Salmón fresco', 300, 1),
    ('clara_nutricion', 'Alubias blancas cocidas', 400, 14),
    ('clara_nutricion', 'Calabaza', 900, 5),
    ('clara_nutricion', 'Tomate triturado', 300, 10)
)
INSERT INTO despensa (id_usuario, id_ingredientes, cantidad, caducidad)
SELECT u.id, i.id, e.cantidad, CURRENT_DATE + e.dias_hasta_caducar FROM existencias e
JOIN usuario u ON u.nombre=e.nombre_usuario JOIN ingredientes i ON i.nombre=e.nombre_ingrediente
WHERE NOT EXISTS (SELECT 1 FROM despensa d WHERE d.id_usuario=u.id AND d.id_ingredientes=i.id);

-- 13) 8 ingredientes en listas de compra de prueba.
WITH compras(nombre_usuario, nombre_ingrediente, cantidad) AS (VALUES
    ('juan_perez', 'Quinoa', 140.00::NUMERIC),
    ('juan_perez', 'Brócoli', 200.00::NUMERIC),
    ('maria_salud', 'Lentejas cocidas', 250.00::NUMERIC),
    ('maria_salud', 'Tomate', 300.00::NUMERIC),
    ('lucas_fit', 'Yogur natural', 2.00::NUMERIC),
    ('lucas_fit', 'Plátano', 3.00::NUMERIC),
    ('pablo_activo', 'Arroz integral', 250.00::NUMERIC),
    ('pablo_activo', 'Pechuga de pavo', 200.00::NUMERIC)
)
INSERT INTO lista_compra
    (id_usuario, id_ingredientes, cantidad, fecha, estado, comprado)
SELECT u.id, i.id, c.cantidad, CURRENT_TIMESTAMP, 'ABIERTA', FALSE
FROM compras c
JOIN usuario u ON u.nombre=c.nombre_usuario
JOIN ingredientes i ON i.nombre=c.nombre_ingrediente
WHERE NOT EXISTS (
    SELECT 1 FROM lista_compra x
    WHERE x.id_usuario=u.id
      AND x.id_ingredientes=i.id
      AND x.estado='ABIERTA'
);

-- 13.1) Casos específicos para probar POST /api/v1/listas-compra/desde-receta.
-- lista_test_parcial: tiene parte de los ingredientes; uno está caducado y
-- pepino ya aparece en la lista abierta para comprobar que la cantidad se acumula.
-- lista_test_completa: tiene existencias suficientes de todos los ingredientes.
INSERT INTO usuario (nombre, rol, contraseña)
SELECT datos.nombre, 1, 'solo_pruebas'
FROM (VALUES
    ('lista_test_parcial'),
    ('lista_test_completa')
) AS datos(nombre)
WHERE NOT EXISTS (
    SELECT 1 FROM usuario u WHERE u.nombre = datos.nombre
);

WITH existencias(nombre_usuario, nombre_ingrediente, cantidad, dias_hasta_caducar) AS (VALUES
    -- Ensalada mediterránea de garbanzos: 240 g garbanzos, 160 g tomate,
    -- 150 g pepino, 1 limón y 10 ml aceite para 2 raciones.
    ('lista_test_parcial', 'Garbanzos', 100.00::NUMERIC, 30),
    ('lista_test_parcial', 'Tomate', 500.00::NUMERIC, 30),
    ('lista_test_parcial', 'Pepino', 50.00::NUMERIC, -2),
    ('lista_test_completa', 'Garbanzos', 500.00::NUMERIC, 30),
    ('lista_test_completa', 'Tomate', 500.00::NUMERIC, 30),
    ('lista_test_completa', 'Pepino', 500.00::NUMERIC, 30),
    ('lista_test_completa', 'Limón', 5.00::NUMERIC, 30),
    ('lista_test_completa', 'Aceite de oliva', 100.00::NUMERIC, 30)
)
INSERT INTO despensa (id_usuario, id_ingredientes, cantidad, caducidad)
SELECT u.id, i.id, e.cantidad, CURRENT_DATE + e.dias_hasta_caducar
FROM existencias e
JOIN usuario u ON u.nombre = e.nombre_usuario
JOIN ingredientes i ON i.nombre = e.nombre_ingrediente
WHERE NOT EXISTS (
    SELECT 1 FROM despensa d
    WHERE d.id_usuario = u.id AND d.id_ingredientes = i.id
);

-- Ya existe una línea ABIERTA de pepino para el usuario parcial.
-- Al generar la lista desde la receta, deberá sumarse la cantidad que falta.
INSERT INTO lista_compra
    (id_usuario, id_ingredientes, cantidad, fecha, estado, comprado)
SELECT u.id, i.id, 20.00, CURRENT_TIMESTAMP, 'ABIERTA', FALSE
FROM usuario u
JOIN ingredientes i ON i.nombre = 'Pepino'
WHERE u.nombre = 'lista_test_parcial'
  AND NOT EXISTS (
      SELECT 1 FROM lista_compra lc
      WHERE lc.id_usuario = u.id
        AND lc.id_ingredientes = i.id
        AND lc.estado = 'ABIERTA'
  );



-- Dos recetas retiradas para comprobar que el GET público las excluye.
UPDATE receta SET retirada = TRUE WHERE nombre IN
    ('Berenjena al horno con tomate', 'Buddha bowl de arroz y verduras');


-- ===========================================================================
-- 14) DATOS ADICIONALES DE PRUEBA PARA LOS FILTROS Y EL GET POR ID.
-- Los 7 nombres PRUEBA se reservan para verificar resultados deterministas.
-- ===========================================================================
-- Caso sin etiqueta: debe dar vegetariano=false y sinGluten=false, aunque
-- se combine con ingredientes etiquetados. NO es un ingrediente para consumo.
INSERT INTO ingredientes (nombre, tipo_cantidad, calorias, precio)
VALUES ('Ingrediente de prueba sin etiquetas', 0, 100, 0.50)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO receta (nombre, raciones, tiempo, calorias, descripcion, dificultad) VALUES
('PRUEBA - Vegetariana sin gluten rapida', 2, 10, 200,
 'Garbanzos con tomate y pepino; apta vegetariana y sin gluten, rapida y economica.', 0),
('PRUEBA - Vegetariana con gluten', 2, 20, 320,
 'Pasta integral con tomate y espinacas; vegetariana, pero contiene trigo.', 0),
('PRUEBA - Pollo sin gluten', 2, 15, 350,
 'Pollo con lechuga y tomate; no vegetariana y apta sin gluten en estos datos.', 0),
('PRUEBA - Pavo con gluten', 2, 20, 400,
 'Pan integral con pavo y tomate; ni vegetariana ni apta sin gluten.', 1),
('PRUEBA - Salmon caro y lento', 2, 40, 500,
 'Salmon, quinoa y brocoli; no vegetariana, sin gluten, lenta y costosa.', 2),
('PRUEBA - Vegetariana cara y lenta', 1, 45, 550,
 'Quinoa, aguacate y tomate; vegetariana y sin gluten, lenta y costosa.', 2),
('PRUEBA - Ingrediente sin etiquetar', 1, 10, 160,
 'Receta tecnica: ingrediente sin marcas de aptitud, no apta para recomendar.', 0)
ON CONFLICT (nombre) DO NOTHING;

WITH detalle(nombre_receta, nombre_ingrediente, cantidad) AS (VALUES
('PRUEBA - Vegetariana sin gluten rapida', 'Garbanzos', 200),
('PRUEBA - Vegetariana sin gluten rapida', 'Tomate', 150),
('PRUEBA - Vegetariana sin gluten rapida', 'Pepino', 100),
('PRUEBA - Vegetariana con gluten', 'Pasta integral', 200),
('PRUEBA - Vegetariana con gluten', 'Tomate triturado', 200),
('PRUEBA - Vegetariana con gluten', 'Espinacas', 100),
('PRUEBA - Pollo sin gluten', 'Pechuga de pollo', 250),
('PRUEBA - Pollo sin gluten', 'Lechuga', 150),
('PRUEBA - Pollo sin gluten', 'Tomate', 100),
('PRUEBA - Pavo con gluten', 'Pan integral', 200),
('PRUEBA - Pavo con gluten', 'Pechuga de pavo', 200),
('PRUEBA - Pavo con gluten', 'Tomate', 100),
('PRUEBA - Salmon caro y lento', 'Salmón fresco', 500),
('PRUEBA - Salmon caro y lento', 'Quinoa', 160),
('PRUEBA - Salmon caro y lento', 'Brócoli', 100),
('PRUEBA - Vegetariana cara y lenta', 'Quinoa', 300),
('PRUEBA - Vegetariana cara y lenta', 'Aguacate', 3),
('PRUEBA - Vegetariana cara y lenta', 'Tomate', 100),
('PRUEBA - Ingrediente sin etiquetar', 'Ingrediente de prueba sin etiquetas', 100),
('PRUEBA - Ingrediente sin etiquetar', 'Tomate', 100)
)
INSERT INTO ingredientes_receta (id_ingredientes, id_receta, cantidad)
SELECT i.id, r.id, d.cantidad
FROM detalle d
JOIN receta r ON r.nombre=d.nombre_receta
JOIN ingredientes i ON i.nombre=d.nombre_ingrediente
WHERE NOT EXISTS (
    SELECT 1 FROM ingredientes_receta ir
    WHERE ir.id_receta = r.id AND ir.id_ingredientes = i.id
);

WITH instrucciones(nombre_receta, numero, descripcion) AS (VALUES
('PRUEBA - Vegetariana sin gluten rapida', 1, 'Lavar y cortar tomate y pepino.'),
('PRUEBA - Vegetariana sin gluten rapida', 2, 'Escurrir los garbanzos.'),
('PRUEBA - Vegetariana sin gluten rapida', 3, 'Mezclar y servir.'),
('PRUEBA - Vegetariana con gluten', 1, 'Cocer la pasta integral.'),
('PRUEBA - Vegetariana con gluten', 2, 'Calentar el tomate con las espinacas.'),
('PRUEBA - Vegetariana con gluten', 3, 'Mezclar con la pasta y servir.'),
('PRUEBA - Pollo sin gluten', 1, 'Cocinar el pollo completamente.'),
('PRUEBA - Pollo sin gluten', 2, 'Lavar y cortar la lechuga y el tomate.'),
('PRUEBA - Pollo sin gluten', 3, 'Mezclar y servir el pollo con verduras.'),
('PRUEBA - Pavo con gluten', 1, 'Cocinar el pavo completamente.'),
('PRUEBA - Pavo con gluten', 2, 'Cortar el pan y el tomate.'),
('PRUEBA - Pavo con gluten', 3, 'Montar y servir.'),
('PRUEBA - Salmon caro y lento', 1, 'Cocer la quinoa.'),
('PRUEBA - Salmon caro y lento', 2, 'Cocinar el salmon completamente.'),
('PRUEBA - Salmon caro y lento', 3, 'Cocinar el brocoli y servir con quinoa.'),
('PRUEBA - Vegetariana cara y lenta', 1, 'Cocer la quinoa.'),
('PRUEBA - Vegetariana cara y lenta', 2, 'Pelar y cortar el aguacate y el tomate.'),
('PRUEBA - Vegetariana cara y lenta', 3, 'Mezclar todo y servir.'),
('PRUEBA - Ingrediente sin etiquetar', 1, 'Preparar los ingredientes de prueba.'),
('PRUEBA - Ingrediente sin etiquetar', 2, 'Mezclar el ingrediente sin etiquetar y tomate.'),
('PRUEBA - Ingrediente sin etiquetar', 3, 'Registrar el resultado del test sin consumir.')
)
INSERT INTO paso (descripcion, numero, id_receta)
SELECT s.descripcion, s.numero, r.id
FROM instrucciones s JOIN receta r ON r.nombre=s.nombre_receta
WHERE NOT EXISTS (
    SELECT 1 FROM paso p WHERE p.id_receta=r.id AND p.numero=s.numero
);

-- ---------------------------------------------------------------------------
-- Comprobaciones automáticas. Un error revierte toda la transacción.
-- ---------------------------------------------------------------------------
DO $verificar$
BEGIN
  IF (SELECT count(*) FROM receta) <> 68 THEN
     RAISE EXCEPTION 'Se esperaban 68 recetas';
  END IF;
  IF (SELECT count(*) FROM ingredientes) <> 94 THEN
     RAISE EXCEPTION 'Se esperaban 94 ingredientes';
  END IF;
  IF (SELECT count(*) FROM preferencias) <> 4 THEN
     RAISE EXCEPTION 'Se esperaban 4 preferencias';
  END IF;
  IF (SELECT count(*) FROM ingredientes_receta) <> 259 THEN
     RAISE EXCEPTION 'Se esperaban 259 ingredientes-receta';
  END IF;
  IF (SELECT count(*) FROM paso) <> 200 THEN
     RAISE EXCEPTION 'Se esperaban 200 pasos';
  END IF;
  IF EXISTS (SELECT 1 FROM preferencias WHERE tipo NOT BETWEEN 0 AND 3) THEN
     RAISE EXCEPTION 'Existen preferencias incompatibles con el enum Java';
  END IF;
  IF (SELECT count(*) FROM receta WHERE nombre LIKE 'PRUEBA - %') <> 7 THEN
     RAISE EXCEPTION 'Se esperaban 7 recetas de prueba de filtros';
  END IF;
  IF EXISTS (
      SELECT 1 FROM alergias a JOIN ingredientes i ON a.id_ingredientes=i.id
      WHERE i.nombre='Ingrediente de prueba sin etiquetas'
  ) THEN
      RAISE EXCEPTION 'El ingrediente de control NO debe tener etiquetas';
  END IF;
END
$verificar$;

COMMIT;

-- Resultados que verás si la carga se ejecutó correctamente.
SELECT 'receta' tabla, count(*) cantidad FROM smart_chef.receta
UNION ALL SELECT 'ingredientes', count(*) FROM smart_chef.ingredientes
UNION ALL SELECT 'preferencias', count(*) FROM smart_chef.preferencias
UNION ALL SELECT 'ingredientes_receta', count(*) FROM smart_chef.ingredientes_receta
UNION ALL SELECT 'alergias', count(*) FROM smart_chef.alergias
UNION ALL SELECT 'paso', count(*) FROM smart_chef.paso
UNION ALL SELECT 'usuario', count(*) FROM smart_chef.usuario
UNION ALL SELECT 'lista_compra', count(*) FROM smart_chef.lista_compra
ORDER BY tabla;

-- Comprueba que todas las dificultades son ordinales Java: 0, 1, 2.
SELECT dificultad, count(*) cantidad FROM smart_chef.receta GROUP BY dificultad ORDER BY dificultad;

-- Etiquetas de aptitud: 0=vegetariano, 1=sin gluten.
SELECT i.nombre, p.tipo AS preferencia_java
FROM smart_chef.alergias a
JOIN smart_chef.ingredientes i ON i.id=a.id_ingredientes
JOIN smart_chef.preferencias p ON p.id=a.id_preferencias
WHERE i.nombre IN ('Queso','Aguacate','Leche semidesnatada','Garbanzos')
ORDER BY i.nombre, p.tipo;
-- Matriz de control para los 7 casos PRUEBA.
-- Los valores de vegetariano/sin_gluten salen de las etiquetas de TODOS
-- los ingredientes, no de la existencia de una etiqueta en alguno de ellos.
SELECT r.id, r.nombre,
       COALESCE(bool_and(EXISTS(
           SELECT 1 FROM smart_chef.alergias a
           JOIN smart_chef.preferencias p ON p.id=a.id_preferencias
           WHERE a.id_ingredientes=ir.id_ingredientes AND p.tipo=0
       )),FALSE) AS vegetariano,
       COALESCE(bool_and(EXISTS(
           SELECT 1 FROM smart_chef.alergias a
           JOIN smart_chef.preferencias p ON p.id=a.id_preferencias
           WHERE a.id_ingredientes=ir.id_ingredientes AND p.tipo=1
       )),FALSE) AS sin_gluten,
       r.tiempo <= 30 AS rapido,
       ROUND(SUM(ir.cantidad * i.precio / CASE WHEN i.tipo_cantidad=2 THEN 1 ELSE 100 END) / r.raciones, 2) AS coste_por_racion,
       ROUND(SUM(ir.cantidad * i.precio / CASE WHEN i.tipo_cantidad=2 THEN 1 ELSE 100 END) / r.raciones, 2) <= 2.50 AS economico
FROM smart_chef.receta r
JOIN smart_chef.ingredientes_receta ir ON ir.id_receta=r.id
JOIN smart_chef.ingredientes i ON i.id=ir.id_ingredientes
WHERE r.nombre LIKE 'PRUEBA - %'
GROUP BY r.id, r.nombre, r.tiempo, r.raciones
ORDER BY r.nombre;


SET search_path TO smart_chef;

-- ===========================================================================
-- 15) MÁS DATOS DE PRUEBA PARA LOS ENDPOINTS DE LA FASE 3 (API REST)
-- ===========================================================================
-- Solo INSERT: no se modifica ninguna tabla. Todos los datos se buscan por
-- NOMBRE (no por id), así que funcionan aunque cambien los ids autonuméricos.
-- Cada bloque usa NOT EXISTS / ON CONFLICT para poder ejecutarse dos veces
-- sin duplicar datos.
--
--   Usuario            Para qué endpoint
--   -----------------  -------------------------------------------------------
--   semana_test        15 · semana ACTUAL completa (fechas relativas a hoy)
--   coleccion_test     12 y 13 · colección vacía y receta retirada guardada
--   cierre_test        10 y 11 · lista abierta + historial de lista cerrada
--   despensa_test      8 y 9 · caducados, caducan hoy, sin caducidad, cantidad 0
--   sin_datos_test     Todos · usuario que existe pero no tiene nada (200 con [])
--   invitado_02        Rol INVITADO (2) sin contraseña
-- ===========================================================================

-- 15.1) Usuarios de prueba (rol 1 = CHEF, 2 = INVITADO; contraseñas solo de desarrollo)
INSERT INTO usuario (nombre, rol, contraseña)
SELECT d.nombre, d.rol, d.contrasena FROM (VALUES
    ('semana_test',    1, 'solo_pruebas'),
    ('coleccion_test', 1, 'solo_pruebas'),
    ('cierre_test',    1, 'solo_pruebas'),
    ('despensa_test',  1, 'solo_pruebas'),
    ('sin_datos_test', 1, 'solo_pruebas'),
    ('invitado_02',    2, NULL)
) AS d(nombre, rol, contrasena)
WHERE NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre = d.nombre);

-- Preferencias de semana_test (para probar RN-03 en el catálogo): vegetariano
INSERT INTO usuario_preferencias (id_usuario, id_preferencias)
SELECT u.id, p.id
FROM usuario u JOIN preferencias p ON p.tipo = 0
WHERE u.nombre = 'semana_test'
ON CONFLICT (id_usuario, id_preferencias) DO NOTHING;


-- 15.2) ENDPOINT 15 · Semana ACTUAL de semana_test.
-- Las fechas se calculan desde el lunes de la semana en que se ejecuta el script
-- (date_trunc('week', ...) devuelve el lunes), así siempre hay datos "de esta semana".
--   dia: 0 = lunes ... 6 = domingo · tipo: 0 desayuno, 1 almuerzo, 2 cena
--   Martes y viernes quedan SIN registros a propósito (deben salir a 0).
--   La semana ANTERIOR tiene 3 registros que NO deben aparecer en la semana actual.
WITH registros(nombre_receta, tipo, dia, hora) AS (VALUES
    -- Semana actual
    ('Yogur con frutos rojos y chía',        0, 0, '08:30'),
    ('Bowl de quinoa con verduras',          1, 0, '14:00'),
    ('Gazpacho casero',                      2, 0, '21:00'),
    ('Pollo al limón con verduras',          1, 2, '14:00'),
    ('Porridge de avena con plátano',        0, 3, '08:00'),
    ('Tortilla de champiñones y espinacas',  2, 3, '21:00'),
    ('Lentejas estofadas con verduras',      1, 5, '14:30'),
    ('Salmón al papillote con espárragos',   2, 6, '21:00'),
    -- Semana anterior (dia negativo)
    ('Crema de calabaza y zanahoria',        2, -7, '21:00'),
    ('Berenjena al horno con tomate',        1, -6, '14:00'),  -- receta RETIRADA: en el historial sigue visible
    ('Avena nocturna con manzana y canela',  0, -5, '08:00')
)
INSERT INTO cocina (id_usuario, id_receta, tipo, fecha)
SELECT u.id, r.id, x.tipo,
       date_trunc('week', CURRENT_DATE) + x.dia * INTERVAL '1 day' + x.hora::interval
FROM registros x
JOIN usuario u ON u.nombre = 'semana_test'
JOIN receta r ON r.nombre = x.nombre_receta
-- RN-07: un registro por usuario, día y momento
WHERE NOT EXISTS (
    SELECT 1 FROM cocina c
    WHERE c.id_usuario = u.id
      AND c.tipo = x.tipo
      AND c.fecha::date = (date_trunc('week', CURRENT_DATE) + x.dia * INTERVAL '1 day')::date
);


-- 15.3) ENDPOINT 16 · Más registros en OCTUBRE de 2026 (fechas fijas) para un
-- ranking claro de "más cocinadas". Junto con los registros de octubre que ya
-- había, con desde=2026-10-01&hasta=2026-10-31 el top debe empezar así:
--   1. Bowl de quinoa con verduras ........ 6
--   2. Gazpacho casero ..................... 4
--   3. Pollo al limón con verduras ......... 3
-- y la receta retirada 'Berenjena al horno con tomate' aparece con total 2.
-- Hay además un registro el 1 de NOVIEMBRE que NO debe contar en octubre.
WITH registros(nombre_usuario, nombre_receta, tipo, fecha) AS (VALUES
    ('maria_salud',     'Bowl de quinoa con verduras',   1, '2026-10-05 14:00'),
    ('lucas_fit',       'Bowl de quinoa con verduras',   1, '2026-10-06 14:00'),
    ('sofia_cocina',    'Bowl de quinoa con verduras',   1, '2026-10-07 14:00'),
    ('pablo_activo',    'Bowl de quinoa con verduras',   1, '2026-10-12 14:00'),
    ('clara_nutricion', 'Bowl de quinoa con verduras',   1, '2026-10-13 14:00'),
    ('lucas_fit',       'Gazpacho casero',               2, '2026-10-08 21:00'),
    ('sofia_cocina',    'Gazpacho casero',               2, '2026-10-14 21:00'),
    ('maria_salud',     'Pollo al limón con verduras',   2, '2026-10-26 21:00'),
    ('juan_perez',      'Pollo al limón con verduras',   1, '2026-10-27 14:00'),
    ('sofia_cocina',    'Berenjena al horno con tomate', 2, '2026-10-28 21:00'),
    ('clara_nutricion', 'Berenjena al horno con tomate', 1, '2026-10-29 14:00'),
    -- Fuera de octubre: no debe contar en el informe de octubre
    ('juan_perez',      'Bowl de quinoa con verduras',   1, '2026-11-01 14:00')
)
INSERT INTO cocina (id_usuario, id_receta, tipo, fecha)
SELECT u.id, r.id, x.tipo, x.fecha::timestamp
FROM registros x
JOIN usuario u ON u.nombre = x.nombre_usuario
JOIN receta r ON r.nombre = x.nombre_receta
WHERE NOT EXISTS (
    SELECT 1 FROM cocina c
    WHERE c.id_usuario = u.id
      AND c.tipo = x.tipo
      AND c.fecha::date = x.fecha::date
);


-- 15.4) ENDPOINTS 12 y 13 · Colecciones de coleccion_test:
--   'Favoritas' VACÍA (debe salir con recetas: [])
--   'Cenas de diario' con una receta RETIRADA (debe salir con retirada: true)
INSERT INTO coleccion (nombre, id_usuario)
SELECT g.nombre_coleccion, u.id
FROM (VALUES
    ('coleccion_test', 'Favoritas'),
    ('coleccion_test', 'Cenas de diario'),
    ('cierre_test',    'Favoritas'),
    ('despensa_test',  'Favoritas'),
    ('semana_test',    'Favoritas')
) AS g(nombre_usuario, nombre_coleccion)
JOIN usuario u ON u.nombre = g.nombre_usuario
WHERE NOT EXISTS (
    SELECT 1 FROM coleccion c WHERE c.id_usuario = u.id AND c.nombre = g.nombre_coleccion
);


-- 15.5) ENDPOINT 16 · Más recetas guardadas para un ranking claro de "más guardadas":
--   1. Bowl de quinoa con verduras ........ 4
--   2. Gazpacho casero ..................... 3
--   3. Berenjena al horno con tomate ....... 2  (retirada: true)
WITH enlaces(nombre_usuario, nombre_coleccion, nombre_receta) AS (VALUES
    ('maria_salud',     'Favoritas',            'Bowl de quinoa con verduras'),
    ('lucas_fit',       'Favoritas',            'Bowl de quinoa con verduras'),
    ('sofia_cocina',    'Favoritas',            'Bowl de quinoa con verduras'),
    ('juan_perez',      'Favoritas',            'Gazpacho casero'),
    ('clara_nutricion', 'Favoritas',            'Gazpacho casero'),
    ('coleccion_test',  'Cenas de diario',      'Berenjena al horno con tomate'),
    ('coleccion_test',  'Cenas de diario',      'Crema de calabaza y zanahoria'),
    ('sofia_cocina',    'Recetas vegetarianas', 'Berenjena al horno con tomate'),
    ('semana_test',     'Favoritas',            'Tostadas integrales con aguacate y tomate')
)
INSERT INTO coleccion_receta (id_coleccion, id_receta)
SELECT c.id, r.id
FROM enlaces e
JOIN usuario u ON u.nombre = e.nombre_usuario
JOIN coleccion c ON c.id_usuario = u.id AND c.nombre = e.nombre_coleccion
JOIN receta r ON r.nombre = e.nombre_receta
WHERE TRUE
ON CONFLICT (id_coleccion, id_receta) DO NOTHING;


-- 15.6) ENDPOINTS 8 y 9 · Despensa de despensa_test con todos los casos:
--   caducado hace 3 días, caduca hoy, caduca mañana, dentro de 10 días,
--   SIN caducidad (debe salir al final) y cantidad 0.
WITH existencias(nombre_ingrediente, cantidad, dias_hasta_caducar) AS (VALUES
    ('Yogur natural',       2.00::NUMERIC,    -3),
    ('Leche semidesnatada', 1000.00::NUMERIC,  0),
    ('Huevo',               6.00::NUMERIC,     1),
    ('Tomate',              400.00::NUMERIC,  10),
    ('Arroz integral',      1000.00::NUMERIC, NULL),
    ('Aceite de oliva',     750.00::NUMERIC,  NULL),
    ('Limón',               0.00::NUMERIC,     7)
)
INSERT INTO despensa (id_usuario, id_ingredientes, cantidad, caducidad)
SELECT u.id, i.id, e.cantidad,
       CASE WHEN e.dias_hasta_caducar IS NULL THEN NULL
            ELSE CURRENT_DATE + e.dias_hasta_caducar END
FROM existencias e
JOIN usuario u ON u.nombre = 'despensa_test'
JOIN ingredientes i ON i.nombre = e.nombre_ingrediente
WHERE NOT EXISTS (
    SELECT 1 FROM despensa d WHERE d.id_usuario = u.id AND d.id_ingredientes = i.id
);


-- 15.7) ENDPOINTS 10 y 11 · Lista de la compra de cierre_test:
--   Historial: una lista CERRADA hace 10 días (una línea comprada y otra no).
--   Actual: una lista ABIERTA con 3 ingredientes para probar el cierre.
--   En su despensa ya hay 200 g de Quinoa: al cerrar comprando la quinoa,
--   la despensa debe pasar a 200 + 150 = 350.
WITH lineas(nombre_ingrediente, cantidad, estado, comprado, dias) AS (VALUES
    -- Historial (CERRADA). El índice único parcial solo afecta a las ABIERTAS.
    ('Quinoa',     300.00::NUMERIC, 'CERRADA', TRUE,  -10),
    ('Plátano',      4.00::NUMERIC, 'CERRADA', FALSE, -10),
    -- Lista actual (ABIERTA)
    ('Quinoa',     150.00::NUMERIC, 'ABIERTA', FALSE,  -1),
    ('Brócoli',    250.00::NUMERIC, 'ABIERTA', FALSE,  -1),
    ('Plátano',      6.00::NUMERIC, 'ABIERTA', FALSE,  -1)
)
INSERT INTO lista_compra (id_usuario, id_ingredientes, cantidad, fecha, estado, comprado)
SELECT u.id, i.id, l.cantidad, CURRENT_TIMESTAMP + l.dias * INTERVAL '1 day', l.estado, l.comprado
FROM lineas l
JOIN usuario u ON u.nombre = 'cierre_test'
JOIN ingredientes i ON i.nombre = l.nombre_ingrediente
WHERE NOT EXISTS (
    SELECT 1 FROM lista_compra x
    WHERE x.id_usuario = u.id
      AND x.id_ingredientes = i.id
      AND x.estado = l.estado
);

INSERT INTO despensa (id_usuario, id_ingredientes, cantidad, caducidad)
SELECT u.id, i.id, 200.00, CURRENT_DATE + 90
FROM usuario u JOIN ingredientes i ON i.nombre = 'Quinoa'
WHERE u.nombre = 'cierre_test'
  AND NOT EXISTS (
      SELECT 1 FROM despensa d WHERE d.id_usuario = u.id AND d.id_ingredientes = i.id
  );