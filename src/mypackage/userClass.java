package mypackage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;

import com.mongodb.DBObject;

public class userClass{
	
	String username;
	String name;
	String password;
	int zipcode;
	String email;
	Date creation_date;
	
//	List<feedback> Feedback = new ArrayList<feedback>();	
//	List<notification> Notifications = new ArrayList<notification>();	
//	List<user_crops> UserCrops = new ArrayList<user_crops>();
//	List<userFriends> Friends = new ArrayList<userFriends>();
//	

	public userClass()
	{
		this.username = "helloWorld";
		this.name = "hilay";
		this.password="safafs";
		this.zipcode = 32493;
		this.email ="assaf@asfsacc.com";
		this.creation_date = new Date();
		
//		feedback f1 = new feedback();
//		Feedback.add(f1);
//		
//		notification n1 = new notification();
//		Notifications.add(n1);
//			
//		user_crops u1 = new user_crops();
//		UserCrops.add(u1);
//		
//		userFriends uf1 = new userFriends();
//		Friends.add(uf1);
//		
		
		
		
	}


	
}
