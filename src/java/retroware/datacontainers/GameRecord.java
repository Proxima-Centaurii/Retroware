
package retroware.datacontainers;

import java.io.Serializable;
import java.sql.Date;


public class GameRecord implements Serializable{
   
    private int game_id;
    private String title;
    private String description;
    private long play_count;
    private short rating;
    private char unlisted;
    private String resource_id;
    private Date publish_date;
    
    //Default constructor
    public GameRecord(){}
    
    public GameRecord(int game_id, String title, String description, long play_count, short rating, char unlisted, String resource_id, Date publish_date){
        this.game_id = game_id;
        this.title = title;
        this.description = description;
        this.play_count = play_count;
        this.rating = rating;
        this.unlisted = unlisted;
        this.resource_id = resource_id;
        this.publish_date = publish_date;
    }
    
    //GETTERS
    public int getGameID(){return game_id;}
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public long getPlay_count(){return play_count;}
    public short getRating(){return rating;}
    public char getUnlisted(){return unlisted;}
    public String getResource_id(){return resource_id;}
    
    public boolean isUnlisted(){return unlisted == 'T' ? true : false;}
    public String getPublishDateDisplay(){
        if(publish_date == null)
            return "No date provided";
        
        return publish_date.toString();
    }
  
    //SETTERS
    /*These following two functions below might be obsolete*/
    public void setTitle(String title){this.title = title;}
    public void setDescription(String description){this.description = description;}
    
    public void setPlay_count(long play_count){this.play_count = play_count;}
    public void setRating(short rating){this.rating = rating;}
    
    @Override
    public String toString(){
        return String.format("[%d; \"%s\", %s, %d, %d%%, \'%s\', \'%s\', %s]", game_id, title, description, play_count, rating, unlisted, resource_id, getPublishDateDisplay());
    }
    
}//End of class
