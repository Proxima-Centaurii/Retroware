package retroware.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("usernameValidator")
public class UsernameValidator implements Validator {

    //RegEx pattern for usernames
    /*Usernames criteria:
        -must be at least 4 characters long and no longer than 25 characters
        -must start with a letter or a number (alpha-numeric character)
        -aside from alpha-numeric characters only the following are allowed: .-_
    */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){1,23}[a-zA-Z0-9]$");

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException{
        
        String username = value.toString();
        FacesMessage message = new FacesMessage();
        
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
    }
    
}//End of class
