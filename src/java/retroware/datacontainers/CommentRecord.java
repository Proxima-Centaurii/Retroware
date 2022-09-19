package retroware.datacontainers;

import java.io.Serializable;

public class CommentRecord implements Serializable{
    
    private int user_id, game_id;
    private String username, comment;
    
    public CommentRecord(int user_id, int game_id, String username, String comment){
        this.user_id = user_id;
        this.game_id = game_id;
        this.username = username;
        this.comment = comment;
    }
    
    //GETTERS
    
    public int getUser_id(){return user_id;}
    public int getGame_id(){return game_id;}
    public String getUsername(){return username;}
    public String getComment(){return comment;}
    
    //SETTERS
    
    public void setUser_id(int user_id){
        this.user_id = user_id;
    }
    public void setGame_id(int game_id){
        this.game_id = game_id;
    }
    public void setUsername(String username){this.username = username;}
    public void setComment(String comment){this.comment = comment;}
    
}//End of class
