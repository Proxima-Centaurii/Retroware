
package retroware;

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
import javax.sql.DataSource;
import retroware.datacontainers.CategoryRecord;
import retroware.datacontainers.GameRecord;

@ManagedBean(name = "homeLists")
@ViewScoped
public class HomeView implements Serializable{
   
    @Resource(name = "jdbc/retroware2")
   DataSource data_source;

   private ArrayList<GameRecord> featured_games;
   private ArrayList<GameRecord> popular_games;
   private ArrayList<GameRecord> all_games;
   
   private int selected_game_index;
   private int list_id;

   private String search_string;
   
   public HomeView() {   
       featured_games = new ArrayList<GameRecord>();
       popular_games = new ArrayList<GameRecord>();
       all_games = new ArrayList<GameRecord>();
       
       search_string = "";
   }
   
   //GETTERS
   
   public String getSearch_string(){
       return search_string;
   }
   
   public ArrayList<GameRecord> getFeatured_games() throws SQLException{
       if(featured_games.size() != 0)
           return featured_games;
       
       getListData(featured_games, "SELECT * FROM FEATURED_GAMES FETCH FIRST 100 ROWS ONLY");
       
       return featured_games;
   }
   
   public ArrayList<GameRecord> getPopular_games() throws SQLException{
       if(popular_games.size() != 0)
           return popular_games;
       
       getListData(popular_games, "SELECT * FROM POPULAR_GAMES FETCH FIRST 100 ROWS ONLY");
       
       return popular_games;
   }
   
   public ArrayList<GameRecord> getAll_games() throws SQLException{
       if(all_games.size() != 0)
           return all_games;
       
       getListData(all_games, "SELECT * FROM GAMES WHERE UNLISTED LIKE 'F' FETCH FIRST 100 ROWS ONLY");
       
       return all_games;
   }
   
   //A helper function for fetching a game list's game data
   private ArrayList<GameRecord> getListData(ArrayList<GameRecord> array, String SQL_string) throws SQLException{
   
       System.out.printf("Fetching data. SQL: \'%s\'\n",SQL_string);
       
       if(data_source == null)
           throw new SQLException("Unable to obtain DataSource object.");
       
       Connection connection = data_source.getConnection("APP", "app");
       if(connection == null)
           throw new SQLException("Unable to connect to database.");

       try(
             PreparedStatement sql_statement = connection.prepareStatement(SQL_string);
             ResultSet rs = sql_statement.executeQuery();
       ){
           
           //Iterate through results
           while(rs.next()){
               int game_id = rs.getInt("GAME_ID");
               String title = rs.getString("TITLE");
               String description = rs.getString("DESCRIPTION");
               long play_count = rs.getLong("play_count");
               short rating = rs.getShort("RATING");
               char unlisted = rs.getString("UNLISTED").charAt(0);
               String resource_id = rs.getString("RESOURCE_ID");
               Date publish_date = rs.getDate("PUBLISH_DATE");
              
              array.add(new GameRecord(game_id, title, description, play_count, rating, unlisted,resource_id,publish_date));
           }
       }catch(Exception e){
           e.printStackTrace();
       }finally{
           connection.close();
       }
       
       
       
       return array;
   }
   
   
   //SETTERS
   
   public void setSearch_string(String search_string){
       this.search_string = search_string;
   }
   
   public void setSelected_game_index(int selected_game_index){
       this.selected_game_index = selected_game_index;
   }
   
   public void setList_id(int list_id){
       this.list_id = list_id;
   }
   
   //VIEW SPECIFIC FUNCTIONS
   
   /**
    Returns the game that the user clicked on.
    * @return Returns a GameRecord object. This is to be further processed by JSF code.
    */
   private int getSelectedGameId(){
  
       ArrayList<GameRecord> source_list = null;
       
       switch(list_id){
           case 0:
               source_list = featured_games;
               System.out.println("From: 'Featured' list");
               break;
           case 1:
               source_list = popular_games;
               System.out.println("From: 'Popular' list");
               break;
           case 2:
               source_list = all_games;
               System.out.println("From: 'All games' list");
               break;
       }
       
       if(source_list == null){
           System.err.println("Source list could not be determined!");
           return -1;
       }
       
       return source_list.get(selected_game_index).getGameID();
   }
   
   public String enterGame(){
       int target_game = getSelectedGameId();
       
       if(target_game < 0)
           return null;
       
       return "/game_page.xhtml?faces-redirect=true&game="+target_game;
   }

   public String startSearch(){
       return "/categories.xhtml?faces-redirect=true&search="+search_string;
   }
   
      
}
