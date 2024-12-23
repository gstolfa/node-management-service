-- Crea la tabella `nodes` se non esiste
CREATE TABLE IF NOT EXISTS nodes (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,        -- Identificativo unico per il nodo
                                     name VARCHAR(255) NOT NULL UNIQUE            -- Nome del nodo, deve essere unico
    );

-- Crea la tabella `node_relationships` se non esiste
CREATE TABLE IF NOT EXISTS node_relationships (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,        -- Identificativo unico per la relazione
                                                  ancestor_id BIGINT NOT NULL,                 -- ID del nodo ancestro
                                                  descendant_id BIGINT NOT NULL,               -- ID del nodo discendente
                                                  depth INT NOT NULL,                          -- Profondità della relazione
                                                  FOREIGN KEY (ancestor_id) REFERENCES nodes(id) ON DELETE CASCADE, -- Relazione con il nodo ancestro
    FOREIGN KEY (descendant_id) REFERENCES nodes(id) ON DELETE CASCADE -- Relazione con il nodo discendente
    );

-- Se il nodo 'root' non esiste, inseriscilo nella tabella `nodes`
INSERT INTO nodes (id, name)
SELECT 1, 'root'
    WHERE NOT EXISTS (SELECT 1 FROM nodes WHERE name = 'root');