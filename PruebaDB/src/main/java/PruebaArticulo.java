import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class PruebaArticulo {

	public static void main(String[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException, SQLException, ClassNotFoundException, InstantiationException {

		Scanner scanner = new Scanner(System.in);


		mostrarMenu();

		while (scanner.hasNext()) {

			System.out.println("");
			String linea = scanner.nextLine();
			String id;
			String nombre;

			switch (linea) {
				case "0":
					System.out.println("Saliendo del programa...");
					System.out.println("Apagado");
					System.exit(0);
					scanner.close();
					break;
	
				case "1":
					System.out.println("Escriba el id de la categoria que quiere recuperar:");
					id = scanner.nextLine();
					Categoria categoria = new Categoria();
					categoria.setId(Long.parseLong(id));
					getOne(categoria);
					System.out.println(categoria.getId() + " " + categoria.getNombre());
					break;
	
				case "2":
					System.out.println("Escriba el nombre de la nueva categoria:");
					nombre = scanner.nextLine();
					Categoria categoriaSave = new Categoria();
					categoriaSave.setId(0);
					categoriaSave.setNombre(nombre);
					save(categoriaSave);
					break;
	
				case "3":
					System.out.println("Escriba el id de la categoria que quiere editar:");
					id = scanner.nextLine();
					Categoria categoriaUpdate = new Categoria();
					categoriaUpdate.setId(Long.parseLong(id));
					getOne(categoriaUpdate);
					System.out.println("Escriba el nombre de la categoria:");
					nombre = scanner.nextLine();
					categoriaUpdate.setNombre(nombre);
					save(categoriaUpdate);
					break;
					
				case "4":
					System.out.println("");
					System.out.println("Escriba el id de la categoria que quiere eliminar:");
					id = scanner.nextLine();
	
					Categoria categoria3 = new Categoria();
					categoria3.setId(Long.parseLong(id));
					delete(categoria3);
					break;
	
				case "5":
					Object[] pojos = getAll("Categoria");
					for (Object pojo : pojos) {
						Categoria categoria2 = (Categoria) pojo;
						System.out.println("Id:" + categoria2.getId() + " Nombre:" + categoria2.getNombre());
	
					}
					break;
			}

			mostrarMenu();
		}

	}

	public static void mostrarMenu(){
		System.out.println("");
		System.out.println("Opciones:");
		System.out.println("0 Salir");
		System.out.println("1 Leer");
		System.out.println("2 Nuevo");
		System.out.println("3 Editar");
		System.out.println("4 Eliminar");
		System.out.println("5 Listar todos");
		System.out.println("Escoja numero de comando:");
	}
	
	/**
	 * 
	 * @param pojoObject
	 * @return Recupera un solo pojo segun el id
	 */
	public static Object getOne(Object pojoObject) throws SQLException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		// Recupera la clase del pojo
		Class<? extends Object> aClass = pojoObject.getClass();

		// Recupera el metodo getId del pojo y lo ejecuta

		long id = (long) aClass.getMethod("getId", (Class<?>[]) null).invoke(pojoObject, (Object[]) null);
		// Conexion

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");
		PreparedStatement preparedStatement = connection
				.prepareStatement("select * from " + aClass.getName() + " where id = " + id);
		ResultSet rows = preparedStatement.executeQuery();

		// Numero de columnas
		int numColumnas = rows.getMetaData().getColumnCount();

		while (rows.next()) {

			for (int i = 1; i <= numColumnas; i++) {

				// Nombre de columna poniendo la primera letra en mayusculas
				String nombreColumna = rows.getMetaData().getColumnLabel(i).substring(0, 1).toUpperCase()
						+ rows.getMetaData().getColumnLabel(i).substring(1);

				if (rows.getObject(i) != null) {
					// objeto con el valor de la columna del row
					Object value = rows.getObject(i);

					try {
						// Recupera el metodo set de la correspondiente columna
						// y lo invoca
						aClass.getMethod("set" + nombreColumna,
								aClass.getDeclaredField(rows.getMetaData().getColumnLabel(i)).getType())
								.invoke(pojoObject, value);

					} catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

		connection.close();

		return pojoObject;

	}

	/**
	 * 
	 * @param tabla
	 * @return Devuelve un array de pojos con todos los pojos de una tabla
	 */
	public static Object[] getAll(String tabla)
			throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");
		PreparedStatement preparedStatement = connection.prepareStatement("select * from " + tabla);
		ResultSet rows = preparedStatement.executeQuery();

		// Recupera el numero de columnas
		int numColumnas = rows.getMetaData().getColumnCount();

		// ArrayLis de pojos vacio
		ArrayList<Object> pojos = new ArrayList<Object>();

		// Consigue la clase segun el nombre de la tabla
		Class<?> aClass = Class.forName(tabla);

		while (rows.next()) {
			// Crea una nueva instancia de la clase recuperada anteriormente
			Object pojo = aClass.newInstance();

			for (int i = 1; i <= numColumnas; i++) {

				// Recupera el nombre de la columna con la primera letra en
				// mayusculas
				String nombreColumna = rows.getMetaData().getColumnLabel(i).substring(0, 1).toUpperCase()
						+ rows.getMetaData().getColumnLabel(i).substring(1);

				if (rows.getObject(i) != null) {
					// Consigue el valor de la fila con respecto a la columna
					Object value = rows.getObject(i);

					try {

						// Recupera el metodo set de la correspondiente columna
						// y lo invoca
						aClass.getMethod("set" + nombreColumna,
								aClass.getDeclaredField(rows.getMetaData().getColumnLabel(i)).getType())
								.invoke(pojo, value);

					} catch (NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			pojos.add(pojo);
		}

		connection.close();

		return pojos.toArray();

	}

	/**
	 * 
	 * Elimina una fila buscando el id en el pojo
	 * @param pojoObject
	 */
	public static void delete(Object pojoObject) throws SQLException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		// Recupera la clase del pojo
		Class<? extends Object> aClass = pojoObject.getClass();

		// Recupera el id del pojo
		long id = (long) aClass.getMethod("getId", (Class<?>[]) null).invoke(pojoObject, (Object[]) null);

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");

		// Realiza un delete con el id recuperado anteriormente
		PreparedStatement preparedStatement = connection
				.prepareStatement("delete from " + pojoObject.getClass().getName() + " where id = " + id);
		preparedStatement.execute();

		System.out.println(
				"El registro de la tabla " + pojoObject.getClass().getName() + " de id " + id + " ha sido eliminada");

	}

	
	
	/**
	 * 
	 * Graba un pojo, si el id es 0 inserta, si el id es distinto edita
	 * @param pojoObject
	 * @return Devuelve el pojo con los datos grabados
	 */
	public static Object save(Object pojoObject) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {

		// Recupera la clase del pojo
		Class<? extends Object> aClass = pojoObject.getClass();

		// Recupera el id del pojo
		long id = (long) aClass.getMethod("getId", (Class<?>[]) null).invoke(pojoObject, (Object[]) null);

		// Recupera los campos de la clase anterior
		Field[] fields = aClass.getDeclaredFields();

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");

		// Si el id es 0, crea uno nuevo, else actualiza uno nuevo
		if (id == 0) {

			String statement = "insert into " + aClass.getName() + " values (";

			// Bucle que recupera los fields
			for (int i = 0; i < fields.length; i++) {

				Field field = fields[i];
				// Recupera el nombre del field y pone la primera letra en
				// mayusculas
				String fieldName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
				// Recupera el metodo get y lo invoca
				String valor = aClass.getMethod("get" + fieldName, (Class<?>[]) null)
						.invoke(pojoObject, (Object[]) null).toString();

				// Si es el ultimo field, cierra el statement sino lo continua
				if (fields.length == i + 1) {

					statement += "'" + valor + "')";
				} else {

					statement += "'" + valor + "',";
				}
			}
			System.out.println("Insert statement: " + statement);
			PreparedStatement preparedStatement = connection.prepareStatement(statement);
			preparedStatement.execute();

			System.out.println("Insertado en tabla " + aClass.getName());

		} else {

			String statement = "update " + aClass.getName() + " set ";

			for (int i = 1; i < fields.length; i++) {
				Field field = fields[i];

				// Recupera el nombre del field y pone la primera letra en
				// mayusculas
				String fieldName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

				// Recupera el metodo get y lo invoca
				String valor = aClass.getMethod("get" + fieldName, (Class<?>[]) null)
						.invoke(pojoObject, (Object[]) null).toString();

				// Si es el ultimo field, cierra el statement sino lo continua
				if (fields.length == i + 1) {

					statement += fieldName + "='" + valor + "' where id='" + id + "';";
				} else {

					statement += fieldName + "='" + valor + "', ";
				}
			}

			System.out.println("Insert statement: " + statement);
			PreparedStatement preparedStatement = connection.prepareStatement(statement);
			preparedStatement.execute();

			System.out.println("Actualizado en tabla " + aClass.getName() + "id = " + id);
		}

		return pojoObject;

	}

}
