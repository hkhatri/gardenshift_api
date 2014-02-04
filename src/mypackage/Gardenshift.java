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
 * Ramana Malladi			user crop search
 * 
 *  Ramana Malladi 			crop search
 *  
 *  Ramana Malladi			create crop				
 *  
 *  Ramana Malladi			update a crop			
 *  
 *  Ramana Malladi			delete a crop
 *  
 *  Ramana Malladi			crop_details_all
 *
 */

package mypackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

	public Gardenshift() {

		try {

		//	mongo = new Mongo("127.3.119.1", 27017);
			mongo = new Mongo("localhost", 27017);
			db = mongo.getDB("gardenshift");

		//	db.authenticate("admin", "redhat".toCharArray());

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
	public Response adduser(@FormParam("username") String userID,
			@FormParam("password") String password,
			@FormParam("email") String emailAdd) {

		/*
		 * Adds a new username to the database.
		 */

		Boolean userExists = false; // See if username already exists
		Boolean emailExists = false; // See if email already exists
		Boolean validEmail = false; // See if valid email address
		Boolean validUsername = false; // See if valid email address
		String user, email; // Stores username and email retrieved from mongoDB
							// for verification

		String msg = ""; // Error message

		try {
			
		//	userClass newUser = new userClass();

			validEmail = isValidEmailAddress(emailAdd);

			if (userID.length() < 6) {
				msg = "Username should not be less than 6 characters";
				return Response.status(200).entity(msg).build();

			}

			if (validEmail) {

				DBCollection collection = db.getCollection("users");
				BasicDBObject document = new BasicDBObject();

				DBCursor cursor = collection.find();

				while (cursor.hasNext()) {

					BasicDBObject obj = (BasicDBObject) cursor.next();

					user = obj.getString("username");
					email = obj.getString("email");

					if (user.equals(userID)) {
						userExists = true;
						msg = "failure-user already exists";

					}

					if (email.equals(emailAdd)) {
						emailExists = true;
						msg = "failure-email already exists";

					}

				}

				// If username or email is unique, create a new user
				if (userExists == false && emailExists == false) {

					msg = "Success-user created";
					document.put("username", userID);
					document.put("password",
							encryptPassword(password, "SHA-1", "UTF-8"));
					document.put("creation_date", new Date().toString());
					document.put("email", emailAdd);
					document.put("name", "");
					document.put("zipcode", ""); // HTML5 Geolocation API can
													// also be used

					BasicDBObject feedback = new BasicDBObject();
					feedback.put("from", "");
					feedback.put("text", "");
					document.put("feedback", feedback);

					BasicDBObject notification = new BasicDBObject();
					notification.put("from", "");
					notification.put("text", "");
					notification.put("type", "");
					document.put("notification", notification);

					BasicDBObject friends = new BasicDBObject();
					friends.put("friends_username", "");
					friends.put("status", "");
					friends.put("friends", friends);

					BasicDBObject user_crops = new BasicDBObject();
					user_crops.put("crop_name", "");
					user_crops.put("crop_expected_quantity", "");
					user_crops.put("crop_harvest_date", "");
					user_crops.put("crop_harvested", "");
					user_crops.put("pictures", "");
					user_crops.put("videos", "");
					user_crops.put("comments", "");
					document.put("user_crops", user_crops);
					
					collection.insert(document);
					
				}
			}

			else {
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

	/*
	 * This method encrypts the password using SHA-1 algorithm and UTF-8
	 * encoding.
	 */
	private static String encryptPassword(String plaintext, String algorithm,
			String encoding) throws Exception {

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
	public Response authenticate(@FormParam("username") String userID,
			@FormParam("password") String password) {

		/*
		 * This method authenticates the user
		 */

		String msg = "false";
	
		try {

			DBCollection collection = db.getCollection("users");

			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("username", userID);

			BasicDBObject keys = new BasicDBObject();
			keys.put("password", 1);

			DBCursor cursor = collection.find(searchQuery, keys);

			while (cursor.hasNext()) {

				BasicDBObject obj = (BasicDBObject) cursor.next();

				String result = obj.getString("password");
				
				System.out.println("function=" + encryptPassword(password, "SHA-1", "UTF-8") + "from database=" + result);

				if (result.equals(encryptPassword(password, "SHA-1", "UTF-8"))) {
					msg = "true";
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

	// @Path("user_info")
	// @POST
	// public Response insert(@FormParam("username") String userID,
	// @FormParam("name") String name, @FormParam("phone") String phone,
	// @FormParam("address") String address, @FormParam("gender") String gender,
	// @FormParam("dob") String dob) {
	//
	// /*
	// * Stores user's personal information in the database
	// */
	//
	// try {
	//
	//
	// DBCollection collection = db.getCollection("users");
	// BasicDBObject document = new BasicDBObject();
	//
	// document.put("username", userID);
	// document.put("name", name);
	// document.put("address", address); //HTML5 Geolocation API can also be
	// used
	// document.put("gender", gender);
	// document.put("phone", phone);
	// document.put("dob", dob);
	//
	// collection.insert(document);
	//
	// } catch (MongoException e) {
	// Response.status(500);
	// }
	//
	// return Response.status(200).entity("Inserted").build();
	//
	//
	// }
	//

	@GET
	@Path("/user_details/{username}")
	@Produces("application/json")
	public Response showUserDetails(@PathParam("username") String username) {

		/*
		 * Displays information for a user
		 */

		String msg = "";

		try {

			BasicDBObject searchQuery = new BasicDBObject();
			DBCollection collection = db.getCollection("users");

			searchQuery.put("username",
					java.util.regex.Pattern.compile(username));
			DBCursor cursor = collection.find(searchQuery);

			if (cursor.hasNext() == false) {
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
	@Path("/user_available/{username}")
	@Produces("application/json")
	public Response isUserAvailable(@PathParam("username") String username) {

		/*
		 * Displays information for a user
		 */

		String msg = "";

		try {

			BasicDBObject searchQuery = new BasicDBObject();
			DBCollection collection = db.getCollection("users");

			searchQuery.put("username",
					username);
			DBCursor cursor = collection.find(searchQuery);

			if (cursor.hasNext() == false) {
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
		 * Displays information for a user
		 */

		String msg = "";

		try {

			DBCollection collection = db.getCollection("users");

			List<BasicDBObject> searchQuery = new ArrayList<BasicDBObject>();

			searchQuery.add(new BasicDBObject("username", data));
			searchQuery.add(new BasicDBObject("email", data));
			searchQuery.add(new BasicDBObject("zipcode", data));
			searchQuery.add(new BasicDBObject("name", data));

			BasicDBObject sQuery = new BasicDBObject();
			sQuery.put("$or", searchQuery);

			DBCursor cursor = collection.find(sQuery);

			if (cursor.hasNext() == false) {
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
		 * Displays information of all users
		 */

		String msg = "[";

		try {

			DBCollection collection = db.getCollection("users");

			DBCursor cursor = collection.find();

			if (cursor.hasNext() == false) {
				msg = "null";
			}

			while (cursor.hasNext()) {
				msg += cursor.next() + ",";
			}

		} catch (Exception e) {
		}

		msg = msg.substring(0, msg.length() - 1);
		msg += "]";

		return Response.status(200).entity(msg).build();

	}

	@Path("updateuser")
	@POST
	public Response update(@FormParam("username") String username,
			@FormParam("name") String name, 
			@FormParam("zip") String zip,
			@FormParam("email") String email) {

		/*
		 * Stores user's personal information in the database
		 */

		
		try {
			DBCollection collection = db.getCollection("users");
			BasicDBObject newDocument = new BasicDBObject().append("$set",
					new BasicDBObject().append("name", name).append("email", email).append("zipcode", zip));
			
			System.out.println(newDocument);

			collection.update(
					new BasicDBObject().append("username", username),
					newDocument);

			return Response.status(200).entity("success").build();

		} catch (Exception e) {
			return Response.status(503).entity("failed").build();
		}

	}

	@GET
	@Path("deleteuser/{username}")
	@Produces("application/json")
	public Response deleteUser(@PathParam("username") String username) {
		/*
		 * This method deletes a particular crop entry
		 */
		try {

			DBCollection collection = db.getCollection("users");
			BasicDBObject searchquery = new BasicDBObject();
			searchquery.put("username", username);

			collection.remove(searchquery);

			return Response.status(200).entity("success").build();

		} catch (Exception e) {
			return Response.status(503).entity("failed").build();
		}

	}

	// @POST
	// @Path("/upload")
	// @Consumes("multipart/form-data")
	// public Response uploadFile(@MultipartForm FileUploadForm form) {
	//
	// String fileName = "/home/hilaykhatri/test211.txt";
	//
	// try {
	// writeFile(form.getData(), fileName);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// System.out.println("Done");
	//
	// return Response.status(200)
	// .entity("uploadFile is called, Uploaded file name : " +
	// fileName).build();
	//
	// }
	//
	// // save to somewhere
	// private void writeFile(byte[] content, String filename) throws
	// IOException {
	//
	//
	//
	// DBCollection collection = db.getCollection("images");
	//
	//
	//
	// // create a "photo" namespace
	// GridFS gfsPhoto = new GridFS(db, "photo");
	//
	// // get image file from local drive
	// GridFSInputFile gfsFile = gfsPhoto.createFile(content);
	//
	// // set a new filename for identify purpose
	// gfsFile.setFilename("hilay");
	//
	// // save the image file into mongoDB
	// gfsFile.save();
	//
	// // print the result
	// DBCursor cursor = gfsPhoto.getFileList();
	// while (cursor.hasNext()) {
	// System.out.println(cursor.next());
	// }
	//
	// // get image file by it's filename
	// GridFSDBFile imageForOutput = gfsPhoto.findOne("hilay");
	//
	// // save it into a new image file
	// imageForOutput.writeTo("/home/hilaykhatri/test1hilay.txt");
	//
	// System.out.println("Done");
	//
	// }
	
	
	
	@GET
	@Path("/userCropSearch/{cropname}")
	@Produces("application/json")
	public Response showUserByCrop(@PathParam("cropname") String cropname) {

		/*
		 * Displays information for a user
		 */

		String msg = "[";

		try {

			BasicDBObject searchQuery = new BasicDBObject();
			
			BasicDBObject keys = new BasicDBObject();
			
			DBCollection collection = db.getCollection("users");
			
			keys.put("username", 1);
			keys.put("email", 1);
			keys.put("zipcode", 1);
			keys.put("user_crops.crop_name", 1);
			
			searchQuery.put("user_crops.crop_name",
					java.util.regex.Pattern.compile(cropname));
			DBCursor cursor = collection.find(searchQuery, keys);

			if (cursor.hasNext() == false) {
				msg = "null";
				return Response.status(200).entity(msg).build();
			}

			while (cursor.hasNext()) {
				msg += cursor.next() + ",";
			}

		} catch (MongoException e) {
			Response.status(500);
		}
		
		msg = msg.substring(0, msg.length() - 1);
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
	
	// Add a crop grown by user
	@GET
    @Path("create_usercrop/{username}/{name}/{quantity}/{date}/{comment}")
    @Produces("application/json")
    public Response addusercrop(
            @PathParam("name") String name,
            @PathParam("username") String username,
            @PathParam("quantity") String quantity,
            @PathParam("date") String date,
            @PathParam("comment") String comment) {

       
        try {
            DBCollection collection = db.getCollection("users");
            
            BasicDBObject update = new BasicDBObject();
            update.put("username", username);

           
            BasicDBObject document = new BasicDBObject();
            
                document.put("crop_name", name);
                document.put("crop_expected_quantity", quantity);
                document.put("crop_harvest_date",date);
                document.put("comments",comment);
                
                BasicDBObject temp = new BasicDBObject();
                temp.put("$push", new BasicDBObject("user_crops", document));

                collection.update(update, temp, true, true);

                return Response.status(200).entity("success").build();

            }
         catch (Exception e) {
            return Response.status(503).entity("failed").build();
        }
    }
	
	// Delete a crop grown by user
	
	@GET
	@Path("delete_usercrop/{username}/{crop_name}")
	@Produces("application/json")
	public Response delete_usercrop(@PathParam("crop_name") String crop_name, @PathParam("username") String username) {
	    /*
	     * This method deletes a particular crop entry
	     */
	    try {

	    	DBCollection collection = db.getCollection("users");
            
            BasicDBObject update = new BasicDBObject();
            update.put("username", username);

            // check if the entry is not a duplicate
            BasicDBObject document = new BasicDBObject();
            
                document.put("crop_name", crop_name);              
                
                BasicDBObject temp = new BasicDBObject();
                temp.put("$pull", new BasicDBObject("user_crops", document));

                collection.update(update, temp, true, true);
               
                return Response.status(200).entity("success").build();


	    } catch (Exception e) {
	        return Response.status(503).entity("failed").build();
	    }

	}
	
	//Update an existing crop grown by user
	@GET
    @Path("update_usercrop/{username}/{name}/{quantity}/{date}/{comment}")
    @Produces("application/json")
    public Response updateusercrop(
            @PathParam("name") String name,
            @PathParam("username") String username,
            @PathParam("quantity") String quantity,
            @PathParam("date") String date,
            @PathParam("comment") String comment) {

        /*
         * Adds a new crop entry to the database.
         */
//        try {
//            DBCollection collection = db.getCollection("users");
//            
//            BasicDBObject change = new BasicDBObject();
//            change.put("username", username);
//            change.put("user_crops.crop_name", name);
//            BasicDBObject setDoc = new BasicDBObject();                 
//           
//            setDoc.append("user_crops.0.crop_expected_quantity", quantity);   
//            setDoc.append("user_crops.0.crop_harvest_date", date); 
//            setDoc.append("user_crops.0.comments", comment); 
//            
//            BasicDBObject account = new BasicDBObject("$set", setDoc);
//            collection.update(change, account);
//               
//                return Response.status(200).entity("success").build();
//
//            }
//         catch (Exception e) {
//            return Response.status(503).entity("failed").build();
//        }
		
		
		try {
            DBCollection collection = db.getCollection("users");

            String URI = 
"http://dev-gardenshift.rhcloud.com/Gardenshift/delete_usercrop/";
            String URI1 = 
"http://dev-gardenshift.rhcloud.com/Gardenshift/create_usercrop/";
            String RESTCall ="";
            String res ="";
            String result="";
            String RESTCall1 ="";
            String res1 ="";
            String result1="";
            System.out.println("3");

                     RESTCall = URI + username+"/"+name;

                      URL url = new URL(RESTCall);

                   URLConnection conn = url.openConnection();

                   BufferedReader in = new BufferedReader(new
                   InputStreamReader(conn.getInputStream()));

                   while ((res = in.readLine()) != null) {

                   result += res;
                   }
                   System.out.println("1");
                   
                 RESTCall1 = URI1 + 
username+"/"+name+"/"+quantity+"/"+date+"/"+comment;

                    URL url1 = new URL(RESTCall1);

                 URLConnection conn1 = url1.openConnection();

                 in = new BufferedReader(new
                 InputStreamReader(conn1.getInputStream()));

                 while ((res1 = in.readLine()) != null) {

                 result1 += res1;
                 }
                 System.out.println("2");
                 return Response.status(200).entity("success").build();

            }
         catch (Exception e) {
            return Response.status(503).entity("failed").build();
        }
    }

	

	// Crops API===============================================

	
	@GET
    @Path("/crop_search/{crop_name}")
    @Produces("application/json")
    public Response searchCrop(@PathParam("crop_name") String crop_name) {

    /*
     *      Displays information for a user
     */

             String msg ="";

             try {


             DBCollection collection = db.getCollection("crop_details");

             BasicDBObject sq = new BasicDBObject();
             
sq.put("crop_name",java.util.regex.Pattern.compile(crop_name));
              DBCursor cursor = collection.find(sq);

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


	@POST
    @Path("create_crop")
    @Produces("application/json")
    public Response addcrop(
            @FormParam("name") String crop_name,
            @FormParam("description") String description) {

        /*
         * Adds a new crop entry to the database.
         */
        try {
            DBCollection collection = db.getCollection("crop_details");

            // check if the entry is not a duplicate
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("crop_name", crop_name);
                        DBCursor cursor = collection.find(searchQuery);
            if (cursor.hasNext()) {
                return Response
                        .status(403)
                        .entity("entry already exists, choose to update instead")
                        .build();
            } else {
                BasicDBObject document = new BasicDBObject();
                document.put("crop_name", crop_name);
                document.put("description", description);
                //document.put("image",image);

                collection.insert(document);

                return Response.status(200).entity("success").build();

            }
        } catch (Exception e) {
            return Response.status(503).entity("failed").build();
        }
    }

@POST
@Path("updatecrop")
@Produces("application/json")
public Response updatecrop(
        @FormParam("crop_name") String crop_name,
        @FormParam("description") String description) {
    /*
     * This method returns the list of all the crops grown by a
particular
     * user
     */
    try {
        DBCollection collection = db.getCollection("crop_details");
        BasicDBObject newDocument = new BasicDBObject().append(
                "$set",
                new BasicDBObject().append("description",
description));

        collection.update(
                new BasicDBObject().append("crop_name", crop_name),
                newDocument);

        return Response.status(200).entity("success").build();

    } catch (Exception e) {
        return Response.status(503).entity("failed").build();
    }

}

@GET
@Path("deletecrop/{crop_name}")
@Produces("application/json")
public Response deletecrop(@PathParam("crop_name") String crop_name) {
    /*
     * This method deletes a particular crop entry
     */
    try {

        DBCollection collection = db.getCollection("crop_details");
        BasicDBObject searchquery = new BasicDBObject();
        searchquery.put("crop_name", crop_name);

        collection.remove(searchquery);

        return Response.status(200).entity("success").build();

    } catch (Exception e) {
        return Response.status(503).entity("failed").build();
    }

}
@GET
@Path("crop_details/all")
@Produces("application/json")
public Response dashboard() {
    /*
     * This method returns the list of all the crops that are in the
     * database
     */
	String msg = "[";

	try {

		DBCollection collection = db.getCollection("crop_details");

		DBCursor cursor = collection.find();

		if (cursor.hasNext() == false) {
			msg = "null";
		}

		while (cursor.hasNext()) {
			msg += cursor.next() + ",";
		}

	} catch (Exception e) {
	}

	msg = msg.substring(0, msg.length() - 1);
	msg += "]";

	return Response.status(200).entity(msg).build();

}


// Geolocation Based API


@GET
@Path("/search/{zipcode}/{distance}")
@Produces("application/json")
public Response search_crop(@PathParam("zipcode") String zipcode, @PathParam("distance") String distance)
{

/*
* Displays all the users which are within the given radius
*/

	String URI = "http://api.geonames.org/findNearbyPostalCodesJSON?";
			
      String RESTCall ="";
      String res ="";
      String result="";

      try {


    	  	 RESTCall = URI + "formatted=true" + "&postalcode=" + zipcode +  "&country=US&" + "radius=" + distance + "&username=gardenshift&" +"style=full";
	      
    	   	 URL url = new URL(RESTCall);
	
	         URLConnection conn = url.openConnection();
	
	         BufferedReader in = new BufferedReader(new
	         InputStreamReader(conn.getInputStream()));
	        
	         while ((res = in.readLine()) != null) {
	
	         result += res;

      }
     
     } catch (IOException e) {
         // TODO Auto-generated catch block
     Response.status(500);
     }

      return Response.status(200).entity(result).build();

      }

@GET
@Path("/search/{zipcode}/{distance}/{cropname}")
@Produces("application/json")
public Response search_user_Crop(@PathParam("zipcode") String zipcode, @PathParam("distance") String distance, @PathParam("cropname") String cropname) throws Exception
{

/*
* Displays all the users which are within the given radius
*/

		  String URI = "http://api.geonames.org/findNearbyPostalCodesJSON?";
				
	      String RESTCall ="";
	      String res ="";
	      String result="";
      
    
    
    

         RESTCall = URI + "formatted=true" + "&postalcode=" + zipcode +  "&country=US&" + "radius=" + distance + "&username=gardenshift&" +"style=full";

         URL url = new URL(RESTCall);

         URLConnection conn = url.openConnection();

         BufferedReader in = new BufferedReader(new
         InputStreamReader(conn.getInputStream()));
                 
         
       
        
         while ((res = in.readLine()) != null) {

         result += res;

      }
         
     
         ArrayList<String> zip = new ArrayList();
       
         JsonElement json = new JsonParser().parse(result);

         JsonObject obj= json.getAsJsonObject();
         
         JsonArray jarray = obj.getAsJsonArray("postalCodes");
         
         
       
         for( int i=0; i < obj.getAsJsonArray("postalCodes").size(); i++)
         {
        	 
        	 JsonObject jobject = jarray.get(i).getAsJsonObject();
        	 String result1 = jobject.get("postalCode").getAsString();
        	 
        	 zip.add(result1);
        	 
        	    
        	       
         }
         
         
        String msg = "[";
         
        BasicDBObject keys = new BasicDBObject();
			
		DBCollection collection = db.getCollection("users");
		
		keys.put("username", 1);
		keys.put("email", 1);
		keys.put("zipcode", 1);
		keys.put("user_crops.crop_name", 1);
		
		List<BasicDBObject> searchQuery = new ArrayList<BasicDBObject>();

		
		BasicDBObject filteredZip = new BasicDBObject(); 
        
        filteredZip.put("zipcode", new BasicDBObject("$in", zip) );
         
        searchQuery.add(new BasicDBObject("user_crops.crop_name",java.util.regex.Pattern.compile(cropname)));
 		searchQuery.add(filteredZip);
 		
 		BasicDBObject sQuery = new BasicDBObject();
 		sQuery.put("$and", searchQuery);
         
 		DBCursor cursor =  collection.find(sQuery, keys);
         
         if(cursor.hasNext() == false)
         {
             return Response.status(200).entity("null").build();
         }
         
         while (cursor.hasNext()) {
             msg = msg + cursor.next() + ", ";
         }
         
         msg = msg.substring(0, msg.length() - 1);
 		 msg += "]";


         return Response.status(200).entity(msg).build();
} 


}

