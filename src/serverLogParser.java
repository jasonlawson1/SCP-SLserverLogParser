import javax.xml.transform.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;

public class serverLogParser {
    public static void main(String[] args){
        //Regular expression pattern that matches the event where a player joins the server. This also groups that player.
        Pattern patternPlayer = Pattern.compile("Nickname of (\\d+@steam) is now \\b(\\w+(?:[-\\s']\\w+)*)\\b.");
        //Regular expression pattern that matches the event where a player kills another player. This also groups the victim name and class, and killer name and class.
        Pattern patternKill = Pattern.compile(" \\((\\d+@steam)\\), playing as \\b(\\w+(?:[-\\s']\\w+)*)\\b, has been killed by \\b.+? \\((\\d+@steam)\\) playing as: \\b(\\w+(?:[-\\s']\\w+)*)\\b. Specific death reason:.*");
        //Regular expression pattern that matches the event where a player suicides. This also groups that players name and class.
        Pattern patternSuicide = Pattern.compile("\\((\\d+@steam)\\), playing as \\b(\\w+(?:[-\\s']\\w+)*)\\b, has died. Specific death reason:.*");
        //Paths to the directory that has the server logs
        File logDR = new File("C:\\Users\\jason\\AppData\\Roaming\\SCP Secret Laboratory\\ServerLogs\\7777");
        playerEvent(patternPlayer, logDR);
        playerKill(patternKill, logDR);
        playerSuicide(patternSuicide, logDR);
    }
    public static void playerEvent(Pattern pattern, File logDirectory){
        try{
            Connection conn = DBconn.getConnection();
            Statement stmt1 = conn.createStatement();
            stmt1.execute("USE scpdb");
            System.out.println("Connected!");
            File[] logs = logDirectory.listFiles();
            for(File log: logs ){
                try(BufferedReader reader = new BufferedReader(new FileReader(log))){
                    String line;
                    while((line = reader.readLine()) != null){
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.find()){
                            String steamID = matcher.group(1);
                            String playerName = matcher.group(2);
                            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM players WHERE player_name = ?");
                            stmt.setString(1, playerName);
                            ResultSet rs = stmt.executeQuery();
                            rs.next();
                            int playerNameCount = rs.getInt(1);
                            PreparedStatement stmt2 = conn.prepareStatement("SELECT COUNT(*) FROM players WHERE steam_id = ?");
                            stmt2.setString(1, steamID);
                            ResultSet rs1 = stmt2.executeQuery();
                            rs1.next();
                            int steamIDCount = rs1.getInt(1);
                            if(steamIDCount > 0 && playerNameCount == 0){
                                PreparedStatement renameStmt = conn.prepareStatement("UPDATE players SET name = ? WHERE steam_id = ?");
                                renameStmt.setString(1, playerName);
                                renameStmt.setString(2, steamID);
                                renameStmt.executeUpdate();
                            }else if(steamIDCount  > 0 && playerNameCount > 0){
                                System.out.println(playerName + " already exists  in the database.");
                            }else{
                                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO players(player_name, steam_id) VALUES (?, ?)");
                                insertStmt.setString(1, playerName);
                                insertStmt.setString(2, steamID);
                                insertStmt.executeUpdate();
                                System.out.println("Added " + playerName + " to the  database.");
                            }
                        }
                    }
                }catch(IOException e){
                    System.out.println("Error reading log file: " + log.getName());
                    e.printStackTrace();
                }
            }
        }catch(SQLException e){
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
    }
    public static void playerKill(Pattern pattern, File logDirectory){
        try {
            Connection conn = DBconn.getConnection();
            Statement stmt1 = conn.createStatement();
            stmt1.execute("USE scpdb");
            System.out.println("Connected!");
            File[] logs = logDirectory.listFiles();
            for(File log: logs){
                try(BufferedReader reader = new BufferedReader(new FileReader(log))){
                    String line;
                    while((line = reader.readLine()) != null){
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.find()){
                            String victimID = matcher.group(1);
                            String victimClass = matcher.group(2);
                            String killerID = matcher.group(3);
                            String killerClass = matcher.group(4);
                            PreparedStatement stmt = conn.prepareStatement("INSERT INTO kill_event (victim_ID, victim_class, killer_ID, killer_class) VALUES (?, ?, ?, ?)");
                            stmt.setString(1, victimID);
                            stmt.setString(2, victimClass);
                            stmt.setString(3, killerID);
                            stmt.setString(4, killerClass);
                            stmt.executeUpdate();
                            System.out.println(victimID + " |" + victimClass + " | " + killerID + " | " + killerClass + " has been added to the database.");
                        }
                    }
                }catch(IOException e){
                    System.out.println("Error reading log file: " + log.getName());
                    e.printStackTrace();
                }
            }
        }catch(SQLException e){
            System.out.println("Failed to connect to the database: " + e.getMessage());

        }
    }
    public static void playerSuicide(Pattern pattern, File logDirectory){
        try{
            Connection conn = DBconn.getConnection();
            Statement stmt1 = conn.createStatement();
            stmt1.execute("USE scpdb");
            System.out.println("Connected!");
            File[] logs = logDirectory.listFiles();
            for(File log: logs){
                try(BufferedReader reader = new BufferedReader(new FileReader(log))){
                    String line;
                    while((line = reader.readLine()) != null){
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.find()){
                            String playerID = matcher.group(1);
                            String playerClass = matcher.group(2);
                            if(!matcher.group(2).equals("None")){
                                PreparedStatement stmt = conn.prepareStatement("INSERT INTO suicide_event (player_id, player_class) VALUES (?, ?)");
                                stmt.setString(1, playerID);
                                stmt.setString(2, playerClass);

                                stmt.executeUpdate();
                                stmt.close();
                                System.out.println(playerID + " |" + playerClass + " has been added to the database,");
                            }

                        }
                    }
                }catch(IOException e){
                    System.out.println("Error reading log file: " + log.getName());
                    e.printStackTrace();
                }
            }

        }catch(SQLException e){
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }




    }
}
