
package retroware.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {
    
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException{
    
        String password = value.toString();
        FacesMessage message = new FacesMessage();
        
        //Perform checks on the password. If a check fails add an error message and trigger an exception to prevent input from being set.
        
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
    
}//End of class
