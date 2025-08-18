Feature: Gestión de productos
  Como usuario de la API
  Quiero gestionar los productos en el sistema de inventario
  Para mantener un control adecuado del inventario

  Scenario: Crear un producto exitosamente
    When envío una solicitud POST a "/productos" con los datos del producto:
      | name        | Laptop XYZ ${random.uuid} |
      | description | Laptop de prueba          |
      | category    | Electronics               |
      | price       | 999.99                    |
      | cost        | 800.0                     |
      | profit      | 199.99                    |
      | quantity    | 10                        |
      | minimumStock| 5                         |
    Then recibo un código de estado 200
    And el producto creado tiene el nombre "Laptop XYZ"

  Scenario: Listar productos con filtros
    Given que existen productos en el sistema
    When envío una solicitud GET a "/productos" con los parámetros:
      | page      | 0         |
      | size      | 10        |
      | name      | Laptop    |
      | category  | Electronics |
      | minPrice  | 500.0     |
      | maxPrice  | 1500.0    |
    Then recibo un código de estado 200
    And la respuesta contiene una lista paginada de productos
    And los productos en la lista tienen el nombre que comienza con "Laptop"

  Scenario: Obtener un producto por ID
    Given que existe un producto con nombre "Tablet ABC ${random.uuid}"
    When envío una solicitud GET a "/productos/{id}"
    Then recibo un código de estado 200
    And el producto retornado tiene el nombre "Tablet ABC"

  Scenario: Actualizar un producto existente
    Given que existe un producto con nombre "Phone XYZ ${random.uuid}"
    When envío una solicitud PUT a "/productos" con los datos actualizados:
      | name        | Phone XYZ Updated ${random.uuid} |
      | description | Teléfono actualizado            |
      | category    | Electronics                    |
      | price       | 799.99                         |
      | cost        | 600.0                          |
      | profit      | 199.99                         |
      | quantity    | 15                             |
      | minimumStock| 3                              |
    Then recibo un código de estado 200
    And el producto actualizado tiene el nombre "Phone XYZ Updated"

  Scenario: Actualizar la cantidad de un producto
    Given que existe un producto con nombre "Monitor XYZ ${random.uuid}"
    When envío una solicitud PUT a "/productos/{id}/update-quantity" con cantidad 20
    Then recibo un código de estado 200
    And el producto tiene una cantidad de 30

  Scenario: Ver el historial de movimientos de un producto
    Given que existe un producto con nombre "Camera XYZ ${random.uuid}" y movimientos de inventario
    When envío una solicitud de historial a "/productos/{id}/history" con los parámetros:
      | page | 0  |
      | size | 10 |
    Then recibo un código de estado 200
    And la respuesta contiene una lista paginada de movimientos de inventario
