
package retroware.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("emailValidator")
public class EmailValidator implements Validator {
    
    //RegEx pattern for emails
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");
    
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException{
    
        Matcher matcher = EMAIL_PATTERN.matcher(value.toString());
        
        //If no match was found, add an error message and trigger an exception to prevent input from being set.
        if(!matcher.matches()){
            FacesMessage message = new FacesMessage("Invalid email format.","Invalid email format. Example: email@domain.co.uk");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            
            
            
            throw new ValidatorException(message);
        }
    }
    
}//End of class
