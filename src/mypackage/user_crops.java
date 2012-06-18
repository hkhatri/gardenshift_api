package mypackage;

import java.util.Date;

public class user_crops {
	
	String crop_name;
	String crop_expected_quantity;
	Date crop_harvest_date;
	String crop_harvested;
	String pictures;
	String videos;
	String comments;
	
	public user_crops()
	{
		 crop_name= "";
		 crop_expected_quantity= "";
		 crop_harvest_date= new Date();
		 crop_harvested= "";
		 pictures= "";
		 videos= "";
		 comments= "";
	}
	

}
