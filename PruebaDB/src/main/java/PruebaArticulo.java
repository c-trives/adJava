import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PruebaArticulo {

	public static void main(String[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException, SQLException, ClassNotFoundException, InstantiationException {

		Categoria categoria = new Categoria();
		categoria.setId(2);
		getOne(categoria);
		System.out.println(categoria.getId() + " " + categoria.getNombre());

		Object[] pojos = getAll("Articulo");

		for (Object pojo : pojos) {

			Articulo articulo = (Articulo) pojo;

			System.out.println("Id: " + articulo.getId() + " Nombre: " + articulo.getNombre());

		}

		Articulo articulo = new Articulo();
		articulo.setId(8);

		delete(articulo);

		Categoria categoriaSave = new Categoria();
		categoriaSave.setId(0);
		categoriaSave.setNombre("prueba2");
		// save(categoriaSave);

		Categoria categoriaUpdate = new Categoria();
		categoriaUpdate.setId(3);
		getOne(categoriaUpdate);
		categoriaUpdate.setNombre("CAMBIADO423");
		save(categoriaUpdate);
	}

	public static Object getOne(Object pojoObject) throws SQLException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Class<? extends Object> aClass = pojoObject.getClass();
		long id = (long) aClass.getMethod("getId", (Class<?>[]) null).invoke(pojoObject, (Object[]) null);

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");
		PreparedStatement preparedStatement = connection
				.prepareStatement("select * from " + pojoObject.getClass().getName() + " where id = " + id);
		ResultSet rows = preparedStatement.executeQuery();

		int numColumnas = rows.getMetaData().getColumnCount();

		while (rows.next()) {

			for (int i = 1; i <= numColumnas; i++) {
				String nombreColumna = rows.getMetaData().getColumnLabel(i).substring(0, 1).toUpperCase()
						+ rows.getMetaData().getColumnLabel(i).substring(1);

				if (rows.getObject(i) != null) {

					Object[] value = new Object[1];
					value[0] = rows.getObject(i);

					try {
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

	public static Object[] getAll(String tabla)
			throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");
		PreparedStatement preparedStatement = connection.prepareStatement("select * from " + tabla);
		ResultSet rows = preparedStatement.executeQuery();
		int numColumnas = rows.getMetaData().getColumnCount();

		ArrayList<Object> pojos = new ArrayList<Object>();
		Class<?> aClass = Class.forName(tabla);

		while (rows.next()) {
			Object pojo = aClass.newInstance();
			for (int i = 1; i <= numColumnas; i++) {
				String nombreColumna = rows.getMetaData().getColumnLabel(i).substring(0, 1).toUpperCase()
						+ rows.getMetaData().getColumnLabel(i).substring(1);

				if (rows.getObject(i) != null) {

					Object[] value = new Object[1];
					value[0] = rows.getObject(i);

					try {
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

	public static void delete(Object pojoObject) throws SQLException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<? extends Object> aClass = pojoObject.getClass();
		long id = (long) aClass.getMethod("getId", (Class<?>[]) null).invoke(pojoObject, (Object[]) null);

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");
		PreparedStatement preparedStatement = connection
				.prepareStatement("delete from " + pojoObject.getClass().getName() + " where id = " + id);
		preparedStatement.execute();

		System.out.println(
				"El registro de la tabla " + pojoObject.getClass().getName() + " de id " + id + " ha sido eliminada");

	}

	public static Object save(Object pojoObject) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {

		Class<? extends Object> aClass = pojoObject.getClass();
		long id = (long) aClass.getMethod("getId", (Class<?>[]) null).invoke(pojoObject, (Object[]) null);
		Field[] fields = aClass.getDeclaredFields();

		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");

		if (id == 0) {
			String statement = "insert into " + aClass.getName() + " values (";

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];

				String fieldName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

				String valor = aClass.getMethod("get" + fieldName, (Class<?>[]) null)
						.invoke(pojoObject, (Object[]) null).toString();

				if (fields.length == i + 1) {

					statement += "'" + valor + "')";
				} else {

					statement += "'" + valor + "',";
				}
			}
			System.out.println("insert statement: " + statement);
			PreparedStatement preparedStatement = connection.prepareStatement(statement);
			preparedStatement.execute();

			System.out.println("insertado en tabla " + aClass.getName());
		} else {

			String statement = "update " + aClass.getName() + " set ";

			for (int i = 1; i < fields.length; i++) {
				Field field = fields[i];

				String fieldName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

				String valor = aClass.getMethod("get" + fieldName, (Class<?>[]) null)
						.invoke(pojoObject, (Object[]) null).toString();

				if (fields.length == i + 1) {

					statement += fieldName + "='" + valor + "' where id='" + id + "';";
				} else {

					statement += fieldName + "='" + valor + "', ";
				}
			}
			System.out.println("insert statement: " + statement);
			PreparedStatement preparedStatement = connection.prepareStatement(statement);
			preparedStatement.execute();

			System.out.println("Actualizado en tabla " + aClass.getName() + "id = " + id);
		}

		return pojoObject;

	}

}
