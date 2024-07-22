package de.jonas.telepads;

import java.sql.*;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataBasePool {
    HikariDataSource hikari;

    public void init() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("db-hikari");
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl("jdbc:mariadb://IP/telepads");
        config.setUsername("telepads");
        config.setPassword("PASSWD");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setMaxLifetime(0);
        config.setKeepaliveTime(120000);
        config.setConnectionTimeout(30000);

        this.hikari = new HikariDataSource(config);
        deregisterDriver("org.mariadb.jdbc.Driver");
    }

    public void shutdown() {
        if (this.hikari != null) {
            this.hikari.close();
        }
    }

    private static void deregisterDriver(String driverClassName) {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals(driverClassName)) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (this.hikari == null) {
            throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
        }

        Connection connection = this.hikari.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool. (getConnection returned null)");
        }

        return connection;
    }

    public void createTableTelepads() throws SQLException {
        Connection con = getConnection();
        String sqlCreate = "CREATE TABLE IF NOT EXISTS telepads (" +
            "id INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL," +
            "location VARCHAR(64) NOT NULL," +
            "name VARCHAR(64) NOT NULL," +
            "owner UUID NOT NULL," +
            "`level` TINYINT NOT NULL," +
            "destinationID INTEGER," +
            "public boolean NOT NULL)" +
            "ENGINE = InnoDB;";

        Statement stmt = con.createStatement();
        stmt.execute(sqlCreate);
    }

    public void createTableTelePermission() throws SQLException {
        Connection con = getConnection();
        String sqlCreate = "CREATE TABLE IF NOT EXISTS telepermission (" +
            "id INTEGER NOT NULL," +
            "player UUID NOT NULL)" +
            "ENGINE = InnoDB;";

        Statement stmt = con.createStatement();
        stmt.execute(sqlCreate);
    }

    public static int setNewTelepad(DataBasePool pool, UUID owner, Location location) {
        String loc = location.getWorld().getName() + "|" + location.getBlockX() + "|" + location.getBlockY() + "|" + location.getBlockZ();
        String querry = "INSERT INTO `telepads` (`location`, `name`, `owner`, `level`, `public`) VALUES (?, ?, ?, 1, false) RETURNING `id`;";
        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, loc);
            sel.setObject(2, loc);
            sel.setObject(3, owner);
            ResultSet res = sel.executeQuery();
            res.first();
            int id = res.getInt("id");
            sel.close();
            con.close();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void removeTelepad(DataBasePool pool, int id) {
        try {
            String querry = "DELETE FROM `telepads` WHERE `id` = ?;";
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            sel.executeQuery();
            sel.close();
            con.close();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    public static String getName(DataBasePool pool, int id) {
        String querry = "SELECT `telepads`.`name` FROM `telepads` WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res = sel.executeQuery();
            res.first();
            String name = res.getString("name");
            sel.close();
            con.close();
            return name;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setName(DataBasePool pool, int id, String newName) {
        String querry = "UPDATE `telepads` SET `name` = ? WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, newName);
            sel.setObject(2, id);
            sel.executeUpdate();
            sel.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Location getlocation(DataBasePool pool, int id) {
        String querry = "SELECT `telepads`.`location` FROM `telepads` WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res = sel.executeQuery();
            res.first();
            String a = res.getString("location");
            String[] loc = a.split("\\|");
            World world = Bukkit.getWorld(loc[0]);
            int x = Integer.parseInt(loc[1]);
            int y = Integer.parseInt(loc[2]);
            int z = Integer.parseInt(loc[3]);
            Location location = new Location(world, x, y, z);
            sel.close();
            con.close();
            return location;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static UUID getOwner(DataBasePool pool, int id) {
        String querry = "SELECT `telepads`.`owner` FROM `telepads` WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res = sel.executeQuery();
            res.first();
            UUID owner = (UUID) res.getObject("owner");
            sel.close();
            con.close();
            return owner;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getLevel(DataBasePool pool, int id) {
        String querry = "SELECT `telepads`.`level` FROM `telepads` WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res = sel.executeQuery();
            res.first();
            int level = res.getInt("level");
            sel.close();
            con.close();
            return level;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<Integer> getTelepads(DataBasePool pool) {
        String querry = "SELECT telepads.id FROM telepads;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            ResultSet res = sel.executeQuery();
            List<Integer> list = new ArrayList<>();
            for (boolean a = res.first(); a; a = res.next() ) {
                list.add(res.getInt("id"));
            }
            sel.close();
            con.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void removeTelepadsDestinationWithDestination(DataBasePool pool, int desti) {
        String querry = "UPDATE `telepads` SET `telepads`.`destinationID` = null WHERE `telepads`.`destinationID` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, desti);
            sel.executeQuery();
            sel.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getTelepadsIFPermission(DataBasePool pool, UUID playerUUID, int id) {
        String querry = "SELECT * FROM telepads WHERE (telepads.owner = ? OR telepads.public OR telepads.id IN (SELECT id FROM telepermission WHERE telepermission.player = ?)) AND NOT telepads.id = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, playerUUID);
            sel.setObject(2, playerUUID);
            sel.setObject(3, id);
            ResultSet res = sel.executeQuery();
            List<Integer> list = new ArrayList<>();
            for (boolean a = res.first(); a; a = res.next() ) {
                list.add(res.getInt("id"));
            }
            sel.close();
            con.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Integer> getAllTelepadsIFPermission(DataBasePool pool, UUID playerUUID) {
        String querry = "SELECT * FROM telepads WHERE (telepads.owner = ? OR telepads.public OR telepads.id IN (SELECT id FROM telepermission WHERE telepermission.player = ?));";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, playerUUID);
            sel.setObject(2, playerUUID);
            ResultSet res = sel.executeQuery();
            List<Integer> list = new ArrayList<>();
            for (boolean a = res.first(); a; a = res.next() ) {
                list.add(res.getInt("id"));
            }
            sel.close();
            con.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Integer> getAllTelepadsIFPermissionAndLevel2Pad(DataBasePool pool, UUID playerUUID) {
        String querry = "SELECT * FROM telepads WHERE (telepads.owner = ? OR telepads.public OR telepads.id IN (SELECT id FROM telepermission WHERE telepermission.player = ?)) AND `level` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, playerUUID);
            sel.setObject(2, playerUUID);
            sel.setObject(3, 2);
            ResultSet res = sel.executeQuery();
            List<Integer> list = new ArrayList<>();
            for (boolean a = res.first(); a; a = res.next() ) {
                list.add(res.getInt("id"));
            }
            sel.close();
            con.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setNewDestinationID(DataBasePool pool, int idSource, int idtarget) {
        String querry = "UPDATE `telepads` SET `destinationID` = ? WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, idtarget);
            sel.setObject(2, idSource);
            sel.executeUpdate();
            sel.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Location getDestination(DataBasePool pool, int id) {
        String querry = "SELECT tp1.`location` as `location` FROM `telepads` as tp1 INNER JOIN `telepads` as tp2 ON tp1.`id` = tp2.`destinationID` WHERE tp2.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res = sel.executeQuery();
            if (!res.first()) {
                sel.close();
                con.close();
                return null;
            }
            String a = res.getString("location");
            String[] loc = a.split("\\|");
            World world = Bukkit.getWorld(loc[0]);
            int x = Integer.parseInt(loc[1]);
            int y = Integer.parseInt(loc[2]);
            int z = Integer.parseInt(loc[3]);
            Location location = new Location(world, x, y, z);
            sel.close();
            con.close();
            return location;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDestinationName(DataBasePool pool, int id) {
        String querry = "SELECT tp1.`name` as `name` FROM `telepads` as tp1 INNER JOIN `telepads` as tp2 ON tp1.`id` = tp2.`destinationID` WHERE tp2.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res = sel.executeQuery();
            if (!res.first()) {
                sel.close();
                con.close();
                return null;
            }
            String a = res.getString("name");
            sel.close();
            con.close();
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addPlayerPermission(DataBasePool pool, int telepad, UUID playerUUID) {
        String querry = "INSERT INTO `telepermission` (`id`, `player`) VALUES (?, ?);";
        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, telepad);
            sel.setObject(2, playerUUID);
            sel.executeQuery();
            sel.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removePlayerPermission(DataBasePool pool, int telepad, UUID playerUUID) {
        String querry = "DELETE FROM telepermission WHERE telepermission.id = ? AND telepermission.player = ?;";
        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, telepad);
            sel.setObject(2, playerUUID);
            sel.executeUpdate();
            sel.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<UUID> getPermittetPlayer(DataBasePool pool, int telepad) {
        String querry = "SELECT telepermission.player FROM telepermission WHERE telepermission.id = ?;";
        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, telepad);
            ResultSet res = sel.executeQuery();
            List<UUID> list = new ArrayList<>();
            for (boolean a = res.first(); a; a = res.next() ) {
                list.add((UUID) res.getObject("player"));
            }
            sel.close();
            con.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean playerHasPermission(DataBasePool pool, int telepad, UUID playerUUID) {
        String querry = "SELECT telepermission.id FROM telepermission WHERE telepermission.id = ? AND telepermission.player = ?;";
        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, telepad);
            sel.setObject(2, playerUUID);
            ResultSet res = sel.executeQuery();
            if (!res.first()) {
                sel.close();
                con.close();
                return false;
            }
            sel.close();
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean playerIsOwner(DataBasePool pool, int telepad, UUID playerUUID) {
        String querry = "SELECT telepads.owner FROM telepads WHERE telepads.id = ? AND telepads.owner = ?;";
        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, telepad);
            sel.setObject(2, playerUUID);
            ResultSet res = sel.executeQuery();
            if (!res.first()) {
                sel.close();
                con.close();
                return false;
            }
            sel.close();
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setPublic(DataBasePool pool,int id) {
        String querry = "UPDATE `telepads` SET `public` = (NOT `public`) WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            sel.executeUpdate();
            sel.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean getPublic(DataBasePool pool,int id) {
        String querry = "SELECT telepads.public FROM `telepads` WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res =  sel.executeQuery();
            res.first();
            boolean b = res.getBoolean("public");
            sel.close();
            con.close();
            return b;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setLevel2(DataBasePool pool,int id) {
        String querry = "UPDATE `telepads` SET `level` = 2 WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            sel.executeUpdate();
            sel.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean telepadExists(DataBasePool pool,int id) {
        String querry = "SELECT * FROM `telepads` WHERE `telepads`.`id` = ?;";

        try {
            Connection con = pool.getConnection();
            PreparedStatement sel = con.prepareStatement(querry);
            sel.setObject(1, id);
            ResultSet res = sel.executeQuery();
            if (!res.first()) {
                sel.close();
                con.close();
                return false;
            }
            sel.close();
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
