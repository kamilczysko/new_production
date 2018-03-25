package controller.functions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@SuppressWarnings("serial")
public class MyDate extends Date {

	private Calendar calendar = GregorianCalendar.getInstance();
	private SimpleDateFormat dayFormat = new SimpleDateFormat("dd-MM");
	
	@Override
	public void setTime(long time) {
		super.setTime(time);
		
		this.calendar.setTime(this);
	}

	public Date getMyDate(){
		return this.calendar.getTime();
	}
	
	public void setHourAndMinute(int hour, int minute){
		this.setHour(hour);
		this.setMinutes(minute);
		this.setSeconds(0);
		
		this.setTime(this.calendar.getTimeInMillis());
	}
	
	public void setHour(int hour) {
		calendar.set(Calendar.HOUR_OF_DAY, hour);
	}

	public void setMinutes(int minutes) {

		calendar.set(Calendar.MINUTE, minutes);
	}

	public void setSeconds(int seconds) {

		calendar.set(Calendar.SECOND, seconds);
	}
	
	public int getDay(){
		return this.calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	public int getYear(){
		return this.calendar.get(Calendar.YEAR);
	}
	
	
	@Override
	public String toString() {
		return dayFormat.format(this);
	}
	

}
