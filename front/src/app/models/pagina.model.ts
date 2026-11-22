/**
 * Página de resultados de la auditoría.
 */
export interface PaginaModel<T> {
  /** Elementos de la página. */
  content: T[];
  /** Número de la página, desde 0. */
  page: number;
  /** Tamaño de la página. */
  size: number;
  /** Total de elementos. */
  totalElements: number;
  /** Total de páginas. */
  totalPages: number;
}
