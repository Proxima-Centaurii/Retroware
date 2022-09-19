
package retroware;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.sql.DataSource;
import retroware.datacontainers.CategoryRecord;
import retroware.datacontainers.GameRecord;


@ManagedBean (name = "categoryLists")
@ViewScoped
public class CategoriesView implements Serializable{

    @Resource(name = "jdbc/retroware2")
    DataSource data_source;
    
    //Searching
    private String search_string;
    
    //Categories
    private ArrayList<CategoryRecord> categories;
    private int selected_category;
    
    //Sorting
    private String sort_criteria;
    private HashMap<String,String> sorting_criterias;
    
    //In page game list
    private ArrayList<GameRecord> results_list;
    private int selected_game_index;
    
    public CategoriesView(){
        categories = new ArrayList<CategoryRecord>();
        results_list = new ArrayList<GameRecord>();
        selected_category = 0;
        
        sort_criteria = "Highest rating";
        
        sorting_criterias = new HashMap<>();
        sorting_criterias.put("Highest rating", "RATING DESC");
        sorting_criterias.put("Lowest rating", "RATING ASC");
        sorting_criterias.put("Most played", "PLAY_COUNT DESC");
        sorting_criterias.put("Least played", "PLAY_COUNT ASC");
        
        search_string = "";
        
        selected_game_index = -1;
    }
    
    //GETTERS
    
    public ArrayList<GameRecord> getResults_list() throws SQLException{
        if(results_list.size() != 0)
            return results_list;
        
        getResults(results_list);
        
        return results_list;
    }

    public ArrayList<CategoryRecord> getCategories() throws SQLException{
       //Update only once
        if(categories.size() != 0)
           return categories;
       
       if(data_source == null)
           throw new SQLException("Unable to obtain DataSource object");
       
       Connection connection = data_source.getConnection("APP", "app");
       if(connection == null)
           throw new SQLException("Unable to connect to database");
       
       categories.add(new CategoryRecord(0,"All games"));
       
       //Try-catch with closeable resources specified
       try(
               PreparedStatement sql_statement = connection.prepareStatement("SELECT * FROM CATEGORIES");
               ResultSet rs = sql_statement.executeQuery();
          ){
          
           //Parse data
           while(rs.next()){
               int category_id = rs.getInt(1);
               String title = rs.getString(2);
               
               categories.add(new CategoryRecord(category_id, title));
           }  
       }catch(Exception e){
           e.printStackTrace();
       }finally{
           connection.close();
       }
       
       return categories;
   }
    
    public int getSelected_category(){return selected_category;}
    
    public String getCurrentCategoryName(){
        return categories.get(selected_category).getTitle();
    }
    
    public String getSort_criteria(){return sort_criteria;}
   
    public Set<String> getCriteriaList(){
        return sorting_criterias.keySet();
    }
    
    public String getSearch_string(){
        return search_string;
    }
    
    private ArrayList<GameRecord> getResults(ArrayList<GameRecord> array) throws SQLException{
        
        if(data_source == null)
            throw new SQLException("Unable to obtain DataSource.");
        
        Connection connection = data_source.getConnection("APP", "app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
            String sql_string = constructSQLString();
            
            System.out.printf("Executing query \'%s\'\n",sql_string);
            
        try(
                PreparedStatement sql_statement = connection.prepareStatement(sql_string);
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
               
              array.add(new GameRecord(game_id, title, description, play_count, rating, unlisted,resource_id, publish_date));
           }
           
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        
        return array;
    }
    
    //SETTERS
    
    public void setSelected_category(int selected_category) throws SQLException{
        this.selected_category = selected_category;
        
        if(selected_category != 0)
            System.out.printf("Selected category: %s\n", categories.get(selected_category).getTitle());
        else
            System.out.println("Selected category: All games");
        
        results_list.clear();
        getResults(results_list);
    }
    
    public void setSort_criteria(String sort_criteria) throws SQLException{
        this.sort_criteria = sort_criteria;
        
        results_list.clear();
        getResults(results_list);
        System.out.println("Sorting list by "+ sort_criteria);
    }
    
    public void setSearch_string(String search_string){
        this.search_string = search_string.toLowerCase();
        System.out.println("Entered: "+search_string);
    }
    
    public void setSelected_game_index(int selected_game_index){
        this.selected_game_index = selected_game_index;
    }
    
    
    //VIEW SPECIFIC FUNCTIONS
    
    public void startSearch() throws SQLException{
        results_list.clear();
        
        getResults(results_list);    
    }
    
    public String enterGame(){
       if(selected_game_index < 0)
           return null;
       
       return "/game_page.xhtml?faces-redirect=true&game="+results_list.get(selected_game_index).getGameID();
   }
    
    /*The use of string builder will result in less readable code but higher performance.*/
    private String constructSQLString(){
        
        //Tokenising search input to help with searching games
        String[] search_tokens = null;
        
        if(!search_string.isEmpty())
            search_tokens = search_string.split(" ");

        //Building SQL
        String sql = "SELECT * FROM GAMES";
        
        if(selected_category != 0){
            sql += " RIGHT JOIN GAME_CATEGORIES ON GAMES.GAME_ID = GAME_CATEGORIES.GAME_ID " +
                    "WHERE GAME_CATEGORIES.CATEGORY_ID = " + selected_category + " AND";
        }
        else
            sql += " WHERE";
        
        if(search_tokens != null){
            StringBuilder sb = new StringBuilder();

            sb.append(" ( LOWER(TITLE) LIKE '%");
            
            //Search will be limited to the first 5 tokens(words)
            int len = search_tokens.length;
            len = len > 5 ? 5 : len;
            
            for(int i=0; i < len; i++){
                sb.append(search_tokens[i]);
                sb.append('%');
            }
            
            for(int i=0; i< len; i++){
                sb.append("%' OR LOWER(TITLE) LIKE '%");
                sb.append(search_tokens[i]);
            }
            
            //Close check value and substatement
            sb.append("%') AND");
            
            sql += sb.toString();
        }
        
        sql += " UNLISTED LIKE 'F' ORDER BY " + sorting_criterias.get(sort_criteria);
        sql += " FETCH FIRST 100 ROWS ONLY";
        return sql;
    }

    
    
    
    
    
}//End of class
