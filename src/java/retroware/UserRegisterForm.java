
package retroware;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import org.jboss.weld.context.RequestContext;
import retroware.security.SHA1PasswordHash;

@ManagedBean (name = "registerForm")
@ViewScoped

public class UserRegisterForm implements Serializable{
    
    @Resource(name = "jdbc/retroware2")
    DataSource data_source;
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){1,23}[a-zA-Z0-9]$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");
    
    private String email;
    private String username;
    private String password;
    private String password_confirm;
    
    public UserRegisterForm(){
        email = null;
        username = null;
        password = null;
        password_confirm = null;
    }
    
    /*GETTERS*/
    public String getEmail(){return email;}
    public String getUsername(){return username;}
    public String getPassword(){return password;}
    public String getPassword_confirm(){return password_confirm;}
    
    /*SETTERS*/
    
    public void setEmail(String email){
        System.out.println("Changed email: " + email);
        this.email = email;
    }
    
    public void setUsername(String username){
        System.out.println("Changed user name: " + username);
        this.username = username;
    }
    
    public void setPassword(String password){
        System.out.println("Changed password: " + password);
        this.password = password;
    }
    
    public void setPassword_confirm(String password_confirm){
        System.out.println("Changed pwd confirm: " + password_confirm);
        this.password_confirm = password_confirm;
    }
    
    /*VALIDATORS*/
   
    private boolean isValidEmail(){return (email != null && email.length() > 0);}
    private boolean isValidUsername(){return (username != null && username.length() > 0);}
    private boolean isValidPassword(){return (password != null && password.length() >0);}
    private boolean isValidPasswordConfirm(){return (password_confirm != null && password_confirm.length() > 0);}
    
    private boolean isUsernameAvailable(String username) throws SQLException{
        
        if(data_source == null)
            throw new SQLException("Unable to obtain DataSource");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        String sql_string = "SELECT COUNT(USERNAME) AS \"RESULTS\" FROM APP_USERS WHERE USERNAME = ?";
        
        long result_count = -1;
        try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
            
            sql_statement.setString(1, username);
            
            //Execute query
            try(ResultSet rs = sql_statement.executeQuery();){
                if(rs.next())
                    result_count = rs.getObject("RESULTS", Long.class);
            }  
            
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        
        System.out.printf("Validating: %sn", username);
        
        return (result_count == 0 ? true : false);
    }
    
    private boolean isEmailAvailable(String email) throws SQLException{
        
        if(data_source == null)
            throw new SQLException("Unable to obtain DataSource");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        String sql_string = "SELECT COUNT(EMAIL) AS \"RESULTS\" FROM APP_USERS WHERE EMAIL = ?";
        
        long result_count = -1;
        try(PreparedStatement sql_statement = connection.prepareStatement(sql_string);){
            
            sql_statement.setString(1, email);
            
            //Execute query
            try(ResultSet rs = sql_statement.executeQuery();){
                if(rs.next())
                    result_count = rs.getLong("RESULTS");
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        
        return (result_count == 0 ? true : false);
    }
    
    /*Check if the email is in the correct format.
    Example: local-part_email@domain.ab.cd 
    NOTE: Accepted characters are -_. in the local part and domain part of the address*/
    public void validateEmail(FacesContext context, UIComponent component, Object value) throws ValidatorException, SQLException{
        System.out.println("Validating email.");
        
        //Invalidate any previous value
        email = null;
        
        String email = value.toString();
        FacesMessage message = new FacesMessage();
        
        //Check if there is any input
        if(email.isEmpty()){
            message.setSummary("No email entered.");
            message.setDetail("You must enter an email address.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            throw new ValidatorException(message);
        }
        
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        
        //If no match was found, add an error message and trigger an exception to prevent input from being set.
        if(!matcher.matches()){
            message.setSummary("Invalid email format.");
            message.setDetail("Invalid email format. Example: email@domain.co.uk");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            throw new ValidatorException(message);
        }
        
        //Email must not be already in use
        if(!isEmailAvailable(email)){
            message.setSummary("Email unavailable.");
            message.setDetail("This email is already associated with an account.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            throw new ValidatorException(message);
        }
    }
    
    /*Validates the username. The criteria is:
     -must be at least 3 characters
     -the first character must be a letter
     -must be composed of letters and numbers or the following characters: -_. 
    */
    public void validateUsername(FacesContext context, UIComponent component, Object value) throws ValidatorException, SQLException{
        System.out.println("Validating username.");
        
        //Invalidate previous username value
        username = null;
        
        String username = value.toString();
        FacesMessage message = new FacesMessage();
        
        //Check if there is any input
        if(username.isEmpty()){
            message.setSummary("No username entered.");
            message.setDetail("You must enter a username.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            throw new ValidatorException(message);
        }
        
        //Check if the username has the minimum length
        if(username.length() < 3){
            message.setSummary("Username too short.");
            message.setDetail("The user name must be at least 3 characters long.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            
            throw new ValidatorException(message);
        }
        
        Matcher matcher = USERNAME_PATTERN.matcher(username);
        
        //If no match was found, add an error message and trigger an exception to prevent input from being set.
        if(!matcher.matches()){
            message.setSummary("Invalid username.");
            message.setDetail("Username contains invalid characters.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            
            throw new ValidatorException(message);
        }
        
        //Username must be not be already in use.
        if(!isUsernameAvailable(username)){
            message.setSummary("Username unavailable.");
            message.setDetail("This username has already been taken.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            throw new ValidatorException(message);
        }
    }
    
    /*Validates the password. Password must:
    -be at least 8 characters long
    -have a mix of uppercase and lowercase letters
    -have at least one digit*/
    public void validatePassword(FacesContext context, UIComponent component, Object value) throws ValidatorException{
        System.out.println("Validating password.");
        
        //Invalidate previous value
        this.password = null;
        
        String password = value.toString();
        FacesMessage message = new FacesMessage();
        
        //Perform checks on the password. If a check fails add an error message and trigger an exception to prevent input from being set.
        
        //Check if there is any input
        if(value == null){
            message.setSummary("No password entered.");
            message.setDetail("You must enter a password!");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            throw new ValidatorException(message);
        }
        
        //Check if the password is at least 8 characters long
        if(password.length() < 8){
            message.setSummary("The password is too short!");
            message.setDetail("The password must be at least 8 characters long!");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            
            throw new ValidatorException(message);
        }
        
        //Check if the password meets the requirements
        boolean hasLowercase = false;
        boolean hasUppercase = false;
        boolean hasDigits = false;
        
        for(char c : password.toCharArray()){
            hasLowercase = (hasLowercase || Character.isLowerCase(c));
            hasUppercase = (hasUppercase || Character.isUpperCase(c));
            hasDigits = (hasDigits || Character.isDigit(c));
            
            if(Character.isLowerCase(c))
                hasLowercase = true;
            else if(Character.isUpperCase(c))
                hasUppercase = true;
            else if(Character.isDigit(c))
                hasDigits = true;
            
            if(hasLowercase && hasUppercase && hasDigits)
                return;
        }
        
        message.setSummary("Password is too weak.");
        message.setDetail("Password does not meet the specified criteria.");
        message.setSeverity(FacesMessage.SEVERITY_ERROR);
        
        

        throw new ValidatorException(message);
    }
    
    /*Must match the value of the first password field.*/
    public void validateConfirmPassword(FacesContext context, UIComponent component, Object value) throws ValidatorException{
        
        //Check if there is any input
        if(value == null){
            FacesMessage message = new FacesMessage("No password confirmation.", "You must confirm your password!");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            throw new ValidatorException(message);
        }
        
        //Invalidate previous value
        password_confirm = null;
        
        if(password == null || !password.equals(value.toString())){
            FacesMessage message = new FacesMessage("Password confirmation failed.", "Passwords do not match. Please check your password.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            System.out.printf("\'%s\' does not match \'%s\'",password, password_confirm);
            
            
            throw new ValidatorException(message);
        }
    }
    
    /*Checks if all the entries of the form are valid and proceeds to the next step if they are.*/
    public String submitForm(){ 
        
        //Check if all entries are valid
        if(!(isValidEmail() && isValidUsername() && isValidPassword() && isValidPasswordConfirm())){
            System.out.println("Form is invalid!");
            return null;
        }
        
        try{
            registerUser();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        
        //Redirect to login page
        return "/login.xhtml?faces-redirect=true";
    }
    
    private void registerUser() throws SQLException, IOException{
        
        /*PREPARING DATA*/
        
        //Hashing user password for safe storage
        SHA1PasswordHash hash = new SHA1PasswordHash();
        byte[] salt = null;
        try{
            salt = hash.generateSalt();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        String password_hash = hash.getSecurePassword(password, salt);
        
        /*Assigning a default profile picture into the database*/
        
        //Getting the absolute path to the resources folder
        String absolute_path = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/resources/");
        
        //Filling in path to default profile picture file
        absolute_path = absolute_path + "/media/website_default/default_profile.jpg";
        
        //Reading byte data
        byte[] image_data = Files.readAllBytes(Paths.get(absolute_path));
        
        /*DATABASE OPERATIONS*/
        
        //Inserting user data into database
        if(data_source == null)
            throw new SQLException("Unable to obtain DataSource");
        
        Connection connection = data_source.getConnection("APP","app");
        if(connection == null)
            throw new SQLException("Unable to connect to database.");
        
        String new_user_sql = "INSERT INTO APP_USERS (USERNAME, EMAIL, PASSWORD_HASH, SALT, JOIN_DATE, DESCRIPTION, BANNED) " +
                "VALUES (?,?,?,?,?,'Hello! I am a new user!','F')";
        
        String get_user_id = "SELECT USER_ID FROM APP_USERS WHERE USERNAME = ?";
        
        String default_image_sql = "INSERT INTO USER_PROFILE_PICTURES (USER_ID, IMAGE, IMAGE_FILE_NAME) VALUES (?,?,?)";
        
        //Register user
        try(PreparedStatement sql_statement = connection.prepareStatement(new_user_sql);){
            sql_statement.setString(1, username);
            sql_statement.setString(2, email);
            sql_statement.setString(3, password_hash);
            sql_statement.setBytes(4, salt);
            sql_statement.setDate(5, new Date( new java.util.Date().getTime()));
            
            sql_statement.execute();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        //Retrieve the user's id
        int user_id = -1;
        try(PreparedStatement sql_statement = connection.prepareStatement(get_user_id);){
            sql_statement.setString(1, username);
            
            try(ResultSet rs = sql_statement.executeQuery();){
                if(rs.next())
                    user_id = rs.getInt("USER_ID");
                else
                    throw new SQLException("User not found, registration likely failed.");
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        //Safety check
        if(user_id == -1){
            System.out.println("User id not updated.");
            return;
        }
        
        //Set default picture
        try(PreparedStatement sql_statement = connection.prepareStatement(default_image_sql);){
            sql_statement.setInt(1, user_id);
            sql_statement.setBytes(2, image_data);
            sql_statement.setString(3, username+".jpg");
        
            sql_statement.execute();
        }
        
        //PreparedStatement sql_statement = connection.prepareStatement(new_user_sql); 
       
    }
    
    public void test() throws IOException{
        String path = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/resources/");
        path = path+"/test_file.txt";
        Path p = Paths.get(path);
        byte[] data = Files.readAllBytes(p);
        
        System.out.println("PATH: "+ path);
        
        if(data == null){
            System.out.println("File not found");
            return;
        }
        
        System.out.printf("File contents: %s\n", new String(data, StandardCharsets.UTF_8));
    }
    
    @Override
    public String toString() {
        return String.format("\nEmail: %s\nUsername: %s\nPassword: %s\nConfirm: %s", email, username, password, password_confirm);
    }
    
}//End of class
