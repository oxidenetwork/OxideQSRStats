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
    private String dbPrefix;
    private int dbTimeOut;

    public DatabaseHelper(OxideQSRStats pl, Database db) throws SQLException {
        plugin = pl;
        database = db;
        dbPrefix = this.plugin.getDbPrefix();
        dbTimeOut = this.plugin.getDbTimeOut();
    	if (database.getConnection().isValid(dbTimeOut)) {
	    	if (!database.hasTable(dbPrefix + "tb_Sales")) {
	    		createSalesTable();
	    	}
	    	if (!database.hasTable(dbPrefix + "tb_Purchases")) {
	    		createPurchaseTable();
	    	}
    	} else {
    		OxideQSRStats.error("Database connection is not valid.");
    	}
    }

    public void createSalesTable() throws SQLException {
    	if (database.getConnection().isValid(5)) {
	    	Statement st = database.getConnection().createStatement();
	        String createTable = null;
	        createTable = "CREATE TABLE " + dbPrefix + "tb_Sales (" +
	        		"ID INTEGER,"+
	        		"ShopOwnerName TEXT,"+
	        		"ShopOwnerUUID TEXT,"+
	        		"ItemName TEXT,"+
	        		"PricePiece REAL,"+
	        		"TotalPayed REAL,"+
	        		"QuantityBought INTEGER,"+
	        		"BuyerName TEXT,"+
	        		"BuyerUUID INTEGER,"+
	        		"AdminShop INTEGER"+
	        	");"+
	        	"CREATE INDEX " + dbPrefix + "tb_Sales_ID_IDX ON " + dbPrefix + "tb_Sales (ID);";
	        st.execute(createTable);
    	} else {
    		OxideQSRStats.error("createSalesTable - Database connection is not valid.");
    	}
    }

    public void insertIntoSales(String shopOwnerName, UUID shopOwnerUUID, String itemName, double pricePiece, double totalPayed, int quantityBought, String buyerName, UUID buyerUUID, boolean adminShop) {
        try {
            String sqlString = "INSERT INTO " + dbPrefix + "tb_Sales (ShopOwnerName,ShopOwnerUUID,ItemName,PricePiece,TotalPayed,QuantityBought,BuyerName,BuyerUUID,AdminShop) VALUES (?,?,?,?,?,?,?,?,?);";
            PreparedStatement ps = database.getConnection().prepareStatement(sqlString);
            ps.setString(1, shopOwnerName);
            ps.setString(2, shopOwnerUUID.toString());
            ps.setString(3, itemName);
            ps.setDouble(4, pricePiece);
            ps.setDouble(5, totalPayed);
            ps.setInt(6, quantityBought);
            ps.setString(7, buyerName);
            ps.setString(8, buyerUUID.toString());
            ps.setBoolean(9, adminShop);
            plugin.getDatabaseManager().add(ps);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void createPurchaseTable() throws SQLException {
    	if (database.getConnection().isValid(5)) {
	    	Statement st = database.getConnection().createStatement();
	        String createTable = null;
	        createTable = "CREATE TABLE " + dbPrefix + "tb_Purchases (" +
	        		"ID INTEGER,"+
	        		"ShopOwnerName TEXT,"+
	        		"ShopOwnerUUID TEXT,"+
	        		"ItemName TEXT,"+
	        		"PricePiece REAL,"+
	        		"TotalPayed REAL,"+
	        		"QuantitySold INTEGER,"+
	        		"SellerName TEXT,"+
	        		"SelletUUID INTEGER,"+
	        		"AdminShop INTEGER"+
	        	");"+
	        	"CREATE INDEX " + dbPrefix + "tb_Purchases_ID_IDX ON " + dbPrefix + "tb_Purchases (ID);";
	        st.execute(createTable);
    	} else {
    		OxideQSRStats.error("createSalesTable - Database connection is not valid.");
    	}
    }    
    
    public void insertIntoPurchases(String shopOwnerName, UUID shopOwnerUUID, String itemName, double pricePiece, double totalPayed, int quantitySold, String sellerName, UUID sellerUUID, boolean adminShop) {
        try {
            String sqlString = "INSERT INTO " + dbPrefix + "tb_Purchases (ShopOwnerName,ShopOwnerUUID,ItemName,PricePiece,TotalPayed,QuantitySold,SellerName,SellerUUID,AdminShop) VALUES (?,?,?,?,?,?,?,?,?);";
            PreparedStatement ps = database.getConnection().prepareStatement(sqlString);
            ps.setString(1, shopOwnerName);
            ps.setString(2, shopOwnerUUID.toString());
            ps.setString(3, itemName);
            ps.setDouble(4, pricePiece);
            ps.setDouble(5, totalPayed);
            ps.setInt(6, quantitySold);
            ps.setString(7, sellerName);
            ps.setString(8, sellerUUID.toString());
            ps.setBoolean(9, adminShop);
            plugin.getDatabaseManager().add(ps);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }   
    
 
    public ResultSet selectAllSales() throws SQLException {
    	if (database.getConnection().isValid(dbTimeOut)) {
	    	Statement st = database.getConnection().createStatement();
	        String selectAllSales = "SELECT * FROM " + dbPrefix + "tb_Sales";
	        return st.executeQuery(selectAllSales);
    	} else {
    		OxideQSRStats.error("selectAllSales - Database connection is not valid.");
    		return null;
    	}
    }

    public ResultSet selectAllPurchases() throws SQLException {
    	if (database.getConnection().isValid(dbTimeOut)) {
	    	Statement st = database.getConnection().createStatement();
	        String selectAllPurchases = "SELECT * FROM " + dbPrefix + "tb_Purchases";
	        return st.executeQuery(selectAllPurchases);
    	} else {
    		OxideQSRStats.error("selectAllPurchases - Database connection is not valid.");
    		return null;
    	}
    }

}