package co.edu.unbosque.bogoparche.service;

import java.util.List;

/**
 * Operaciones CRUD básicas que implementan los servicios de la aplicación. Cuando una operación no
 * se puede completar, el servicio lanza una excepción de {@code co.edu.unbosque.bogoparche.exception}.
 *
 * @param <T> tipo de DTO que maneja el servicio
 */
public interface CRUDOperation<T> {

	/**
	 * Crea un registro.
	 *
	 * @param data datos del registro
	 */
	public void create(T data);

	/**
	 * Obtiene todos los registros.
	 *
	 * @return lista de registros
	 */
	public List<T> getAll();

	/**
	 * Elimina un registro por su identificador.
	 *
	 * @param id identificador del registro
	 */
	public void deleteById(Long id);

	/**
	 * Actualiza un registro por su identificador.
	 *
	 * @param id      identificador del registro
	 * @param newData datos nuevos
	 */
	public void updateById(Long id, T newData);

	/**
	 * Cuenta los registros existentes.
	 *
	 * @return número de registros
	 */
	public long count();

	/**
	 * Indica si existe un registro con el identificador dado.
	 *
	 * @param id identificador del registro
	 * @return {@code true} si existe
	 */
	public boolean exist(Long id);
}
