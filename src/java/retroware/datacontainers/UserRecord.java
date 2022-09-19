package retroware.datacontainers;

import java.io.Serializable;
import java.sql.Date;

public class UserRecord implements Serializable{
    
    private int user_id;
    private String username;
    private String email;
    private String password_hash;
    private byte[] salt;
    private Date join_date;
    private String description;
    private char banned;
    
    //Default constructor
    public UserRecord(){}
    
    public UserRecord(int user_id, String username, String email, String password_hash, byte[] salt, Date join_date, String description, char banned){
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.password_hash = password_hash;
        this.salt = salt;
        this.join_date = join_date;
        this.description = description;
        this.banned = banned;
    }
    
    //GETERS
    public int getUser_id(){return user_id;}
    public String getUsername(){return username;}
    public String getEmail(){return email;}
    public String getPassword_hash(){return password_hash;}
    public byte[] getSalt(){return salt;}
    public Date getJoin_date(){return join_date;}
    public String getDescription(){return description;}
    public char getBanned(){return banned;}
    
    public boolean isBanned(){return banned == 'T' ? true : false;}
    public String getJoinDateDisplay(){
        if(join_date == null)
            return "No date provided";
        
        return join_date.toString();
    }
    
    //Setters
    public void setEmail(String email){this.email = email;}
    public void setPassword_hash(String password_hash){this.password_hash = password_hash;}
    public void setSalt(byte[] salt){this.salt = salt;}
    public void setDescription(String description){this.description = description;}
    public void setBanned(char banned){this.banned = banned;}

    
    @Override
    public String toString(){
        return String.format("(%d: %s, %s, %s, #pwd: %s, #salt: %s, %s)", 
                user_id, username, email,join_date.toString(), password_hash, salt, isBanned()?"BANNED":"NOT BANNED");
    }
    
}//end of class
