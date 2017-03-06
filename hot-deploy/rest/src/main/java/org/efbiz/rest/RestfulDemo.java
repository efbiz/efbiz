package org.efbiz.rest;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/demo")    
public class RestfulDemo {  
	
	
        @GET    
        @Produces(MediaType.TEXT_PLAIN)    
        public String sayHello() {    
            return "Hello World!" ;    
        }    
         
            
        @GET    
        @Path("/{param}")      
        @Produces("text/plain;charset=UTF-8")    
        public String sayHelloToUTF8(@PathParam("param") String username) {    
            return "Hello " + username;    
        }    
        
        @POST  
        @Path("test_post3")   
        @Produces("text/plain")  
        public String getTest2222(String entity, @Context HttpServletRequest request){  
            System.out.println("entity:"+entity);//hello 传入方式：resource.entity("hello").post(String.class);  
            String result;   
            result= "--------"+request.getContextPath();   
            return result;  
        }  
          
        @POST  
        @Path("test_post4")  
        //@Consumes("application/xml"),这样就会出错；@Consumes("application/x-www-form-urlencoded") 可以。  
        @Produces("text/plain")  
        public String getTest22222(InputStream is, @Context HttpServletRequest request) throws Exception{  
            byte[] buf = new byte[is.available()];  
            is.read(buf);  
            System.out.println("buf:"+new String(buf));  
            String result;   
            result= "--------"+request.getContextPath();   
            return result;  
        }  
 
}  