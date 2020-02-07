package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

public class MySQLConnection {
	private Connection conn;

	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setFavoriteItems(String userId, Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
//		String sql = "INSERT INTO history(user_id, item_id) VALUES(" + userId + "," + item.getItemId() + ")";
//		String sql = String.format("INSERT INTO history(user_id, item_id) VALUES (%s, %s) ", userId, item.getItemId());
//		try {
//			Statement statement = conn.createStatement(sql);
//			statement.executeUpdate(sql);
//		}catch(SQLException e){
//			e.printStackTrace();
//		}
		saveItem(item);
		String sql = "INSERT INTO history(user_id, item_id) VALUES(?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		saveItem(item);
	}

	public void unsetFavoriteItems(String userId, Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		String sql = "DELETE FROM history WHERE user_id = ? and item_id = ?";
		// "DELETE FROM history WHERE user_id = (111 OR 1=1)" --> no where
		// "DELETE FROM history WHERE user_id = \"111 OR 1=1\" --> ?)" sql injection
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();// update //statement.executeQuery() read
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void saveItem(Item item) {
		if (conn == null) {
			System.out.println("DB connection failed");
			return;
		}
		String sql = "INSERT IGNORE INTO items VALUES(?, ?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		sql = "INSERT IGNORE INTO keywords VALUES(?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
