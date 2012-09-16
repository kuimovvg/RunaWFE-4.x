/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import net.sourceforge.jtds.jdbc.Driver;

/**
 * Parse jboss logs and store cache statistic to database.
 * @author Konstantinov Aleksey
 */
public class LogParser {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    static final String counterPrefixString = "Statistic for counter";

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("r:/server.log");
        try {
            DriverManager.registerDriver(new Driver());
            Connection connection = DriverManager.getConnection(
                "jdbc:jtds:sqlserver://192.168.1.3:4333;instanceName=SHARD1;databaseName=CacheAnalize;", "sa", "123456");
            try {
                Statement stmt = connection.createStatement();
                stmt
                    .executeUpdate("IF EXISTS(SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'STATISTIC') DROP TABLE STATISTIC;");
                stmt
                    .executeUpdate("CREATE TABLE STATISTIC (Id BIGINT IDENTITY(1, 1) PRIMARY KEY, " +
                        "CounterName VARCHAR(128), Time DATETIME NOT NULL, Elapsed INT NOT NULL, Rebuild INT NOT NULL, CommitCount INT NOT NULL, " +
                        "HitGet INT NOT NULL, MissGet INT NOT NULL, HitContains INT NOT NULL, MissContains INT NOT NULL)");

                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String data;
                while ((data = br.readLine()) != null) {
                    if (data.contains("ru.runa.commons.cache.CacheStatistic")) {
                        processStatistic(stmt, br, data);
                    }
                }
            } finally {
                connection.close();
            }
        } finally {
            fis.close();
        }
    }

    /**
     * @param stmt
     * @param br
     * @param data
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private static void processStatistic(Statement stmt, BufferedReader br, String data) throws IOException, SQLException {
        String dateString = data.substring(0, data.indexOf("INFO")).trim().replace(",", ".");
        if (!data.contains(counterPrefixString)) {
            data = br.readLine();
        }
        while (data.contains(counterPrefixString)) {
            String counterName = data.substring(data.indexOf(counterPrefixString) + counterPrefixString.length() + 2, data.indexOf("'."));
            long elapsed = Long.parseLong(data.substring(data.indexOf("elapsed") + 8, data.indexOf("milliseconds")).trim());
            data = br.readLine();
            int startIdx = data.indexOf("Rebuild:") + 8;
            long rebuild = Long.parseLong(data.substring(startIdx, data.indexOf("(", startIdx)).trim());
            startIdx = data.indexOf("Commit:", startIdx) + 7;
            long commit = Long.parseLong(data.substring(startIdx, data.indexOf("(", startIdx)).trim());
            startIdx = data.indexOf("Hit on get:", startIdx) + 11;
            long hitGet = Long.parseLong(data.substring(startIdx, data.indexOf("(", startIdx)).trim());
            startIdx = data.indexOf("Miss on get:", startIdx) + 12;
            long missGet = Long.parseLong(data.substring(startIdx, data.indexOf("(", startIdx)).trim());
            startIdx = data.indexOf("Hit on contains:", startIdx) + 16;
            long hitContains = Long.parseLong(data.substring(startIdx, data.indexOf("(", startIdx)).trim());
            startIdx = data.indexOf("Miss on contains:", startIdx) + 17;
            long missContains = Long.parseLong(data.substring(startIdx, data.indexOf("(", startIdx)).trim());
            String string = "INSERT INTO STATISTIC " +
                "(CounterName, Time, Elapsed, Rebuild, CommitCount, HitGet, MissGet, HitContains, MissContains)" +
                " VALUES ('" + counterName + "', '" + dateString + "', " + elapsed + ", " + rebuild + ", " + commit + ", " +
                hitGet + ", " + missGet + ", " + hitContains + ", " + missContains + ")";
            stmt.executeUpdate(string);
            data = br.readLine();
        }
    }
}
