
package retroware.datacontainers;

import java.io.Serializable;

public class CategoryRecord implements Serializable{
    
    private int category_id;
    private String title;
    
    //Default constructor
    public CategoryRecord(){}
    
    public CategoryRecord(int category_id, String title){
        this.category_id = category_id;
        this.title = title;
    }
    
    /*GETTERS*/
    public int getCategory_id(){return category_id;}
    public String getTitle(){return title;}
    
    /*SETTERS*/
    public void setCategory_id(int category_id){this.category_id = category_id;}
    public void setTitle(String title){this.title = title;}
    
    
    @Override
    public String toString(){
        return String.format("(%d,%s)", category_id, title);
    }
    
}//End of class
