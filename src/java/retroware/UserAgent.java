
package retroware;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.sql.DataSource;
import retroware.datacontainers.GameRecord;
import retroware.datacontainers.UserRecord;
import retroware.security.SHA1PasswordHash;

@ManagedBean (name = "userAgent")
@SessionScoped
public class UserAgent implements Serializable{

    @Resource(name = "jdbc/retroware2")
    DataSource data_source;
    
    transient SHA1PasswordHash password_hasher;
    
    private int user_id;
    private String login_username;
    private String login_password;
    private boolean failed_login;
    private String fail_reason;
    
    private GameRecord selected_game;
    private HashMap<Integer,Integer> game_like_status;
    
    public UserAgent(){
        password_hasher = new SHA1PasswordHash();
        failed_login = false;
        fail_reason = "NULL";
        user_id = -1;
        
        game_like_status = new HashMap<Integer,Integer>();
    }
    
    /*GETTERS*/
  
    public GameRecord getSelected_game(){return selected_game;}
    public String getLogin_username(){return login_username;}
    public String getLogin_password(){return login_password;}
    public String getFail_reason(){return fail_reason;}
    public int getUser_id(){return user_id;}
    
    /*SETTERS*/
   
    public void setSelected_game(GameRecord selected_game){this.selected_game = selected_game;}
    public void setLogin_username(String login_username){this.login_username = login_username;}
    public void setLogin_password(String login_password){this.login_password = login_password;}
    
    
    /*BEAN SPECIFIC FUNCTIONS*/
    
    public int getLikeStatus(int target_game){
        //The user must be logged in to get rating
        if(!isLoggedIn())
            return 0;
        
        Integer rating = game_like_status.get(target_game);
        
        //If there are no records or the game id can't be found then rating will be null
        if(rating == null){
            System.out.println("NO RATING");
            return 0; //Default value (no rating)
        }
        
        return rating.intValue();
    }
    
    public boolean isGameLiked(int target_game){
        //The user must be logged in to get rating
        if(!isLoggedIn())
            return false;
        
        Integer rating = game_like_status.get(target_game);
        
        //If there are no records or the game id can't be found then rating will be null
        if(rating == null){
            System.out.println("NO RATING");
            return false; //Default value (no rating)
        }
        
        System.out.println("GAME IS LIKED");
        
        return rating.intValue() == 1;
    }
    
    public boolean isGameDisliked(int target_game){
        //The user must be logged in to get rating
        if(!isLoggedIn())
            return false;
        
        Integer rating = game_like_status.get(target_game);
        
        //If there are no records or the game id can't be found then rating will be null
        if(rating == null){
            System.out.println("NO RATING");
            return false; //Default value (no rating)
        }
        
        return rating.intValue() == -1;
    }
    
    public String setLikeStatus(int target_game, int rating) throws SQLException{
        //The user must be logged in to set a rating
        if(!isLoggedIn())
            return "/login.xhtml?faces-redirect=true";
        
        if(data_source == null)
            throw new SQLException("Could not obtain DataSource object.");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        String sql_string = "";
        
        //Check if there is an existing record to be updated or create a new one
        if(game_like_status.containsKey(target_game)){
            sql_string = "UPDATE GAME_RATINGS SET RATING = ? WHERE GAME_ID = ? AND USER_ID = ?";
            
            int final_rating = game_like_status.get(target_game) == rating ? 0 : rating;
            int previous_rating = game_like_status.get(target_game);
            
            /* -1 = dislike; 0 = no rating; 1 = like
            The amounts by which the counts will increase/decrease
             Toggling the rating or switching to another rating was taken into account 
             Previous ratings must be decremented and new rating must be incremented. If the
            rating was toggled (eg. Was liked but then the user clicked like again) then only decrement
            the previous rating without incrementing anything else.
            */
            int delta_like = previous_rating == 1 ? -1 : ( rating == 1 ? 1 : 0 );
            int delta_dislike = previous_rating == -1 ? -1 : ( rating == -1 ? 1 : 0);
            
            try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
                sql_statement.setInt(1, final_rating);
                sql_statement.setInt(2, target_game);
                sql_statement.setInt(3, user_id);
                
                sql_statement.execute();
                
                updateRatingCount(connection, target_game, delta_like, delta_dislike);
                
                game_like_status.put(target_game, final_rating);
            }catch(SQLException e){
                e.printStackTrace();
                System.out.printf("Could not update game rating. (target: %d, rating: %d)\n", target_game, rating);
            }
            //Finally block is ensured to always execute in case of an exception
            finally{
                connection.close();
            }
        }
        //A new record must be inserted
        else{
            sql_string = "INSERT INTO GAME_RATINGS (GAME_ID, USER_ID, RATING) VALUES (?,?,?)";
            
            try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
                sql_statement.setInt(1, target_game);
                sql_statement.setInt(2, user_id);
                sql_statement.setInt(3, rating);
                
                sql_statement.execute();
                
                if(rating == 1)
                    updateRatingCount(connection, target_game, 1, 0);
                else
                    updateRatingCount(connection, target_game, 0, 1);
                
                game_like_status.put(target_game, rating);
            }catch(SQLException e){
                e.printStackTrace();
                System.out.printf("Could not insert new game rating record into database. (game_id: %d, user_id: %d, rating: %d)", target_game,user_id,rating);
            }finally{
                connection.close();
            }
        }
        
        return null;
    }
    
    private void updateRatingCount(int game_id, int delta_like, int delta_dislike) throws SQLException{
        if(data_source == null)
            throw new SQLException("Unable to obtain DataSource object.");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        updateRatingCount(connection, game_id, delta_like, delta_dislike);
        
        connection.close();
    }
    
    private void updateRatingCount(Connection connection, int game_id, int delta_like, int delta_dislike) throws SQLException{
        
        int likes_count, dislikes_count;
        
         //Get record
        try(PreparedStatement sql_statement = connection.prepareStatement("SELECT LIKES_COUNT, DISLIKES_COUNT FROM LIKES_COUNT WHERE GAME_ID = ?");)
        {
            sql_statement.setInt(1, game_id);
            
            try(ResultSet rs = sql_statement.executeQuery();){
                if(rs.next()){
                    likes_count = rs.getInt("LIKES_COUNT");
                    dislikes_count = rs.getInt("DISLIKES_COUNT");
                }
                else{
                    System.out.printf("Could not get data for game_id = %d\n", game_id);
                    return;
                }
            }
        }
        
        //Update counts
        likes_count += delta_like;
        dislikes_count += delta_dislike;
        
        try(PreparedStatement sql_statement = connection.prepareStatement("UPDATE LIKES_COUNT SET LIKES_COUNT = ?, DISLIKES_COUNT = ? WHERE GAME_ID = ?");)
        {
            sql_statement.setInt(1, likes_count);
            sql_statement.setInt(2, dislikes_count);
            sql_statement.setInt(3, game_id);
            
            sql_statement.execute();
        }
    }
    
    //Overloaded function, gets user ratings but also creates a new connection to be used
    private void getUserRatings() throws SQLException{
        if(data_source == null)
            throw new SQLException("Unable to obtain DataSource object.");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        getUserRatings(connection);
        
        connection.close();
    }
    
    private void getUserRatings(Connection connection) throws SQLException{
        
        try(PreparedStatement sql_statement = connection.prepareStatement("SELECT GAME_ID, RATING FROM GAME_RATINGS WHERE USER_ID = ?");){
              
            sql_statement.setInt(1, user_id);
            
            try(ResultSet rs = sql_statement.executeQuery();){
                
                while(rs.next()){
                    int game_id = rs.getInt("GAME_ID");
                    int rating = rs.getInt("RATING");
                    
                    game_like_status.put(game_id, rating);
                }
                
            }//End try
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        
        System.out.printf("Game liked status:\n%s\n", game_like_status.toString());
    }
    
    public boolean isLoginFailed(){
        System.out.printf("Login %s.\n", failed_login ? "failed" : "successful");
        return failed_login;
    }
    
    public boolean isLoggedIn(){
        return (user_id != -1 ? true: false);
    }
    
    public String logIn() throws SQLException{
        System.out.printf("Submitted: %s - %s\n", login_username, login_password);
        
        if(data_source == null)
            throw new SQLException("Unable to obtain DataSource object.");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        try(PreparedStatement sql_statement = connection.prepareStatement("SELECT * FROM APP_USERS WHERE USERNAME = ?");){
              
            sql_statement.setString(1, login_username);
            
            try(ResultSet rs = sql_statement.executeQuery();){
                
                //If no matching result was found then the username does not exist
                if(!rs.next()){
                    fail_reason = "Invalid username or password.";
                    failed_login = true;
                }
                else{
                    byte[] salt = rs.getBytes("SALT");
               
                    String pwd = password_hasher.getSecurePassword(login_password, salt);
                    String password_hash = rs.getString("PASSWORD_HASH");
               
                    if(!pwd.equals(password_hash)){
                        fail_reason = "Invalid username or password.";
                        failed_login = true;
                    }
                    else{
                        System.out.println("Successfully logged in!");
                        failed_login = false;
                   
                        char banned = rs.getString("BANNED").charAt(0);
                        if(banned == 'F'){
                            user_id = rs.getInt("USER_ID");
                    }
                        else{
                            fail_reason = "Your account has been banned.";
                            failed_login = true;
                        }
                    }
                   
                }
                
                getUserRatings(connection);
            }//End try
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        
        //Reset form entries
        login_password = "";
        login_username = "";
        
        if(failed_login)
            return null;
        
        
        System.out.println("Successfuly logged in!");
        return "home.xhtml?faces-redirect=true";
    }
    
    public String logOut(){
        user_id = -1;
        
         System.out.println("Successfuly logged out!");
        
        return "home.xhtml?faces-redirect=true";
    }
    
    //Takes the user to their own profile page
    public String toUserProfile(){
        return "/user_page.xhtml?faces-redirect=true&uid="+user_id;
    }
    
}//End of class
