Feature: Creación de un producto
  Como usuario de la API
  Quiero crear un producto nuevo
  Para añadirlo al sistema de inventario

  Scenario: Crear un producto exitosamente
    When envío una solicitud POST a "/productos" con los datos del producto:
      | name        | Laptop XYZ       |
      | description | Laptop de prueba |
      | category    | Electronics      |
      | price       | 999.99           |
      | cost        | 800.0            |
      | profit      | 199.99           |
      | quantity    | 10               |
      | minimumStock| 5                |
    Then recibo un código de estado 200
    And el producto creado tiene el nombre "Laptop XYZ"
