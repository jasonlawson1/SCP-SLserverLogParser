# SCP-SLserverLogParser
This project reads text files in the serverlog folder from the game SCP: Secret Laboratory. It takes 3 categories of data, player name, kill event, and environmental death, and adds them to a mySQL database.

I used a configuration file to specify my database login credentials.
If you want to do it the way I did, create a file, in my case I called it config.properties.

db.username= 
db.password =
db.url=

add the above text and fill in your username, password, and JDBC connection string.
The DBconn class will take over from there.
You'll also have to change the logDR path. in my case its, "C:\Users\jason\AppData\Roaming\SCP Secret Laboratory\ServerLogs\7777" but this will probably be different in your case.


This is my first project so thanks for taking a look at it!
