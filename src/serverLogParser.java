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
        Pattern patternPlayer = Pattern.compile("Nickname of \\d+@steam is now \\b(\\w+(?:[-\\s']\\w+)*)\\b.");
        //Regular expression pattern that matches the event where a player kills another player. This also groups the victim name and class, and killer name and class.
        // (.*\\b(\\w+(?:-|\\s+\\w+)*) \\(\\d+@steam\\), playing as (\\w+(?:-|\\s+\\w+)*) has been killed by (\\w+(?:-|\\s+\\w+)*) \\(\\d+@steam\\) playing as: (\\w+(?:-|\\s+\\w+)*). Specific death reason:.*)
        Pattern patternKill = Pattern.compile("\\b(\\w+(?:[-\\s']\\w+)*)\\b \\(\\d+@steam\\), playing as \\b(\\w+(?:[-\\s']\\w+)*)\\b, has been killed by \\b(\\w+(?:[-\\s']\\w+)*)\\b \\(\\d+@steam\\) playing as: \\b(\\w+(?:[-\\s']\\w+)*)\\b. Specific death reason:.*");
        //Regular expression pattern that matches the event where a player suicides. This also groups that players name and class.  \b(\w+(?:[-\s]*\w+)*)
        Pattern patternSuicide = Pattern.compile("\\b(\\w+(?:[-\\s']\\w+)*)\\b \\(\\d+@steam\\), playing as \\b(\\w+(?:[-\\s']\\w+)*)\\b, has died. Specific death reason:.*");
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
                            String playerName = matcher.group(1);
                            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM players WHERE player_name = ?");
                            stmt.setString(1, playerName);
                            ResultSet rs = stmt.executeQuery();
                            rs.next();
                            int count = rs.getInt(1);
                            if(count  > 0){
                                System.out.println(playerName + " already exists  in the database.");
                            }else{
                                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO players(player_name) VALUES (?)");
                                insertStmt.setString(1, playerName);
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
                            String victimName = matcher.group(1);
                            String victimClass = matcher.group(2);
                            String killerName = matcher.group(3);
                            String killerClass = matcher.group(4);
                            PreparedStatement stmt = conn.prepareStatement("INSERT INTO kill_event (victim_name, victim_class, killer_name, killer_class) VALUES (?, ?, ?, ?)");
                            stmt.setString(1, victimName);
                            stmt.setString(2, victimClass);
                            stmt.setString(3, killerName);
                            stmt.setString(4, killerClass);
                            stmt.executeUpdate();
                            System.out.println(victimName + " |" + victimClass + " | " + killerName + " | " + killerClass + " has been added to the database,");
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
                            String playerName = matcher.group(1);
                            String playerClass = matcher.group(2);
                            if(!matcher.group(2).equals("None")){
                                PreparedStatement stmt = conn.prepareStatement("INSERT INTO suicide_event (player_name, player_class) VALUES (?, ?)");
                                stmt.setString(1, playerName);
                                stmt.setString(2, playerClass);

                                stmt.executeUpdate();
                                System.out.println(playerName + " |" + playerClass + " has been added to the database,");
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
