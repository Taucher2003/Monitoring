package de.taucher.monitoring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MysqlManager {

	private Connection con;
	
	private String host;
	private int port;
	private String user;
	private String pass;
	private String db;
	
	/**
	 * constructor for database manager
	 * @param host the host of the database
	 * @param port the port of the database
	 * @param user the user of the database
	 * @param pass the password for the user
	 * @param db the database to use on the database server
	 */
	public MysqlManager(String host, int port, String user, String pass, String db) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.db = db;
	}
	
	/**
	 * connects to the database
	 * @return the instance
	 */
	public MysqlManager connect() {
		return connect(this.host, this.port, this.user, this.pass, this.db);
	}

	/**
	 * connects to the database
	 * @param host the host of the database
	 * @param port the port of the database
	 * @param user the user of the database
	 * @param pass the password for the user
	 * @param db the database to use on the database server
	 * @return the instance
	 */
	private MysqlManager connect(String host, int port, String user, String pass, String db) {
		if(!isConnected()) {
			try {
				con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Berlin", user, pass);
				System.out.println("[MySQL] Zu MySQL verbunden.");
				return this;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * disconnects from the database
	 */
	public void disconnect(){
		if(isConnected()) {
			try {
				con.close();
				System.out.println("[MySQL] Von MySQL getrennt.");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @return true if connected to the database, otherwise false
	 */
	public boolean isConnected(){
		return(con == null ? false : true);
	}

	/**
	 * executes an update on the database
	 * @param qry the query to update
	 */
	protected void update(String qry){
		try{
			PreparedStatement ps = con.prepareStatement(qry.replace(";", ""));
			ps.executeUpdate();
		}catch(SQLException ex ){
			ex.printStackTrace();
		}
	}
	
	/**
	 * executes an update on the database
	 * @param qry the query with placeholders to update
	 * @param params the values to replace the placeholders
	 */
	public void update(String qry, String... params) {
		try {
			PreparedStatement ps = con.prepareStatement(qry);
			for(int i = 0; i<params.length; i++) {
				ps.setString(i+1, params[i]);
			}
			ps.executeUpdate();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * executes a query on the database
	 * @param qry the query
	 * @return the result of the query
	 */
	protected ResultSet query(String qry) {
		ResultSet rs = null;

		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(qry.replace(";", ""));
		} catch (SQLException e) {
			connect(this.host, this.port, this.user, this.pass, this.db);
			System.err.println(e);
		}
		return rs;
	}
	
	/**
	 * executes a query on the database
	 * @param qry the query with placeholders
	 * @param column the column to get
	 * @param params the values to replace the placeholders
	 * @return the first result from the database matching the query
	 */
	public String getFirstResult(String qry, String column, String... params) {
		try {
			PreparedStatement ps = con.prepareStatement(qry.replace(";", ""));
			for(int i = 0; i<params.length; i++) {
				ps.setString(i+1, params[i]);
			}
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(column);
			}
			return null;
		}catch(SQLException e) {
		}
		return null;
	}
	
	/**
	 * executes a query on the database
	 * @param qry the query with placeholders
	 * @param params the values to replace the placeholders
	 * @return first result from the database matching the query. 
	 */
	public HashMap<String, String> getFullResult(String qry, String... params){
        try {
            PreparedStatement ps = con.prepareStatement(qry.replace(";", ""));
            for(int i = 0; i<params.length; i++) {
                ps.setString(i+1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                ResultSetMetaData metadata = rs.getMetaData();
                HashMap<String, String> hm = new HashMap<>();
                int columnCount = metadata.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                   hm.put(metadata.getColumnName(i), rs.getString(i));
                }
                return hm;
            }
        }catch(SQLException e) {
        }
        return null;
    }
	
	/**
	 * executes a query on the database
	 * @param qry the query with placeholders
	 * @param column the column to get
	 * @param params the values to replace the placeholders
	 * @return all results from the database matching the query
	 */
	public List<String> getAllResults(String qry, String column, String... params) {
		try {
			PreparedStatement ps = con.prepareStatement(qry.replace(";", ""));
			for(int i = 0; i<params.length; i++) {
				ps.setString(i+1, params[i]);
			}
			ResultSet rs = ps.executeQuery();
			List<String> result = new LinkedList<>();
			while(rs.next()) {
				result.add(rs.getString(column));
			}
			return result;
		}catch(SQLException e) {
		}
		return null;
	}
	
	/**
	 * executes a query on the database
	 * @param qry the query with placeholders
	 * @param params the values to replace the placeholders
	 * @return all results from the database matching the query. 
	 */
	public List<HashMap<String, String>> getFullResults(String qry, String... params){
        try {
            PreparedStatement ps = con.prepareStatement(qry.replace(";", ""));
            for(int i = 0; i<params.length; i++) {
                ps.setString(i+1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            List<HashMap<String, String>> result = new LinkedList<>();
            while(rs.next()) {
                ResultSetMetaData metadata = rs.getMetaData();
                HashMap<String, String> hm = new HashMap<>();
                int columnCount = metadata.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                   hm.put(metadata.getColumnName(i), rs.getString(i));
                }
                result.add(hm);
            }
            return result;
        }catch(SQLException e) {
        }
        return null;
    }
}
