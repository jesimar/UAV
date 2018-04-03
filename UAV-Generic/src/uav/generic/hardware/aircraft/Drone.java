package uav.generic.hardware.aircraft;

import java.text.SimpleDateFormat;
import java.util.Date;
import uav.generic.struct.HomeLocation;
import uav.generic.struct.ListParameters;
import uav.generic.hardware.sensors.Attitude;
import uav.generic.hardware.sensors.Barometer;
import uav.generic.hardware.sensors.Battery;
import uav.generic.hardware.sensors.GPS;
import uav.generic.hardware.sensors.GPSInfo;
import uav.generic.hardware.sensors.SensorUAV;
import uav.generic.hardware.sensors.StatusUAV;
import uav.generic.hardware.sensors.Velocity;

/**
 * Classe que modela uma aeronave.
 * @author Jesimar S. Arantes
 */
public abstract class Drone {
    
    String nameAircraft;

    double speedCruize;//in m/s
    double speedMax;//in m/s
    double mass;//in kg
    double payload;//in kg
    double endurance;//in seconds
    
    double time;//in seconds
    int nextWaypoint;
    int countWaypoint;
    double distanceToHome;//in meters
    double distanceToCurrentWaypoint;//in meters
    
    String typeFailure;
    
    HomeLocation homeLocation;
    ListParameters listParameters;
    
    Battery battery; 
    GPS gps;
    Barometer barometer;       
    Attitude attitude;
    Velocity velocity;
    GPSInfo gpsinfo;
    SensorUAV sensorUAV;
    StatusUAV statusUAV;    

    public String getNameAircraft() {
        return nameAircraft;
    }
    
    public double getSpeedCruize() {
        return speedCruize;
    }
    
    public double getSpeedMax() {
        return speedMax;
    }    

    public double getMass() {
        return mass;
    }

    public double getPayload() {
        return payload;
    }
    
    public double getEndurance(){
        return endurance;
    }
    
    public double getTime() {
        return time;
    }
    
    public int getNextWaypoint() {
        return nextWaypoint;
    }
    
    public int getCountWaypoint() {
        return countWaypoint;
    }

    public double getDistanceToHome() {
        return distanceToHome;
    }
    
    public double getDistanceToCurrentWaypoint() {
        return distanceToCurrentWaypoint;
    }
    
    public void setTime(double time){
        this.time = time;
    }
    
    public void setNextWaypoint(String next) {
        this.nextWaypoint = Integer.parseInt(next);
    } 
    
    public void setCountWaypoint(String count) {
        this.countWaypoint = Integer.parseInt(count);
    }
    
    public void setDistanceToHome(String dist) {
        try{
            this.distanceToHome = Double.parseDouble(dist);
        }catch (NumberFormatException ex){
            this.distanceToHome = -1;
        }
    }
    
    public void setDistanceToCurrentWaypoint(String dist) {
        try{
            this.distanceToCurrentWaypoint = Double.parseDouble(dist);
        }catch (NumberFormatException ex){
            this.distanceToCurrentWaypoint = -1;
        }
    }

    public void setDistanceToHome2(String distanceToHome) {
        distanceToHome = distanceToHome.substring(21, distanceToHome.length() - 1);
        this.distanceToHome = Double.parseDouble(distanceToHome);
    }

    public void setTypeFailure(String typeFailure) {
        this.typeFailure = typeFailure;
    }        

    public HomeLocation getHomeLocation() {
        return homeLocation;
    }  
    
    public ListParameters getListParameters() {
        return listParameters;
    }

    public Battery getBattery() {
        return battery;
    }
    
    public GPS getGPS() {
        return gps;
    }
    
    public Barometer getBarometer() {
        return barometer;
    }
    
    public Attitude getAttitude(){
        return attitude;
    }
    
    public Velocity getVelocity(){
        return velocity;
    } 

    public SensorUAV getSensorUAV() {
        return sensorUAV;
    }

    public GPSInfo getGPSInfo() {
        return gpsinfo;
    }
    
    public StatusUAV getStatusUAV(){
        return statusUAV;
    }
    
    public void defineHomeLocation(double lat, double lng, double alt) {
        homeLocation.setHomeLocation(lat, lng, alt);
    }
    
    public void defineHomeLocation(String home) {
        homeLocation.parserInfoHomeLocation(home);
    }
    
    public void defineListParameters(String parameters) {
        listParameters.parseInfoParameters(parameters);
    }
    
    public void setInfoGPS(double lat, double lng) {
        this.gps.lat = lat;
        this.gps.lng = lng;
    }
    
    public void setInfoBarometer(double alt_rel, double alt_abs) {
        this.barometer.alt_rel = alt_rel;
        this.barometer.alt_abs = alt_abs;
    }
        
    public void setInfoBattery(double voltage, double current, double level) {
        this.battery.voltage = voltage;
        this.battery.current = current;
        this.battery.level = level;
    }           
    
    public void setInfoAttitude(double pitch, double yaw, double roll) {
        this.attitude.pitch = pitch;
        this.attitude.yaw = yaw;
        this.attitude.roll = roll;
    } 
    
    public void setInfoGPSInfo(int fix, int num_sat) {
        this.gpsinfo.fixType = fix;
        this.gpsinfo.satellitesVisible = num_sat;
    }
    
    public String title(){
        return "date;hour;time;lat;lng;alt_rel;alt_abs;voltage_bat;current_bat;"
                + "level_bat;pitch;yaw;roll;vx;vy;vz;fixtype;satellitesvisible;"
                + "eph;epv;heading;groundspeed;airspeed;next_wpt;count_wpt;"
                + "dist_to_home;dist_to_current_wpt;mode;system-status;armed;"
                + "is-armable;ekf-ok;type-failure";
    }
    
    @Override
    public String toString() {
        String dateHour = new SimpleDateFormat("yyyy/MM/dd;HH:mm:ss").format(new Date());
        return String.format("%s;%.1f;%.7f;%.7f;%.2f;%.2f;%.3f;%.2f;%.1f;%.4f;%.4f;%.4f;%.2f;" +
                "%.2f;%.2f;%d;%d;%d;%d;%.1f;%.2f;%.2f;%d;%d;%.2f;%.2f;%s;%s;%s;" +
                "%s;%s;%s", 
                dateHour, time, gps.lat, gps.lng, barometer.alt_rel, barometer.alt_abs,
                battery.voltage, battery.current, battery.level, attitude.pitch,
                attitude.yaw, attitude.roll, velocity.vx, velocity.vy, velocity.vz, 
                gpsinfo.fixType, gpsinfo.satellitesVisible, gpsinfo.eph, gpsinfo.epv, 
                sensorUAV.heading, sensorUAV.groundspeed, sensorUAV.airspeed, 
                nextWaypoint, countWaypoint, distanceToHome, distanceToCurrentWaypoint,
                statusUAV.mode, statusUAV.systemStatus, statusUAV.armed, 
                statusUAV.isArmable, statusUAV.ekfOk, typeFailure);
    }
}