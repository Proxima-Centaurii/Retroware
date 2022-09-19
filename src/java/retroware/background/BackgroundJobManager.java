
package retroware.background;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.sql.DataSource;

@Singleton
public class BackgroundJobManager {
    
    
    @Schedule(hour="*", minute="*/1", second="0", persistent=false)
    public void updateRatings() throws SQLException{
        System.out.println("Updating ratings...");

        Connection connection = DriverManager.getConnection("jdbc:derby://localhost:1527/retroware2", "APP", "app");
        
        System.out.println("Connection enstablished.");
        
        try{
            startUpdatingRatings(connection);
        }catch(SQLException e){
            e.printStackTrace();
        }
        finally{
            connection.close();
        }
        
    }
    
    private void startUpdatingRatings(Connection connection) throws SQLException{
        
        //GAME_ID - RATING (0-100)
        HashMap<Integer, Integer> game_ratings = new HashMap<Integer, Integer>();
        HashMap<Integer, LikesCountContainer> rating_count = new HashMap<Integer, LikesCountContainer>();
        
        //Fetch all games
        try(
                PreparedStatement sql_statement = connection.prepareStatement("SELECT GAME_ID, RATING FROM GAMES");
                ResultSet rs = sql_statement.executeQuery();
        ){
            while(rs.next())
                game_ratings.put(rs.getInt("GAME_ID"), rs.getInt("RATING"));
            
            //If there are no games present exit
            if(game_ratings.size() == 0)
                return;
            
        }catch(Exception e){
            System.out.println("Failed to load games. (BackgroundJobManager)");
        }
        
        //Fetch all rating counts
        try(
                PreparedStatement sql_statement = connection.prepareStatement("SELECT GAME_ID, LIKES_COUNT, DISLIKES_COUNT FROM LIKES_COUNT");
                ResultSet rs = sql_statement.executeQuery();
        ){
            while(rs.next()){
                LikesCountContainer rating = new LikesCountContainer(rs.getInt("LIKES_COUNT"),rs.getInt("DISLIKES_COUNT"));
                
                //Don't add entries that don't have ratings
                if(rating.likes == 0 && rating.dislikes == 0)
                    continue;
                
                rating_count.put(rs.getInt("GAME_ID"), rating);
            }
            
            //If there are no games present exit
            if(rating_count.size() == 0)
                return;
            
        }catch(Exception e){
            System.out.println("Failed to fetch game ratings. (BackgroundJobManager)");
        }
        
        //Process data
        for(Entry<Integer, LikesCountContainer> entry : rating_count.entrySet()){
           LikesCountContainer x = entry.getValue();
           
           int total = x.likes + x.dislikes;
           int new_rating = (int)((x.likes / (float)total)*100);
           
           game_ratings.put(entry.getKey(), new_rating);
        }
        
        //Update data
        try(
                PreparedStatement sql_statement = connection.prepareStatement("UPDATE GAMES SET RATING = ? WHERE GAME_ID = ?");
        ){
            
            for(Entry<Integer, Integer> entry : game_ratings.entrySet()){
                sql_statement.setInt(1, entry.getValue());
                sql_statement.setInt(2, entry.getKey());
                
                sql_statement.executeUpdate();
                sql_statement.clearParameters();
            }
            
        }catch(Exception e){
            System.out.println("Failed to update game ratings. (BackgroundJobManager)");
        }
        
        System.out.printf("Finished updating %d entries!", rating_count.size());
    }
    
    
    //This is a lightweight class that will hold the likes and dislikes count
    class LikesCountContainer{
        
        public int likes, dislikes;
        
        public LikesCountContainer(){
            likes = dislikes = 0;
        }
        
        public LikesCountContainer(int likes, int dislikes){
            this.likes = likes;
            this.dislikes = dislikes;
        }
    }
    
    
}//end of class
