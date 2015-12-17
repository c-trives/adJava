import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PruebaArticulo {
	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {

		Categoria articulo = new Categoria();
		articulo.setId(1);
		getOne(articulo);
		System.out.println(articulo.getNombre());

	}

	public static Object getOne(Object pojoObject) throws SQLException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
	
		
		Class<? extends Object> aClass = pojoObject.getClass();
		long id = (long) aClass
				.getMethod("getId",(Class<?>[])null)
				.invoke(pojoObject, (Object[]) null);
		
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/dbprueba", "root", "sistemas");
		PreparedStatement preparedStatement = connection.prepareStatement("select * from "+ pojoObject.getClass().getName() + " where id = " +id);
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
						aClass
								.getMethod("set" + nombreColumna,
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

}
