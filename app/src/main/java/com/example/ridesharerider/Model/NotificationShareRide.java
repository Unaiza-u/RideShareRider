package com.example.ridesharerider.Model;

public class NotificationShareRide {
    String id, name, latStart, lngStart, latEnd, lngEnd, startLocation, endLocation, numOfSeats, ratePerSeat;

    public NotificationShareRide() {
    }

    public NotificationShareRide(String id, String name, String latStart, String lngStart, String latEnd, String lngEnd, String startLocation, String endLocation, String numOfSeats, String ratePerSeat) {
        this.id = id;
        this.name = name;
        this.latStart = latStart;
        this.lngStart = lngStart;
        this.latEnd = latEnd;
        this.lngEnd = lngEnd;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.numOfSeats = numOfSeats;
        this.ratePerSeat = ratePerSeat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatStart() {
        return latStart;
    }

    public void setLatStart(String latStart) {
        this.latStart = latStart;
    }

    public String getLngStart() {
        return lngStart;
    }

    public void setLngStart(String lngStart) {
        this.lngStart = lngStart;
    }

    public String getLatEnd() {
        return latEnd;
    }

    public void setLatEnd(String latEnd) {
        this.latEnd = latEnd;
    }

    public String getLngEnd() {
        return lngEnd;
    }

    public void setLngEnd(String lngEnd) {
        this.lngEnd = lngEnd;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getNumOfSeats() {
        return numOfSeats;
    }

    public void setNumOfSeats(String numOfSeats) {
        this.numOfSeats = numOfSeats;
    }

    public String getRatePerSeat() {
        return ratePerSeat;
    }

    public void setRatePerSeat(String ratePerSeat) {
        this.ratePerSeat = ratePerSeat;
    }
}
