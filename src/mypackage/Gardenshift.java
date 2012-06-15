/*
 * Gardenshift Webservices with Resteasy for Jboss
 * 
 * 
 * Author					Comment														Modified
 * 
 * Hilay Khatri				Add new user with email/username validation					06-11-2012
 * 	
 * Hilay Khatri				Insert user details in mongoDB								06-11-2012
 * 
 * Hilay Khatri				Show all users personal information							06-11-2012
 * 
 * Hilay Khatri				Show information of a particular user						06-11-2012
 * 
 * Hilay Khatri				Update personal records 									06-11-2012
 * 
 * Hilay Khatri				Authenticate user											06-11-2012
 *
 */

package mypackage;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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


@Path("/")
public class Gardenshift {
	
	
public DB db;
public Mongo mongo;
    
    public Gardenshift()
    {
    
    
try {


mongo = new Mongo("localhost", 27017);
db = mongo.getDB("gardenshift");

// db.authenticate("admin", "redhat".toCharArray());

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
	Boolean validUsername = false;				// See if valid email address	
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
		 document.put("name", "");
		 document.put("zipcode", ""); //HTML5 Geolocation API can also be used
		 
		 BasicDBObject feedback = new BasicDBObject();
		 feedback.put("from","");
		 feedback.put("text","");
		 document.put("feedback", feedback);
		 
		 BasicDBObject notification = new BasicDBObject();		 
		 notification.put("from","");
		 notification.put("text","");
		 notification.put("type", "");
		 document.put("notification", notification);
		 
		 BasicDBObject friends = new BasicDBObject();		 
		 friends.put("friends_username","");
		 friends.put("status","");
		 friends.put("friends", friends);
		 
		 BasicDBObject user_crops = new BasicDBObject();
		 user_crops.put("crop_name","");
		 user_crops.put("crop_expected_quantity","");
		 user_crops.put("crop_harvest_date", "");
		 user_crops.put("crop_harvested", "");
		 user_crops.put( "pictures", "");
		 user_crops.put("videos","");
		 user_crops.put("comments","");
		 document.put("user_crops", user_crops);
		 
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

	 
@Path("authenticate")	
@POST
public Response authenticate(@FormParam("username") String userID, @FormParam("password") String password) {
 	
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
		
				 BasicDBObject obj = (BasicDBObject) cursor.next();
				
				 String result = obj.getString("password");
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

	
@Path("user_info")
@POST
public Response insert(@FormParam("username") String userID, @FormParam("name") String name, @FormParam("phone") String phone, @FormParam("address") String address, @FormParam("gender") String gender, @FormParam("dob") String dob) {

/* 
 * Stores user's personal information in the database
*/

	try {
	
	
	DBCollection collection = db.getCollection("users");
	BasicDBObject document = new BasicDBObject();
	
	document.put("username", userID);
	document.put("name", name);
	document.put("address", address); //HTML5 Geolocation API can also be used
	document.put("gender", gender);
	document.put("phone", phone);
	document.put("dob", dob);
	
	collection.insert(document);
	
	} catch (MongoException e) {
		Response.status(500);
	}
	
	return Response.status(200).entity("Inserted").build();


}


		 
@GET
@Path("/user_details/{username}")
@Produces("application/json")
public Response showUserDetails(@PathParam("username") String userID) {
	
/*
 * 	Displays information for a user
 */

	 String msg ="";

	 try {
		 
	 
	 DBCollection collection = db.getCollection("users");
	 
	 BasicDBObject searchQuery = new BasicDBObject();
	 searchQuery.put("username", userID);
	
	 
	 DBCursor cursor = collection.find(searchQuery);
	 
	 if(cursor.hasNext() == false)
	 {
	 msg = "null";
	 return Response.status(200).entity(msg).build();
	 }
	  
	 while (cursor.hasNext()) {
	 msg += cursor.next();
	 }
	
	 } catch (MongoException e) {
		 Response.status(500);
	 }
	
	 return Response.status(200).entity(msg).build();

	
}

@GET
@Path("/user_search/{data}")
@Produces("application/json")
public Response searchUser(@PathParam("data") String data) {
	
/*
 * 	Displays information for a user
 */

	 String msg ="";

	 try {
		 
	 
	 DBCollection collection = db.getCollection("users");
	 
	 List<BasicDBObject> searchQuery = new ArrayList<BasicDBObject>();

	 searchQuery.add(new BasicDBObject("username", data))  ;
	 searchQuery.add(new BasicDBObject("email", data));
	 searchQuery.add(new BasicDBObject("zipcode", data));
	 searchQuery.add(new BasicDBObject("name", data));

	 BasicDBObject sQuery = new BasicDBObject();
	 sQuery.put("$or", searchQuery);
	 
	 DBCursor cursor = collection.find(sQuery);
	 
	 if(cursor.hasNext() == false)
	 {
	 msg = "null";
	 return Response.status(200).entity(msg).build();
	 }
	  
	 while (cursor.hasNext()) {
	 msg += cursor.next();
	 }
	
	 } catch (MongoException e) {
		 Response.status(500);
	 }
	
	 return Response.status(200).entity(msg).build();

	
}

@GET
@Path("/user_details/all")
@Produces("application/json")
public Response showAllUserDetails() {
	
/*
 * 	Displays information of all users
 */

	 String msg ="[";

	 try {
		 
	 
	 DBCollection collection = db.getCollection("users");
	 
	 DBCursor cursor = collection.find();
	 
	 if(cursor.hasNext() == false)
	 {
	 msg = "null";
	 }
	  
	 while (cursor.hasNext()) {
	 msg += cursor.next() + ",";
	 }
	
	 } catch (Exception e) {	
	 }
	 
	 msg = msg.substring(0, msg.length() -1);
	 msg += "]"; 
	
	 return Response.status(200).entity(msg).build();

	
}

@Path("user_info_update")
@POST
public Response update(@FormParam("username") String userID, @FormParam("name") String name, @FormParam("phone") String phone, @FormParam("address") String address, @FormParam("gender") String gender, @FormParam("dob") String dob) {

/* 
 * Stores user's personal information in the database
*/

	try {
	
	
	DBCollection collection = db.getCollection("users");
	
	BasicDBObject userSets = new BasicDBObject(); 
	
	BasicDBObject update = new BasicDBObject();
	
	update.put("name", name);
	update.put("zipcode", address);
	
	userSets.put("$set", update);
	 
	collection.update(new BasicDBObject().append("username", userID), userSets);
	
	} catch (MongoException e) {
		Response.status(500);
	}
	
	return Response.status(200).entity("Inserted").build();


}




//@POST
//@Path("/upload")
//@Consumes("multipart/form-data")
//public Response uploadFile(@MultipartForm FileUploadForm form) {
//
//	String fileName = "/home/hilaykhatri/test211.txt";
//
//	try {
//		writeFile(form.getData(), fileName);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//
//	System.out.println("Done");
//
//	return Response.status(200)
//	    .entity("uploadFile is called, Uploaded file name : " + fileName).build();
//
//}
//
//// save to somewhere
//private void writeFile(byte[] content, String filename) throws IOException {
//
//
//
//	DBCollection collection = db.getCollection("images");
//
//	
//
//	// create a "photo" namespace
//	GridFS gfsPhoto = new GridFS(db, "photo");
//
//	// get image file from local drive
//	GridFSInputFile gfsFile = gfsPhoto.createFile(content);
//
//	// set a new filename for identify purpose
//	gfsFile.setFilename("hilay");
//
//	// save the image file into mongoDB
//	gfsFile.save();
//
//	// print the result
//	DBCursor cursor = gfsPhoto.getFileList();
//	while (cursor.hasNext()) {
//		System.out.println(cursor.next());
//	}
//
//	// get image file by it's filename
//	GridFSDBFile imageForOutput = gfsPhoto.findOne("hilay");
//
//	// save it into a new image file
//	imageForOutput.writeTo("/home/hilaykhatri/test1hilay.txt");
//	
//	System.out.println("Done");
//
//}


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
