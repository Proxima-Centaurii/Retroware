
package retroware.servlets;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet("/userProfileImage")
public class UserProfileImageServlet extends HttpServlet{
    
    @Resource(name = "jdbc/retroware2")
    DataSource data_source;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        int user_id = Integer.valueOf(request.getParameter("uid"));
        
        //Try-catch block which specifies resources that can be closed.
        try(
            Connection connection = data_source.getConnection("APP", "app");
            PreparedStatement sql_statement = connection.prepareStatement("SELECT IMAGE, IMAGE_FILE_NAME, LENGTH(IMAGE) AS IMAGE_BYTE_LENGTH FROM USER_PROFILE_PICTURES WHERE USER_ID = ?");
        ){
            sql_statement.setInt(1, user_id);
            
            try(ResultSet rs = sql_statement.executeQuery()){
                if(rs.next()){
                    
                    int contentLength = rs.getInt("IMAGE_BYTE_LENGTH");
                    String filename = rs.getString("IMAGE_FILE_NAME");
                    String mimeType = getServletContext().getMimeType(filename);
                    
                    System.out.printf("Loading details:\nFilename: %s\nLength: %d\nMime type: %s\n", filename, contentLength, mimeType);
                    
                    response.reset();
                    response.setContentType(mimeType);
                    response.setContentLength(contentLength); //This is the length of the image
                    response.setHeader("Content-Disposition", "inline;filename=\""+filename+"\"");
                
                    
                    try(
                            ReadableByteChannel input = Channels.newChannel(rs.getBinaryStream("IMAGE"));
                            WritableByteChannel output = Channels.newChannel(response.getOutputStream());
                            //FacesContext.getCurrentInstance().getExternalContext().getResponseOutputStream()
                    ){
                        //Because of the JDK and glassfish version, the ByteBuffer object has to be casted to Buffer to use some methods that
                        //now are implemented for ByteBuffer as well
                        for(ByteBuffer buffer = ByteBuffer.allocateDirect(contentLength); input.read(buffer) != -1; ((Buffer)buffer).clear()){
                            output.write((ByteBuffer) ((Buffer)buffer).flip());
                        }
                    }
                }    
                else
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            
        }catch(SQLException e){
            e.printStackTrace();
            throw new ServletException("SQL/DB exception occured in servlet.");
        }
    }
    
}//end of class
