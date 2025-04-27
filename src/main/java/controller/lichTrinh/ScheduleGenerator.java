package controller.lichTrinh;

import entity.LichTrinh;
import entity.Tau;
import util.maLTGenerator;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ScheduleGenerator {
    private static final int STOPOVER_MINUTES = 10;

    private final Map<String, Integer> travelTimes;

    public ScheduleGenerator(Map<String, Integer> travelTimes) {
        this.travelTimes = travelTimes;
    }

    public List<LichTrinh> generateSchedules(List<String> stations, LocalTime departureTime, LocalDate departureDate, Tau train) {
        List<LichTrinh> schedules = new ArrayList<>();
        if (stations.size() < 2) {
            return schedules;
        }

        List<Time> departureTimes = new ArrayList<>();
        List<Time> arrivalTimes = new ArrayList<>();
        List<Date> departureDates = new ArrayList<>();
        List<Date> arrivalDates = new ArrayList<>();
        Map<String, Time> stationToTime = new HashMap<>();

        for (int i = 0; i < stations.size() - 1; i++) {
            String start = stations.get(i);
            String end = stations.get(i + 1);
            int travelMinutes = calculateTravelTime(start, end);

            String startFullName = getFullStationName(start);
            if (!stationToTime.containsKey(startFullName)) {
                stationToTime.put(startFullName, Time.valueOf(departureTime));
            }

            Time startTime = stationToTime.get(startFullName);
            LocalTime localStartTime = startTime.toLocalTime();
            LocalTime localEndTime = localStartTime.plusMinutes(travelMinutes);
            Time endTime = Time.valueOf(localEndTime);

            long daysToAdd = travelMinutes / 1440;
            LocalDate currentDepartureDate = departureDate.plusDays(daysToAdd);
            if (localEndTime.isBefore(localStartTime)) {
                currentDepartureDate = currentDepartureDate.plusDays(1);
            }

            Date sqlDepartureDate = Date.valueOf(currentDepartureDate);
            Date sqlArrivalDate = Date.valueOf(currentDepartureDate);

            departureTimes.add(startTime);
            arrivalTimes.add(endTime);
            departureDates.add(sqlDepartureDate);
            arrivalDates.add(sqlArrivalDate);

            String endFullName = getFullStationName(end);
            stationToTime.put(endFullName, Time.valueOf(localEndTime.plusMinutes(STOPOVER_MINUTES)));
            departureTime = localEndTime.plusMinutes(STOPOVER_MINUTES);
            departureDate = currentDepartureDate;
        }

        for (int i = 0; i < stations.size() - 1; i++) {
            for (int j = i + 1; j < stations.size(); j++) {
                Time departure = departureTimes.get(i);
                Time arrival = arrivalTimes.get(j - 1);
                Date departureDateSql = departureDates.get(i);
                Date arrivalDateSql = arrivalDates.get(j - 1);

                String startFullName = getFullStationName(stations.get(i));
                String endFullName = getFullStationName(stations.get(j));

                String scheduleId = maLTGenerator.generateMaLT(
                        startFullName,
                        endFullName,
                        departureDateSql.toLocalDate(),
                        departure.toLocalTime()
                );

                schedules.add(new LichTrinh(
                        scheduleId,
                        startFullName,
                        endFullName,
                        departureDateSql,
                        arrival,
                        departure,
                        arrivalDateSql,
                        0,
                        train
                ));
            }
        }

        return schedules;
    }

    private int calculateTravelTime(String start, String end) {
        String key1 = start + "_" + end;
        String key2 = end + "_" + start;
        return travelTimes.getOrDefault(key1, travelTimes.getOrDefault(key2, 0));
    }

    private String getFullStationName(String station) {
        return maLTGenerator.getStationMap().entrySet().stream()
                .filter(entry -> entry.getValue().equals(station))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ga không hợp lệ: " + station));
    }
}