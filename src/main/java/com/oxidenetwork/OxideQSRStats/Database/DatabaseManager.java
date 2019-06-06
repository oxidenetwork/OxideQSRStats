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
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.oxidenetwork.OxideQSRStats.OxideQSRStats;

/**
 * Queued database manager.
 * Use queue to solve run SQL make server lag issue.
 */
@SuppressWarnings("unused")
public class DatabaseManager {
	private OxideQSRStats plugin;
    private Database database;
    private boolean useQueue;
    private Queue<PreparedStatement> sqlQueue = new LinkedBlockingQueue<>();
    private BukkitTask task;

    /**
     * Queued database manager.
     * Use queue to solve run SQL make server lag issue.
     *
     * @param plugin plugin main class
     * @param db database
     */
    public DatabaseManager(OxideQSRStats pl, Database db) {
        plugin = pl;
        database = db;
        useQueue = plugin.getConfig().getBoolean("DatabaseQueue");
        if (!useQueue)
            return;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getDatabaseManager().runTask();
            }
        }.runTaskTimer(plugin, 1, 200);
    }

    /**
     * Unload the DatabaseManager, run at onDisable()
     */
    public void uninit() {
        if ((task != null) && !task.isCancelled())
            task.cancel();
        OxideQSRStats.info("Please waiting for flushing data to database...");
        runTask();
    }

    /**
     * Internal method, runTasks in queue.
     */
    private void runTask() {
        while (true) {
            PreparedStatement statement = sqlQueue.poll();
            if (statement == null)
                break;
            try {
                OxideQSRStats.debug("Executing the SQL task...");
                statement.execute();

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }

            //Close statement anyway.
            try {
                statement.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }

        }
    }


    
    /**
     * Add preparedStatement to queue waiting flush to database,
     * @param ps The ps you want add in queue.
     */
    public void add(PreparedStatement ps) {
        if (useQueue) {
            sqlQueue.offer(ps);
        } else {
            try {
                ps.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
}
