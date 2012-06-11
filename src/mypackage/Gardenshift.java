/*
 * Beershift Webservices with Resteasy for Jboss
 * 
 * 
 * Author					Comment														Modified
 * 
 * Hilay Khatri				Add new user with email/username validation					05-31-2012
 
 */

package mypackage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response;

import sun.misc.BASE64Encoder;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import java.io.*;
import java.net.*;


@Path("/")
public class Gardenshift {
	
	
	DB db;
    
    public Gardenshift()
    {
    
	try {
	
	Mongo mongo;
	mongo = new Mongo("localhost", 27017);
	db = mongo.getDB("gardenshift");
	
	} catch (UnknownHostException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
	} catch (MongoException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
	}
	
}

		
    
	
@Path("adduser")	 
@POST
public Response adduser(@FormParam("username") String userID, @FormParam("password") String password, @FormParam("email") String emailAdd) {
	
/*	
 * 	Adds a new username to the database.
 */ 

	Boolean userExists = false;					// See if username already exists
	Boolean emailExists = false;				// See if email already exists
	Boolean validEmail = false;					// See if valid email address
	Boolean validUsername = false;					// See if valid email address	
	String user, email;							// Stores username and email retrieved from mongoDB for verification
	
	String msg = "";							// Error message

	try {
	 
		
	validEmail = isValidEmailAddress(emailAdd);
	
	if(userID.length() <6)
	{
		msg = "Username should not be less than 6 characters";
		return Response.status(200).entity(msg).build();
		
	}
			
	if(validEmail) 
	{
		
	
		DBCollection collection = db.getCollection("users");
		BasicDBObject document = new BasicDBObject();
				 	 
	 	DBCursor cursor = collection.find();
	 	 
	 	while (cursor.hasNext()) {
	 	
		 	BasicDBObject obj = (BasicDBObject) cursor.next();
		 	 	    
			user = obj.getString("username");
			email = obj.getString("email");
		
			
			if(user.equals(userID) )
			{
				userExists=true;
				msg = "failure-user already exists";
				
			}
			
			if(email.equals(emailAdd) )
			{
				emailExists=true;
				msg = "failure-email already exists";
				
			}
		
		}
	
 	 // If username or email is unique, create a new user
		 if(userExists == false && emailExists == false ){
			 
		 msg ="Success-user created";
	 	 document.put("username", userID);
		 document.put("password", encryptPassword(password,"SHA-1","UTF-8"));
		 document.put("creation_date", new Date().toString());
		 document.put("email", emailAdd);
		 
	
		 collection.insert(document);
		 
		 }
	}

	else
	{
		 msg = "Invalid Email Address";
		 return Response.status(200).entity(msg).build();
	}
	
	
	} catch (UnknownHostException e) {
		 Response.status(500);
	} catch (MongoException e) {
		 Response.status(500);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		 Response.status(500);
	}
	 return Response.status(200).entity(msg).build();

}
	

	// TODO Auto-generated method stub
	/*This method encrypts the password using SHA-1 algorithm and UTF-8 encoding. */
private static  String encryptPassword(String plaintext, String algorithm, String encoding) throws Exception {
	
	        MessageDigest msgDigest = null;
	        String hashValue = null;
	        
	        try {
	            msgDigest = MessageDigest.getInstance(algorithm);
	            msgDigest.update(plaintext.getBytes(encoding));
	            byte rawByte[] = msgDigest.digest();
	            hashValue = (new BASE64Encoder()).encode(rawByte);
	 
	        } catch (NoSuchAlgorithmException e) {
	            System.out.println("No Such Algorithm Exists");
	        } catch (UnsupportedEncodingException e) {
	            System.out.println("The Encoding Is Not Supported");
	        }
	        return hashValue;
	    }

	 
 @GET
 @Path("/authenticate/{userId}/{password}")	
 @Produces("application/json")
 public Response authenticate(@FormParam("username") String userID, @FormParam("username") String password) {
 	
 /*
  * This method authenticates the user
  */

 String msg ="false";

 try {
	 
 
 DBCollection collection = db.getCollection("users");

 BasicDBObject searchQuery = new BasicDBObject();
 searchQuery.put("username", userID);

 BasicDBObject keys = new BasicDBObject();
 keys.put("password", 1);

 
 DBCursor cursor = collection.find(searchQuery,keys);
 
 while (cursor.hasNext()) {
// msg += cursor.next();
 BasicDBObject obj = (BasicDBObject) cursor.next();
 String result="";
result+= obj.getString("password");
if(result.equals(encryptPassword(password,"SHA-1","UTF-8")))
{
	msg="true";
}

 }

 } catch (UnknownHostException e) {
 Response.status(500);
 } catch (MongoException e) {
	Response.status(500);
 } catch (Exception e) {
	// TODO Auto-generated catch block
	Response.status(500);
}
 
return Response.status(200).entity(msg).build();

 }

	 
	 
@Path("beer")
@POST
public Response drinkbeer(@FormParam("username") String userID, @FormParam("beerName") String beer, @FormParam("when") String when) {

/* Takes in userID and the beer as parameter and inserts into
* MongoDB with the current Time
*/

	try {
	
	
	DBCollection collection = db.getCollection("drank");
	BasicDBObject document = new BasicDBObject();
	
	document.put("username", userID);
	document.put("beer", beer);
	document.put("when", when);
	
	collection.insert(document);
	
	} catch (MongoException e) {
		Response.status(500);
	}
	
	return Response.status(200).entity("Inserted").build();


}


		 
@GET
@Path("/firehose")
@Produces("application/json")
public Response firehose() {
	
/*
 * 	Displays all beers drank by all user
 */

	 String msg ="[";

	 try {
		 
	 
	 DBCollection collection = db.getCollection("drank");
	 
	 BasicDBObject sortOrder = new BasicDBObject();	 
	 sortOrder.put("date", -1);
	 
	 DBCursor cursor = collection.find().limit(50).sort(sortOrder);
	 
	 if(cursor.hasNext() == false)
	 {
	 msg = "null";
	 return Response.status(200).entity(msg).build();
	 }
	  
	 while (cursor.hasNext()) {
	 msg += cursor.next()+",";
	 }
	
	 } catch (MongoException e) {
		 Response.status(500);
	 }
	 
	 msg = msg.substring(0, msg.length() -1);
	 msg += "]"; 
	
	 return Response.status(200).entity(msg).build();

	
	 }


@GET
@Path("/userbeers/username/{name}")
@Produces("application/json")
public Response user_beer(@PathParam("name") String userID)
{
	
/*  
 * 	Displays all the beers drank by user
 */
	
	
	 String msg ="[";

	 try {
	
	 DBCollection collection = db.getCollection("drank");
	 
	 BasicDBObject sortOrder = new BasicDBObject();	 
	 sortOrder.put("when", -1);
	
	 BasicDBObject searchQuery = new BasicDBObject();	 
	 searchQuery.put("username", userID);
	 
	 DBCursor cursor = collection.find(searchQuery).sort(sortOrder);
	 
	 if(cursor.hasNext() == false)
		 {
		 msg = "null";
		 return Response.status(200).entity(msg).build();
		 }
		 	
	 
	 while (cursor.hasNext()) {
	 msg += cursor.next()+",";
	 }
	
	 } catch (MongoException e) {
		 Response.status(500);
	 }
	 
	 msg = msg.substring(0, msg.length() -1);

	 msg += "]"; 
	
	 return Response.status(200).entity(msg).build();
	
	 }


	public static boolean isValidEmailAddress(String email) {
	   boolean result = true;
	   try {
	      InternetAddress emailAddr = new InternetAddress(email);
	      emailAddr.validate();
	   } catch (AddressException ex) {
	      result = false;
	   }
	   return result;
	}

	
}
