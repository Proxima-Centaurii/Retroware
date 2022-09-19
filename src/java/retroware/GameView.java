
package retroware;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;
import retroware.datacontainers.CommentRecord;
import retroware.datacontainers.GameRecord;

@ManagedBean(name = "gamePage")
@ViewScoped
public class GameView implements Serializable{
    
    @Resource(name = "jdbc/retroware2")
    DataSource data_source;
    
    private int selected_game_id;
    private GameRecord selected_game;
    private ArrayList<CommentRecord> comments;
    
    private String user_comment;
    
    public GameView(){
        selected_game_id = -1;
        comments = new ArrayList<CommentRecord>();
    }
    
    //GETTERS
    public int getSelected_game_id(){return selected_game_id;}
    public GameRecord getSelected_game(){return selected_game;}
    public ArrayList<CommentRecord> getComments(){return comments;}
    public String getUser_comment(){return user_comment;}
    
    //SETTERS
    public void setSelected_game_id(int selected_game_id){
        this.selected_game_id = selected_game_id;
    
        try{
            boolean loaded_successfully = loadGame();
            
            //If loading the game fails, redirect to home page.
            if(!loaded_successfully){
                System.out.println("Failed to load the requested game.");
                FacesContext.getCurrentInstance().getExternalContext().redirect("home.xhtml");
            }
            incrementPlayCount();
            selected_game.setPlay_count(selected_game.getPlay_count()+1);
            loadComments();
        }catch(SQLException | IOException e){
            e.printStackTrace();
            System.out.println("Requested game could not be loaded, exception thrown.");
        }
        
    }
    
    public void setUser_comment(String user_comment){
        //Removes trailing space (beggining and end)
        this.user_comment = user_comment.trim();
        System.out.println(user_comment);
    }

    //PAGE SPECIFIC FUNCTIONS
   
    private void incrementPlayCount() throws SQLException{
    
       if(data_source == null)
           throw new SQLException("Unable to obtain DataSource object");
       
       Connection connection = data_source.getConnection("APP", "app");
       if(connection == null)
           throw new SQLException("Unable to connect to database");
  
       long play_count = 0;
       
       //Try-catch with closeable resources specified
       try(PreparedStatement sql_statement = connection.prepareStatement("SELECT PLAY_COUNT FROM GAMES WHERE GAME_ID = ?");){
           sql_statement.setInt(1, selected_game_id);
           
           try(ResultSet rs = sql_statement.executeQuery();){
               if(!rs.next())
                   return;
               else
                   play_count = rs.getLong("PLAY_COUNT");
           }
           
       }catch(Exception e){
           e.printStackTrace();
       }
       
       play_count++;
       
       try(PreparedStatement sql_statement = connection.prepareStatement("UPDATE GAMES SET PLAY_COUNT = ? WHERE GAME_ID = ?");){
           sql_statement.setLong(1, play_count);
           sql_statement.setInt(2, selected_game_id);
           
           sql_statement.execute();
       }catch(Exception e){
           e.printStackTrace();
       }
       
       System.out.println("Play count incremented.");
    }
    
    private void loadComments() throws SQLException{
       if(comments.size() != 0)
           return;
        
       if(data_source == null)
          throw new SQLException("Unable to obtain DataSource object");
       
       Connection connection = data_source.getConnection("APP", "app");
       if(connection == null)
           throw new SQLException("Unable to connect to database");
  
       //Select relevant comment details form comments table (first 100 rows only) and order them in descenting order of their row entry number
       String sql_string = "SELECT * FROM (SELECT GAME_ID, APP_USERS.USER_ID, APP_USERS.USERNAME, COMMENT, ROW_NUMBER() OVER() AS rownum FROM COMMENTS LEFT JOIN APP_USERS "
               + "ON COMMENTS.USER_ID = APP_USERS.USER_ID WHERE GAME_ID = ? FETCH FIRST 100 ROWS ONLY ) as TMP ORDER BY rownum DESC";
       
       //Try-catch with closeable resources specified
       try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
           sql_statement.setInt(1, selected_game_id);
           
            try(ResultSet rs = sql_statement.executeQuery();){
                while(rs.next()){
                    int game_id = rs.getInt("GAME_ID");
                    int user_id = rs.getInt("USER_ID");
                    String username = rs.getString("USERNAME");
                    String comment = rs.getString("COMMENT");
                    
                    comments.add(new CommentRecord(user_id, game_id, username, comment));
                }
           } 
       }catch(Exception e){
           e.printStackTrace();
       }finally{
           connection.close();
       }
       
    }
    
    public boolean hasComments(){
        return comments.size() != 0 ? true : false;
    }
    
    public void postUserComment(int user_id) throws SQLException{
       
       //Don't post empty comments
       if(user_comment.equals("")){
           System.out.println("Empty comment!");
           return;
       }
       
       comments.add(0, new CommentRecord(user_id, selected_game_id, "You" ,user_comment));
       
       if(data_source == null)
           throw new SQLException("Unable to obtain DataSource object");
       
       Connection connection = data_source.getConnection("APP", "app");
       if(connection == null)
           throw new SQLException("Unable to connect to database");
  
       String sql_string = "INSERT INTO COMMENTS (GAME_ID, USER_ID, COMMENT) VALUES (?, ?, ?)";
       
       //Try-catch with closeable resources specified
       try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
           sql_statement.setInt(1, selected_game_id);
           sql_statement.setInt(2, user_id);
           sql_statement.setString(3, user_comment);
        
           sql_statement.execute();
       }catch(Exception e){
           e.printStackTrace();
       }finally{
           connection.close();
       }
       
       System.out.println("Comment posted!");
    }
    
    private boolean loadGame() throws SQLException{
       
       if(data_source == null)
           throw new SQLException("Unable to obtain DataSource object");
       
       Connection connection = data_source.getConnection("APP", "app");
       if(connection == null)
           throw new SQLException("Unable to connect to database");
  
       String sql_string = "SELECT * FROM GAMES WHERE GAME_ID = ?";
       
       //Try-catch with closeable resources specified
       try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
           sql_statement.setInt(1, selected_game_id);
           
            try(ResultSet rs = sql_statement.executeQuery();){
                if(rs.next()){
                    int game_id = rs.getInt("GAME_ID");
                    String title = rs.getString("TITLE");
                    String description = rs.getString("DESCRIPTION");
                    long play_count = rs.getLong("play_count");
                    short rating = rs.getShort("RATING");
                    char unlisted = rs.getString("UNLISTED").charAt(0);
                    String resource_id = rs.getString("RESOURCE_ID");
                    Date publish_date = rs.getDate("PUBLISH_DATE");
              
                    selected_game = new GameRecord(game_id, title, description, play_count, rating, unlisted,resource_id,publish_date);
                }
                else
                    return false;
           } 
       }catch(Exception e){
           e.printStackTrace();
       }finally{
           connection.close();
       }
        
        return true;
    }
    
}//End of class
