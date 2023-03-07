import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBconn {
    private static Connection conn;
    private static String url;
    private static String user;
    private static String password;

    static{
        try{
            Properties prop = new Properties();
            InputStream input = serverLogParser.class.getClassLoader().getResourceAsStream("config.properties");
            prop.load(input);
            url = prop.getProperty("db.url");
            user =  prop.getProperty("db.username");
            password = prop.getProperty("db.password");

            conn = DriverManager.getConnection(url, user, password);

        }catch(IOException | SQLException ex){
            ex.printStackTrace();
        }
    }
    public static Connection getConnection(){
        return conn;
    }

}
