package mypackage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;

import com.mongodb.DBObject;

public class userClass implements DBObject  {
	
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


	@Override
	public boolean containsField(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean containsKey(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Object get(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object put(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void putAll(BSONObject arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void putAll(Map arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Object removeField(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map toMap() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isPartialObject() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void markAsPartialObject() {
		// TODO Auto-generated method stub
		
	}
	
	

}
