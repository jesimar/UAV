package uav.mosa.module.path_planner;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import lib.color.StandardPrints;
import uav.generic.hardware.aircraft.Drone;
import uav.generic.struct.constants.TypeCC;
import uav.generic.struct.mission.Mission;
import uav.generic.struct.mission.Mission3D;
import uav.generic.struct.constants.TypeOperationMode;
import uav.generic.struct.geom.PointGeo;
import uav.generic.struct.reader.ReaderFileConfigGlobal;
import uav.mosa.module.mission_manager.MissionManager;

/**
 * Classe que modela o planejador da missão do drone evitando obstáculos.
 * @author Jesimar S. Arantes
 */
public abstract class Planner {
    
    final ReaderFileConfigGlobal configGlobal;
    final String dir;
    final Drone drone;
    final Mission3D waypointsMission;
    final Mission3D mission3D;
    final Mission missionGeo;
    final PointGeo pointGeo;

    /**
     * Class constructor
     * @param drone instance of the aircraft
     * @param waypointsMission waypoints of the mission
     */
    public Planner(Drone drone, Mission3D waypointsMission) {
        this.configGlobal = ReaderFileConfigGlobal.getInstance();
        this.dir = configGlobal.getDirPlanner();
        this.drone = drone;
        this.waypointsMission = waypointsMission; 
        this.mission3D = new Mission3D();
        this.missionGeo = new Mission();
        this.pointGeo = MissionManager.pointGeo;
    }
    
    public abstract void clearLogs();
    
    public Mission3D getMission3D(){
        return mission3D;
    }
    
    public Mission getMissionGeo(){
        return missionGeo;
    }
    
    boolean execMethod(){
        try {
            boolean print = false;
            boolean error = false;
            File f = new File(dir);
            String cmd = "";
            if (configGlobal.getOperationMode().equals(TypeOperationMode.SITL_LOCAL)){
                cmd = configGlobal.getCmdExecPlanner() + " local";
            } else if (configGlobal.getOperationMode().equals(TypeOperationMode.SITL_CC) ||
                    configGlobal.getOperationMode().equals(TypeOperationMode.REAL_FLIGHT)){
                if (configGlobal.getTypeCC().equals(TypeCC.EDISON)){
                    cmd = configGlobal.getCmdExecPlanner() + " edison";
                }else if (configGlobal.getTypeCC().equals(TypeCC.RASPBERRY)){
                    cmd = configGlobal.getCmdExecPlanner() + " rpi";
                }
            }
            final Process comp = Runtime.getRuntime().exec(cmd, null, f);
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Scanner sc = new Scanner(comp.getInputStream());
                    if (print) {
                        while (sc.hasNextLine()) {
                            System.out.println(sc.nextLine());
                        }
                    }
                    sc.close();
                }
            });
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Scanner sc = new Scanner(comp.getErrorStream());
                    if (error) {
                        while (sc.hasNextLine()) {
                            System.err.println("err:" + sc.nextLine());
                        }
                    }
                    sc.close();
                }
            });
            comp.waitFor();
            return true;
        } catch (IOException ex) {
            StandardPrints.printMsgWarning("Warning [IOException] execMethod()");
            return false;
        } catch (InterruptedException ex) {
            StandardPrints.printMsgWarning("Warning [InterruptedException] execMethod()");
            return false;
        }
    }
}
