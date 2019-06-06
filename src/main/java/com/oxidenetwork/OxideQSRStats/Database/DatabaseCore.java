/*
 * This class is heavily inspired by QuickShop Reremake from  Ghost-chu
 * 
 * Check out his github page: https://github.com/Ghost-chu/QuickShop-Reremake
 * 
 * Thank you for this learning experience Ghost-chu
 * 
 */

package com.oxidenetwork.OxideQSRStats.Database;

import java.sql.Connection;

public interface DatabaseCore {
    public Connection getConnection();

    public void queue(BufferStatement bs);

    public void flush();

    public void close();
}