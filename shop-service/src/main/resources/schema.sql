-- 1. Таблица товаров (Item)
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    img_path VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL -- цена в копейках/центах (целое число)
);

-- 2. Таблица заказов (Order)
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    total_sum BIGINT NOT NULL, -- итоговая сумма заказа в копейках/центах
     session_id  VARCHAR(255)
);

-- Индекс для ускорения поиска по id заказа (обычно уже есть как PRIMARY KEY)
CREATE INDEX IF NOT EXISTS idx_orders_id ON orders(id);

-- 3. Таблица позиций заказа (OrderItem)
-- Связывает заказ (order_id) с товаром (item_id) и хранит количество и цену на момент заказа
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    count INTEGER NOT NULL,
    price BIGINT NOT NULL, -- цена за единицу на момент оформления заказа

    -- Внешние ключи с каскадным удалением
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_item
        FOREIGN KEY (item_id)
        REFERENCES items(id)
        ON DELETE RESTRICT -- чтобы не удалять товар, пока есть заказы с ним
);

-- Индексы для ускорения JOIN и поиска
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_item_id ON order_items(item_id);


-- 4. (Опционально) Индекс по полю title для быстрого поиска товаров
CREATE INDEX IF NOT EXISTS idx_items_title ON items(title);

-- 5. (Опционально) Индекс по полю description для поиска по описанию
CREATE INDEX IF NOT EXISTS idx_items_description ON items(description);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    session_id  VARCHAR(255),
    quantity    INTEGER,
    item_id     BIGINT
);
insert into items(price,description,img_path,title) values(1,'Мяч','/images/1.jpg','мяч');
insert into items(price,description,img_path,title) values(20,'Мяч1','/images/2.jpg','мяч1');
insert into items(price,description,img_path,title) values(3,'Мяч2','_','мяч2');
insert into items(price,description,img_path,title) values(40,'Мяч3','_','мяч3');

