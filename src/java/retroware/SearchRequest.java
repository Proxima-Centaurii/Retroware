
package retroware;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name="searchRequest")
@RequestScoped
public class SearchRequest {
    
    private String search_string;
    
    public SearchRequest(){}
    
    
    public void setSearch_string(String search_string){
        this.search_string = search_string;
    }
    
    public String getSearch_string(){return search_string;}
    
    
    public String startSearch(){
        return "/categories.xhtml?faces-redirect=true&search="+search_string;
    }
    
}//End of class
