
package retroware;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.sql.DataSource;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import retroware.datacontainers.GameRecord;
import retroware.datacontainers.UserRecord;

@ManagedBean(name = "userPage")
@ViewScoped
public class UserPageView {
    
    @Resource(name = "jdbc/retroware2")
    DataSource data_source;
    
    //User page details
    private int target_user;
    private boolean invalid_user_id;
    private UserRecord user_data; 
    private ArrayList<GameRecord> liked_games;
    
    private Part profile_upload; //Only used when the user views their own profile
    
    private int selected_game_index;
    
    //Default constructor
    public UserPageView(){
        invalid_user_id = true;
        liked_games = new ArrayList<GameRecord>();
        
        selected_game_index = -1;
    }
    
    //GETTERS
    public int getTarget_user(){return target_user;}
    public UserRecord getUser_data(){return user_data;}
    public String getDescription(){return user_data.getDescription();}
    public Part getProfile_upload(){return profile_upload;}
    public int getSelected_game_index(){return selected_game_index;}
    
    
    //SETTERS
    
    public void setProfile_upload(Part profile_upload){
        this.profile_upload = profile_upload;
    }
    
    public void setTarget_user(int target_user) throws SQLException{
        this.target_user = target_user;
        
        loadUser(target_user);
        
        //Liked games can only be loaded if there is a valid user loaded
        if(!invalid_user_id)
            loadLikedGames();
    }
    public void setDescription(String description) throws SQLException{
        
        if(data_source == null)
            throw new SQLException("Could not obtain DataSource object.");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        try{
            //Prepare SQL statement
            PreparedStatement sql_statement = connection.prepareStatement("UPDATE APP_USERS SET DESCRIPTION = ? WHERE USER_ID = ?");
            sql_statement.setString(1, description);
            sql_statement.setInt(2, target_user);
        
            //Execure query
            sql_statement.execute();
            
            //Update user record
            user_data.setDescription(description);
            
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
            
        System.out.println("Description updated.");
    }
    
    public void setSelected_game_index(int selected_game_index){
        this.selected_game_index = selected_game_index;
    }
  
    //PAGE SPECIFIC FUNCTIONS
    
    private void loadUser(int user_id) throws SQLException{
        
        if(data_source == null)
            throw new SQLException("Could not obtain DataSource object.");
        
        Connection connection = data_source.getConnection("APP", "app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        //Try block with closable resources specified
        try(
                PreparedStatement sql_statement = connection.prepareStatement("SELECT * FROM APP_USERS WHERE USER_ID = ? FETCH FIRST 1 ROWS ONLY");
        ){
            
            sql_statement.setInt(1, target_user);
            
            try(ResultSet rs = sql_statement.executeQuery();){
                if(rs.next()){
                    invalid_user_id = false;
                
                    String username = rs.getString("USERNAME");
                    String email = rs.getString("EMAIL");
                    String password_hash = rs.getString("PASSWORD_HASH");
                    Date join_date = rs.getDate("JOIN_DATE");
                    String description = rs.getString("DESCRIPTION");
                    char banned = rs.getString("BANNED").charAt(0);
                    byte[] salt = rs.getBytes("SALT");
                
                    user_data = new UserRecord(target_user, username, email, password_hash, salt, join_date, description, banned);
                }
                else
                    invalid_user_id = true;
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        
        try{
            //If the user requested could not be found, redirect to home page (temporary solution)
            if(invalid_user_id){
                System.out.printf("Requested user could not be found! (%d)\n", target_user);
                FacesContext.getCurrentInstance().getExternalContext().redirect("home.xhtml");
            }
        }catch(IOException e){
            e.printStackTrace();
        } 
    }
    
    public void deleteAccount() throws SQLException{
        System.out.println("Deleting account...");
        
        if(data_source == null)
            throw new SQLException("Could not obtain DataSource object.");
        
        Connection connection = data_source.getConnection("APP", "app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        //Try block with closable resources specified
        try(
                PreparedStatement sql_statement = connection.prepareStatement("DELETE FROM APP_USERS WHERE USER_ID = ?");
        ){
            
            sql_statement.setInt(1, target_user);
            
            sql_statement.execute();
            
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        
        System.out.println("Account deleted!");
    }
    
    public void processNewProfilePicture() throws IOException{
        if(profile_upload == null)
            return;
        
        //Scaling image
        InputStream input_stream = profile_upload.getInputStream();
        //byte[] image_data = input_stream.readAllBytes();
        
        //Reads the image from the input stream and passes it to a function in turn will return the image
        //resized to the specified size.
        BufferedImage image = resizePicture(ImageIO.read(input_stream), 128, 128);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        
        //Update database
        try(
                Connection connection = data_source.getConnection("APP","app");
                PreparedStatement sql_statement = connection.prepareStatement("UPDATE USER_PROFILE_PICTURES SET IMAGE = ? WHERE USER_ID = ?");
        ){
            sql_statement.setBytes(1, baos.toByteArray());
            sql_statement.setInt(2, target_user);
            
            sql_statement.execute();
            
        }catch(SQLException e){
            e.printStackTrace();
            System.out.println("An error occured while updating new profile picture inside DB.");
        }
        
        
        System.out.println("Image Processed.");
    }
    
    private BufferedImage resizePicture(BufferedImage source,int new_width, int new_height){
        BufferedImage resized = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB);
        
        //This object will redraw the image to its new size
        Graphics2D g2d = resized.createGraphics();
        
        //Setting rendering hints that will have an effect on the quality of the resized image.
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        g2d.drawImage(source, 0, 0, new_width, new_height, null);
        
        //Release system resources that the graphic context uses (IMPORTANT STEP)
        g2d.dispose();
        
        return resized;
    }
    
    public void validateProfilePicture(FacesContext context, UIComponent component, Object value) throws ValidatorException{
        Part file = (Part) value;
        
        String contentType = file.getContentType();
        System.out.printf("File content type: %s\n", contentType);
        
        //Check for the correct file type
        if(!contentType.equals("image/jpeg") && !contentType.equals("image/jpg")){
            throw new ValidatorException(new FacesMessage("The file uploaded is not a JPG file."));
        }

    }
    
    //This function is used to pre-load liked games when a user page is entered.
    //It's important to do this because a message will be displayed instead of the games if the list is empty
    private void loadLikedGames() throws SQLException{
        //Update only once
        if(liked_games.size() != 0)
           return;
       
       if(data_source == null)
           throw new SQLException("Unable to obtain DataSource object");
       
       Connection connection = data_source.getConnection("APP", "app");
       if(connection == null)
           throw new SQLException("Unable to connect to database");
  
       String sql_string = "SELECT * FROM GAMES WHERE GAMES.GAME_ID IN ( SELECT GAME_RATINGS.GAME_ID FROM GAME_RATINGS WHERE GAME_RATINGS.USER_ID = ? AND GAME_RATINGS.RATING = 1 )";
       
       //Try-catch with closeable resources specified
       try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
           sql_statement.setInt(1, target_user);
           
            try(ResultSet rs = sql_statement.executeQuery();){
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
              
                    liked_games.add(new GameRecord(game_id, title, description, play_count, rating, unlisted,resource_id,publish_date));
                }
           } 
       }catch(Exception e){
           e.printStackTrace();
       }finally{
           connection.close();
       }
    }
    
    public ArrayList<GameRecord> getLikedGames(){
       return liked_games;
    }
    
    public boolean hasLikedGames(){
        return liked_games.size() != 0 ? true : false;
    }
    
   public String enterGame(){
       if(selected_game_index < 0)
           return null;
       
       return "/game_page.xhtml?faces-redirect=true&game="+liked_games.get(selected_game_index).getGameID();
   }
    
}//end of class
