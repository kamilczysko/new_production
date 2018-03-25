package controller.functions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import application.NextWorkingDay;
import application.WorkingHoursConstants;
import model.Batch;
import model.Production;
import model.Station;
import model.Timing;
import service.ProductionService;
import service.TimingService;

@Service
public class TimingTool {

	@Autowired
	private ProductionService productionService;
	@Autowired
	private TimingService timingService;

	private MultiValueMap<Long, Date[]> dateMapForStations;
	private Map<Long, Date> batchEndTimeMap;
	private Date[] findTime = null;

	private Date START_WORKING_TIME, END_WORKING_TIME;

	private void setWorkingTimes(Date processStartTime) {
		START_WORKING_TIME = getStartDate(processStartTime);
		END_WORKING_TIME = getEndDate(processStartTime);
	}

	private Date getStartDate(Date d) {
		MyDate startWorking = new MyDate();
		startWorking.setTime(d.getTime());
		startWorking.setHourAndMinute(WorkingHoursConstants.WORK_START_HOUR, WorkingHoursConstants.WORK_START_MINUTES);
		return startWorking;
	}

	private Date getEndDate(Date d) {
		MyDate endWorking = new MyDate();
		endWorking.setTime(d.getTime());
		endWorking.setHourAndMinute(WorkingHoursConstants.WORK_END_HOUR, WorkingHoursConstants.WORK_END_MINUTES);
		return endWorking;
	}

	public void initDateMap() {
		this.dateMapForStations = new LinkedMultiValueMap<Long, Date[]>();
		this.batchEndTimeMap = new TreeMap<Long, Date>();
	}

	

	public Date saveTiming(List<Production> prod, Date processStart) throws SQLException {

		setWorkingTimes(processStart);
		processStart = checkProcessStartTime(processStart);

		for (Production p : prod) {
			long idPartia = p.getBatchObject().getIDPartia();

			Timing t = makeTiming(p);

			if (batchEndTimeMap.containsKey(idPartia)) {
				Date date = batchEndTimeMap.get(idPartia);
				if (date.after(processStart))
					if (p.getOperationObject() != null)
						processStart = date;
			}
			findTime = findTime(processStart, p.getDurationInSeconds(),
					getListOfTimeRanges(p.getStationObject(), processStart), p.getStationObject());
			checkIfTimingIsOk(findTime, t);
			t.setStart(findTime[0]);
			saveBatchEndTime(p.getBatchObject(), findTime[1]);
			timingService.save(t);
		}
		return findTime[1];

	}

	private Date checkProcessStartTime(Date start) {
		Date time = start;

		if (start.after(END_WORKING_TIME) || start.equals(END_WORKING_TIME)) {
			time = getNextWorkingDayWithStartTime(start);
			setWorkingTimes(time);
		}

		return time;
	}

	private boolean checkIfTimingIsOk(Date[] dates, Timing t) {
		setWorkingTimes(dates[0]);
		if (!startsInWorkingHoursBeforeEnd(dates[0])) {
			Date nextWorkingDayWithStartTime = getNextWorkingDayWithStartTime(dates[0]);
			Date[] moment = findMoment(t.getProduction().getStationObject(), t.getDurationOfThisActualTiming(),
					nextWorkingDayWithStartTime);
			t.setStart(moment[0]);
//			calculateEndTimeWhenNecessary(moment[1]);
			findTime = moment; 
			return false;
		}
		return true;
	}
	
	private Date calculateEndTimeWhenNecessary(Date endTime){
		Date d = endTime;
		
		if(!endBeforeEndWorkingHours(endTime)){
			Date nextWorkingDayWithStartTime = getNextWorkingDayWithStartTime(endTime);
			long calculatedMilis = calculateTimeFromEndWorkingTimeToEndOperationTimeInMilis(endTime);
			d = new Date(nextWorkingDayWithStartTime.getTime()+calculatedMilis);
		}
		return d;
	}
	
	private long calculateTimeFromEndWorkingTimeToEndOperationTimeInMilis(Date endTime){
		return (END_WORKING_TIME.getTime() - endTime.getTime());
	}


	private boolean startsInWorkingHoursBeforeEnd(Date startTime) {
		return ((startTime.after(START_WORKING_TIME) || startTime.equals(START_WORKING_TIME))
				&& (startTime.before(END_WORKING_TIME))) ? true : false;
	}
	
	private boolean endBeforeEndWorkingHours(Date endTime) {
		return  (endTime.before(END_WORKING_TIME) || endTime.equals(END_WORKING_TIME)) ? true : false;
	}

	
	
	private Timing makeTiming(Production p) {
		Timing t = new Timing();

		t.setId(p.getIDProdukcja());
		t.setAmount(p.getBatchObject().getIlosc());
		t.setProduction(p);
		t.setWorker("");
		t.setOpis("");

		return t;
	}

	private Date getNextWorkingDayWithStartTime(Date today) {
		Calendar calendar = Calendar.getInstance();
		Date d = new Date();

		calendar.setTime(today);
		d = setStartingDayHour(NextWorkingDay.getNextWorkingday(calendar, false).getTime());

		return d;
	}

	private Date setStartingDayHour(Date d) {
		MyDate startTime = new MyDate();
		startTime.setTime(d.getTime());
		startTime.setHourAndMinute(WorkingHoursConstants.WORK_START_HOUR, WorkingHoursConstants.WORK_START_MINUTES);
		return startTime;
	}

	private void saveBatchEndTime(Batch b, Date date) {
		long idPartia = b.getIDPartia();
		if (batchEndTimeMap.containsKey(idPartia))
			batchEndTimeMap.remove(idPartia);

		batchEndTimeMap.put(idPartia, date);
	}

	public Date[] findTime(Date whenStartOperation, long operationDuration, List<Date[]> timingList, Station station) {

		if (timingList.isEmpty()) {
			Date[] time = new Date[2];
			time = getStartAndEndDateReversed(whenStartOperation, operationDuration);
			timingList.add(time);
			dateMapForStations.add(station.getStationId(), time);
		}
		return findMoment(station, operationDuration, whenStartOperation);

	}

	private Date[] findMoment(Station station, long opDuration, Date processStart) {
		Date[] result = null;
		List<Date[]> list = dateMapForStations.get(station.getStationId());
		ListIterator<Date[]> i = list.listIterator();

		Date[] d = getStartAndEndDate(processStart, opDuration);
//		Date[] tryBefore = d;

//		if (tryBefore[1].before(list.get(0)[0])) {
//			tryBefore = getStartAndEndDateReversed(processStart, opDuration);
//			dateMapForStations.get(station.getStationId()).add(0, tryBefore);
//		}

		i = dateMapForStations.get(station.getStationId()).listIterator();
		while (i.hasNext()) {
			Date[] d1 = i.next();
			if (i.hasNext()) {
				Date[] d2 = i.next();
				if (d2[1].before(processStart)) {
					result = getStartAndEndDate(processStart, opDuration);
					continue;
				}

				long diference = ((d2[0].getTime() - d1[1].getTime()) / 1000) - WorkingHoursConstants.TIME_MARGIN;
				if (opDuration < diference) {
					Date[] dates = getStartAndEndDate(d1[1], opDuration);
					result = dates;
					int previousIndex = i.previousIndex();
					dateMapForStations.get(station.getStationId()).add(previousIndex, dates);
					break;
				} else
					i.previous();

			} else {

				if (d1[1].after(processStart)) {
					Date[] dat = getStartAndEndDate(d1[1], opDuration);
					dateMapForStations.add(station.getStationId(), dat);
					result = dat;
				} else {
					Date[] dat = getStartAndEndDate(processStart, opDuration);
					result = dat;
					dateMapForStations.add(station.getStationId(), dat);
				}

				break;
			}
		}

		return result;
	}

	private Date[] getStartAndEndDate(Date start, long duration) {
		Date[] dat = new Date[2];
		dat[0] = start;
		dat[1] = getEndDateOfOperation(start, duration);
		return dat;
	}

	private Date[] getStartAndEndDateReversed(Date end, long duration) {
		Date[] dat = new Date[2];
		dat[0] = getStartDate(end, duration);
		dat[1] = end;
		return dat;
	}

	private List<Date[]> getListOfTimeRanges(Station station, Date beginDate) throws SQLException {
		if (!dateMapForStations.containsKey(station.getStationId())) {
			List<Timing> timingsForStation = productionService.findTimingsByStation(station, beginDate);
			List<Date[]> timingList = prepareTimingList(timingsForStation, beginDate, station);
			return timingList;
		} else
			return dateMapForStations.get(station.getStationId());
	}
	
	private List<Date[]> prepareTimingList(List<Timing> timingsFromBase, Date beginDate, Station station){
		List<Date[]> timingList = new ArrayList<Date[]>();
		
		for (Timing t : timingsFromBase) {
			Date[] singleTimeRange = new Date[2];
			long durationInSeconds = t.getProduction().getDurationInSeconds();
			singleTimeRange[0] = t.getStart();
			
			if (timingIsNotEmpty(t))
				singleTimeRange[1] = t.getEnd();
			else
				singleTimeRange[1] = getEndDateOfOperation(t.getStart(), durationInSeconds);

			if (singleTimeRange[1].after(beginDate)) {
				timingList.add(singleTimeRange);
				dateMapForStations.add(station.getStationId(), singleTimeRange);
			}
		}
		return timingList;	
	}
		
	private long getDurationFromRangeInSeconds(Date[] d){
		long duration = 0;
		
		duration = (d[1].getTime() - d[0].getTime())/1000;
		return duration;
	}
	
	private long calculateTimeTillEndOfWorkDay(Date[] range){
		long time = 0;
		
		time = (END_WORKING_TIME.getTime() - range[1].getTime())/1000;
		return time;
	}
	
	private boolean timingIsNotEmpty(Timing t){
		return (t.getEnd() != null && t.getEnd() != new Date(0));
	}

	private Date getEndDateOfOperation(Date start, long duration) {
		long czas = start.getTime() + duration * 1000;

		return new Date(czas);
	}

	private Date getStartDate(Date start, long duration) {
		long czas = start.getTime() - duration * 1000;

		return new Date(czas);
	}

}
