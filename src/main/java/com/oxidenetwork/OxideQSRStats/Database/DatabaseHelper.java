/*
 * This class is heavily inspired by QuickShop Reremake from  Ghost-chu
 * 
 * Check out his github page: https://github.com/Ghost-chu/QuickShop-Reremake
 * 
 * Thank you for this learning experience Ghost-chu
 * 
 */

package com.oxidenetwork.OxideQSRStats.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.oxidenetwork.OxideQSRStats.OxideQSRStats;

public class DatabaseHelper {
    private Database database;
    private OxideQSRStats plugin;
    private String dbPrefix = "";
    private int dbTimeOut = 10;
    private boolean isDbMySQL = false;

    public DatabaseHelper(OxideQSRStats pl, Database db) throws SQLException {
        plugin = pl;
        database = db;
        dbPrefix = this.plugin.getDbPrefix();
        dbTimeOut = this.plugin.getDbTimeOut();
        isDbMySQL = this.plugin.isDbMySQL();
        
    	if (database.getConnection().isValid(dbTimeOut)) {
	    	if (!database.hasTable(dbPrefix + "tb_Transactions")) {
	    		createTransactionTable();
	    		OxideQSRStats.debug(dbPrefix + "tb_Transactions created in database.");
	    	}
    	} else {
    		OxideQSRStats.error("Database connection is not valid.");
    	}
    }

    public void createTransactionTable() throws SQLException {
    	if (database.getConnection().isValid(5)) {
    		Statement st = database.getConnection().createStatement();
	        String createTable = null;

	        if (isDbMySQL) {
		        createTable = "CREATE TABLE " + dbPrefix + "`tb_Transactions` (" + 
		        		"  `ID` INTEGER unsigned NOT NULL AUTO_INCREMENT," + 
		        		"  `ShopOwnerName` text DEFAULT NULL," + 
		        		"  `ShopOwnerUUID` text DEFAULT NULL," + 
		        		"  `ItemName` text DEFAULT NULL," + 
		        		"  `PiecePrice` double DEFAULT NULL," + 
		        		"  `TotalPrice` double DEFAULT NULL," +
		        		"  `TaxPrice` double DEFAULT NULL," +
		        		"  `Tax` double DEFAULT NULL," +
		        		"  `Quantity` INTEGER DEFAULT NULL," + 
		        		"  `PlayerName` text DEFAULT NULL," + 
		        		"  `PlayerUUID` text DEFAULT NULL," + 
		        		"  `AdminShop` INTEGER DEFAULT NULL," +
		        		"  `Action` TEXT DEFAULT NULL," + 
		        		"  PRIMARY KEY (`ID`)," + 
		        		"  KEY `" + dbPrefix + "tb_Transactions` (`ID`)" + 
		        		");";
		        st.execute(createTable);
	        } else {
		        createTable = "CREATE TABLE `" + dbPrefix + "tb_Transactions` (" + 
		        		"    ID 			INTEGER PRIMARY KEY AUTOINCREMENT" + 
		        		"  , ShopOwnerName 	TEXT" + 
		        		"  , ShopOwnerUUID 	TEXT" + 
		        		"  , ItemName 		TEXT" + 
		        		"  , PiecePrice 	DOUBLE" + 
		        		"  , TotalPrice 	DOUBLE" + 
		        		"  , TaxPrice 		DOUBLE" + 
		        		"  , Tax 			DOUBLE" + 
		        		"  , Quantity 		INTEGER" + 
		        		"  , PlayerName 	TEXT" + 
		        		"  , PlayerUUID 	TEXT" + 
		        		"  , AdminShop 		INTEGER" + 
		        		"  , Action 		TEXT" + 
		        		");";	        	
		        st.execute(createTable);
	        }
    	} else {
    		OxideQSRStats.error("createTransactionTable - Database connection is not valid.");
    	}
    }

    public void insertIntoTransactions(String shopOwnerName, UUID shopOwnerUUID, String itemName, double piecePrice, double totalPrice, double taxPrice, double tax, int quantity, String playerName, UUID playerUUID, boolean adminShop, String action) {
        try {
            String sqlString = "INSERT INTO " + dbPrefix + "tb_Transactions "
            		+ "(ShopOwnerName, ShopOwnerUUID, ItemName,"
            		+ "PiecePrice, TotalPrice, TaxPrice, Tax,"
            		+ "Quantity, PlayerName, PlayerUUID,"
            		+ "AdminShop, Action) "
            		+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement ps = database.getConnection().prepareStatement(sqlString);
            ps.setString(1, shopOwnerName);
            ps.setString(2, shopOwnerUUID.toString());
            ps.setString(3, itemName);
            ps.setDouble(4, piecePrice);
            ps.setDouble(5, totalPrice);
            ps.setDouble(6, taxPrice);
            ps.setDouble(7, tax);
            ps.setInt(8, quantity);
            ps.setString(9, playerName);
            ps.setString(10, playerUUID.toString());
            ps.setBoolean(11, adminShop);
            ps.setString(12, action);
            plugin.getDatabaseManager().add(ps);
            OxideQSRStats.debug("New transaction added to database");
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
   
 
    public ResultSet selectAllSales() throws SQLException {
    	if (database.getConnection().isValid(dbTimeOut)) {
	    	Statement st = database.getConnection().createStatement();
	        String selectAllSales = "SELECT * FROM " + dbPrefix + "tb_Transactions WHERE `action`='SALE'";
	        return st.executeQuery(selectAllSales);
    	} else {
    		OxideQSRStats.error("selectAllSales - Database connection is not valid.");
    		return null;
    	}
    }

    public ResultSet selectAllPurchases() throws SQLException {
    	if (database.getConnection().isValid(dbTimeOut)) {
	    	Statement st = database.getConnection().createStatement();
	        String selectAllPurchases = "SELECT * FROM " + dbPrefix + "tb_Transactions WHERE `action`='BUY'";
	        return st.executeQuery(selectAllPurchases);
    	} else {
    		OxideQSRStats.error("selectAllPurchases - Database connection is not valid.");
    		return null;
    	}
    }

}