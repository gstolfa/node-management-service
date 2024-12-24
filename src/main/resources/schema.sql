
CREATE TABLE IF NOT EXISTS nodes (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL UNIQUE
    );


CREATE TABLE IF NOT EXISTS node_relationships (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  ancestor_id BIGINT NOT NULL,
                                                  descendant_id BIGINT NOT NULL,
                                                  depth INT NOT NULL,
                                                  FOREIGN KEY (ancestor_id) REFERENCES nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (descendant_id) REFERENCES nodes(id) ON DELETE CASCADE -- Relazione con il nodo discendente
    );


INSERT INTO nodes (id, name)
SELECT 1, 'root'
    WHERE NOT EXISTS (SELECT 1 FROM nodes WHERE name = 'root');
