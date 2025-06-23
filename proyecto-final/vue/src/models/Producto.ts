export interface Producto {
  id?: number
  nombre: string
  descripcion: string
  categoria: string
  precio: number
  costo: number
  beneficio: number
  impuesto?: number
  cantidadInicial: number
}
