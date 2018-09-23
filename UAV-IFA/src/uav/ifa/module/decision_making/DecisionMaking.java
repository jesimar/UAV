package uav.ifa.module.decision_making;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import lib.color.StandardPrints;
import uav.generic.module.data_communication.DataCommunication;
import uav.generic.module.sensors_actuators.ParachuteControl;
import uav.generic.util.UtilString;
import uav.generic.hardware.aircraft.Drone;
import uav.generic.struct.constants.TypeWaypoint;
import uav.generic.struct.mission.Mission;
import uav.generic.struct.Waypoint;
import uav.generic.struct.constants.Constants;
import uav.generic.struct.constants.TypeAircraft;
import uav.generic.struct.constants.TypeInputCommand;
import uav.generic.struct.constants.TypeOperationMode;
import uav.generic.struct.constants.TypeReplanner;
import uav.generic.struct.constants.TypeSystemExecIFA;
import uav.generic.struct.reader.ReaderFileConfig;
import uav.generic.struct.states.StateReplanning;
import uav.ifa.module.communication_control.CommunicationGCS;
import uav.ifa.module.path_replanner.DE4s;
import uav.ifa.module.path_replanner.PrePlanned4s;
import uav.ifa.module.path_replanner.GA4s;
import uav.ifa.module.path_replanner.GA_GA_4s;
import uav.ifa.module.path_replanner.GA_GH_4s;
import uav.ifa.module.path_replanner.GH4s;
import uav.ifa.module.path_replanner.MPGA4s;
import uav.ifa.module.path_replanner.MS4s;
import uav.ifa.module.path_replanner.Replanner;
import uav.ifa.struct.Failure;

/**
 * Classe que faz as tomadas de decisão do sistema IFA.
 *
 * @author Jesimar S. Arantes
 */
public class DecisionMaking {

    private final Drone drone;
    private final DataCommunication dataAcquisition;
    private final ReaderFileConfig config;
    private Replanner replanner;
    private StateReplanning stateReplanning;
    private String typeAction = "";
    private final double DISPLACEMENT_FACTOR;
    private final double ONE_METER = Constants.ONE_METER;

    /**
     * Class constructor.
     *
     * @param drone instance of the aircraft
     * @param dataAcquisition object to send commands to drone
     */
    public DecisionMaking(Drone drone, DataCommunication dataAcquisition) {
        this.config = ReaderFileConfig.getInstance();
        this.drone = drone;
        this.dataAcquisition = dataAcquisition;
        this.stateReplanning = StateReplanning.WAITING;
        this.DISPLACEMENT_FACTOR = config.getDisplacFactorController();
    }

    public void actionToDoSomethingOffboard(Failure failure, CommunicationGCS communicationGCS) {
        stateReplanning = StateReplanning.REPLANNING;
        if (config.getSystemExecIFA().equals(TypeSystemExecIFA.REPLANNER)) {
            if (failure.getTypeFailure() != null) {
                switch (failure.getTypeFailure()) {
                    case FAIL_AP_POWEROFF: //feito no proprio cc (offboard nao faz sentido)
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_AP_EMERGENCY: //feito no proprio cc (offboard nao faz sentido)
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_AP_CRITICAL:
                        execEmergencyLandingOffboard(communicationGCS);
                        break;
                    case FAIL_GPS: //feito no proprio cc (offboard nao faz sentido)
                        if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                            landVertical();
                        } else {
                            if (config.hasParachute()) {
                                openParachute();
                            }
                        }
                        break;
                    case FAIL_ENGINE: //feito no proprio cc (offboard nao faz sentido)
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_SYSTEM_IFA: //feito no proprio cc (offboard nao faz sentido)
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_LOW_BATTERY:
                        execEmergencyLandingOffboard(communicationGCS);
                        break;
                    case FAIL_BATTERY_OVERHEATING:
                        execEmergencyLandingOffboard(communicationGCS);
                        break;
                    case FAIL_SYSTEM_MOSA:
                        execEmergencyLandingOffboard(communicationGCS);
                        break;
                    case FAIL_BASED_INSERT_FAILURE:
                        if (typeAction.equals(TypeInputCommand.CMD_EMERGENCY_LANDING)) {
                            execEmergencyLandingOffboard(communicationGCS);
                        } else if (typeAction.equals(TypeInputCommand.CMD_LAND)) {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        } else if (typeAction.equals(TypeInputCommand.CMD_RTL)) {
                            RTL();
                        }
                        break;
                    case FAIL_BAD_WEATHER: //feito no proprio cc (offboard nao faz sentido)
                        RTL();
                    default:
                        break;
                }
            }
        }
        stateReplanning = StateReplanning.READY;
    }

    private void execEmergencyLandingOffboard(CommunicationGCS communicationGCS) {
        boolean itIsOkEmergencyLanding = emergenyLandingOffboard(communicationGCS);
        if (!itIsOkEmergencyLanding) {
            if (config.hasParachute()) {
                openParachute();
            } else {
                landVertical();
            }
        }
    }

    private boolean emergenyLandingOffboard(CommunicationGCS communicationGCS) {
        double navSpeed = drone.getListParameters().getValue("WPNAV_SPEED");
        dataAcquisition.changeNavigationSpeed(navSpeed/10);

        String attributes = config.getTypeReplanner() 
                + ";" + config.getDirFiles() 
                + ";" + config.getFileGeoBase()
                + ";" + config.getDirReplanner() 
                + ";" + config.getCmdExecReplanner()
                + ";" + config.getTypeAltitudeDecayReplanner()
                + ";" + config.getTimeExecReplanner()
                + ";" + config.getNumberWaypointsReplanner() 
                + ";" + config.getDeltaReplanner();
        communicationGCS.sendDataReplannerInGCS(attributes);
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {

            }
        } while (!communicationGCS.hasReceiveRouteGCS());

        String msgRoute = communicationGCS.getRouteReplannerGCS();

        dataAcquisition.changeNavigationSpeed(navSpeed);

        if (msgRoute.equals("failure")) {
            System.out.println("Route GCS [Failure]: " + msgRoute);
            return false;
        } else {
            dataAcquisition.setMission(msgRoute);
            return true;
        }
    }

    public void actionToDoSomethingOnboard(Failure failure) {
        stateReplanning = StateReplanning.REPLANNING;
        if (config.getSystemExecIFA().equals(TypeSystemExecIFA.REPLANNER)) {
            if (failure.getTypeFailure() != null) {
                switch (failure.getTypeFailure()) {
                    case FAIL_AP_POWEROFF:
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_AP_EMERGENCY:
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_AP_CRITICAL:
                        execEmergencyLanding();
                        break;
                    case FAIL_GPS:
                        if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                            landVertical();
                        } else {
                            if (config.hasParachute()) {
                                openParachute();
                            }
                        }
                        break;
                    case FAIL_ENGINE:
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_SYSTEM_IFA:
                        if (config.hasParachute()) {
                            openParachute();
                        } else {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        }
                        break;
                    case FAIL_LOW_BATTERY:
                        execEmergencyLanding();
                        break;
                    case FAIL_BATTERY_OVERHEATING:
                        execEmergencyLanding();
                        break;
                    case FAIL_SYSTEM_MOSA:
                        execEmergencyLanding();
                        break;
                    case FAIL_BASED_INSERT_FAILURE:
                        if (typeAction.equals(TypeInputCommand.CMD_EMERGENCY_LANDING)) {
                            execEmergencyLanding();
                        } else if (typeAction.equals(TypeInputCommand.CMD_LAND)) {
                            if (config.getTypeAircraft().equals(TypeAircraft.ROTARY_WING)) {
                                landVertical();
                            }
                        } else if (typeAction.equals(TypeInputCommand.CMD_RTL)) {
                            RTL();
                        }
                        break;
                    case FAIL_BAD_WEATHER:
                        RTL();
                    default:
                        break;
                }
            }
        } else if (config.getSystemExecIFA().equals(TypeSystemExecIFA.FIXED_ROUTE)) {
            boolean respF = sendFixedRouteToDrone();
            if (respF) {
                stateReplanning = StateReplanning.READY;
                StandardPrints.printMsgEmph("send fixed mission with success");
            } else {
                stateReplanning = StateReplanning.DISABLED;
                StandardPrints.printMsgWarning("send fixed mission to drone failure");
                return;
            }
        } 
        stateReplanning = StateReplanning.READY;
    }

    /**
     * Este comando chama o algoritmo de pouso emergencial caso algo dê errado
     * então o paraquedas é disparado.
     */
    private void execEmergencyLanding() {
        boolean itIsOkEmergencyLanding = emergenyLanding();
        if (!itIsOkEmergencyLanding) {
            if (config.hasParachute()) {
                openParachute();
            } else {
                landVertical();
            }
        }
    }

    /**
     * Este comando calcula uma rota de pouso emergencial usando algum algoritmo
     * configurado para isso, então passa a nova rota para o AutoPilot.
     *
     * @return true - se ocorrer tudo bem.
     * <br> false - caso contrário.
     */
    private boolean emergenyLanding() {
        double navSpeed = drone.getListParameters().getValue("WPNAV_SPEED");
        dataAcquisition.changeNavigationSpeed(navSpeed / 10);

        StandardPrints.printMsgEmph("decison making -> emergeny landing: " + typeAction);
        if (config.getTypeReplanner().equals(TypeReplanner.GH4S)) {
            replanner = new GH4s(drone);
        } else if (config.getTypeReplanner().equals(TypeReplanner.GA4S)) {
            replanner = new GA4s(drone);
        } else if (config.getTypeReplanner().equals(TypeReplanner.MPGA4S)) {
            replanner = new MPGA4s(drone);
        } else if (config.getTypeReplanner().equals(TypeReplanner.MS4S)) {
            replanner = new MS4s(drone);
        } else if (config.getTypeReplanner().equals(TypeReplanner.DE4S)) {
            replanner = new DE4s(drone);
        } else if (config.getTypeReplanner().equals(TypeReplanner.GA_GA_4S)) {
            replanner = new GA_GA_4s(drone);
        } else if (config.getTypeReplanner().equals(TypeReplanner.GA_GH_4S)) {
            replanner = new GA_GH_4s(drone);
        } else if (config.getTypeReplanner().equals(TypeReplanner.PRE_PLANNED4s)) {
            replanner = new PrePlanned4s(drone);
        }
        replanner.clearLogs();
        boolean itIsOkExec = replanner.exec();
        if (!itIsOkExec) {
            return false;
        }

        dataAcquisition.changeNavigationSpeed(navSpeed);

        Mission mission = new Mission();
        String path = config.getDirReplanner() + "routeGeo.txt";

        if (config.getTypeReplanner().equals(TypeReplanner.GA_GA_4S)) {
            System.out.println("TypeReplanner.GA_GA_4S");
            path = "../Modules-IFA/GA4s/" + "routeGeo.txt";
        } else if (config.getTypeReplanner().equals(TypeReplanner.GA_GH_4S)) {
            String best = ((GA_GH_4s) replanner).bestMethod();
            if (best.equals("GA")) {
                path = "../Modules-IFA/GA4s/" + "routeGeo.txt";
            } else if (best.equals("GH")) {
                path = "../Modules-IFA/GH4s/" + "routeGeo.txt";
            } else {
                return false;
            }
        }

        boolean resp = readFileRoute(mission, path, 0);
        if (!resp) {
            return false;
        }
        if (mission.getMission().size() > 0) {
            mission.printMission();
            dataAcquisition.setMission(mission);
        }
        return true;
    }

    /**
     * Este comando guia a aeronave até a posição especificada e então pousa
     * verticalmente quando o veículo eh um multi-rotor.
     *
     * @param lat latitude da região onde ocorrerá o pouso.
     * @param lng longitude da região onde ocorrerá o pouso.
     */
    private void land(double lat, double lng) {
        StandardPrints.printMsgEmph("decison making -> land");
        Waypoint wpt = new Waypoint(TypeWaypoint.LAND, lat, lng, 0.0);
        dataAcquisition.setWaypoint(wpt);
    }

    /**
     * Este comando pousa a aeronave verticalmente no local em que a aeronave
     * estava quando esse comando foi chamado.
     */
    private void landVertical() {
        StandardPrints.printMsgEmph("decison making -> land vertical");
        Waypoint wpt = new Waypoint(TypeWaypoint.LAND_VERTICAL, 0.0, 0.0, 0.0);
        dataAcquisition.setWaypoint(wpt);
    }

    /**
     * Este comando faz a aeronave voltar ao ponto de lançamento, para isso a
     * aeronave primeiramente sobe até a altitude RTL_ALT então volta ao local
     * de lançamento e por fim pousa na vertical quando é um multi-rotor.
     */
    private void RTL() {
        if (config.hasPowerModule()
                || !config.getOperationMode().equals(TypeOperationMode.REAL_FLIGHT)) {
            if (drone.getBattery().level > drone.getEstimatedConsumptionBatForRTL()){
                StandardPrints.printMsgEmph("decison making -> RTL");
                Waypoint wpt = new Waypoint(TypeWaypoint.RTL, 0.0, 0.0, 0.0);
                dataAcquisition.setWaypoint(wpt);
            }else{
                StandardPrints.printMsgEmph("battery is not enough to do RTL.");
                StandardPrints.printMsgEmph("decison making -> RTL -> changed to -> land vertical");
                landVertical();
            }
        }else{
            StandardPrints.printMsgEmph("decison making -> RTL");
            Waypoint wpt = new Waypoint(TypeWaypoint.RTL, 0.0, 0.0, 0.0);
            dataAcquisition.setWaypoint(wpt);
        }
    }

    /**
     * Este comando é responsável por fazer o disparo do paraquedas. Melhorar no
     * futuro: Deve-se desarmar o motor e entao abrir o paraquedas.
     */
    public void openParachute() {
        StandardPrints.printMsgEmph("decison making -> open parachute");
        ParachuteControl parachute = new ParachuteControl();
        boolean isOpen = parachute.open();
        if (!isOpen) {
            stateReplanning = StateReplanning.DISABLED;
        }
//        Waypoint wpt = new Waypoint(TypeWaypoint.LAND_VERTICAL, 0.0, 0.0, 0.0);
//        dataAcquisition.setWaypoint(wpt);
    }

    /**
     * Retorna o estado do planejador [WAITING, REPLANNING, READY, DISABLED].
     *
     * @return estado do planejador.
     */
    public StateReplanning getStateReplanning() {
        return stateReplanning;
    }

    /**
     * Define o tipo de ação executada pelo sistema de decisão do IFA [CMD_MPGA,
     * CMD_LAND, CMD_RTL].
     *
     * @param action representa a ação a ser executada.
     */
    public void setTypeAction(String action) {
        this.typeAction = action;
    }

    private boolean sendFixedRouteToDrone() {
        StandardPrints.printMsgEmph("send fixed route");
        String path = config.getDirFixedRouteIFA()+ config.getFileFixedRouteIFA();
        Mission route = new Mission();
        boolean resp = readFileRoute(route, path, 2);
        if (!resp) {
            return false;
        }
        route.printMission();
        if (route.getMission().size() > 0) {
            dataAcquisition.setMission(route);
        }
        return true;
    }

    private boolean readFileRoute(Mission wps, String path, int time) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String sCurrentLine;
            double lat = 0.0;
            double lng = 0.0;
            double alt = 0.0;
            while ((sCurrentLine = br.readLine()) != null) {
                sCurrentLine = UtilString.changeValueSeparator(sCurrentLine);
                String s[] = sCurrentLine.split(";");
                lat = Double.parseDouble(s[0]);
                lng = Double.parseDouble(s[1]);
                alt = Double.parseDouble(s[2]);
                if (time > 1) {
                    wps.addWaypoint(new Waypoint(TypeWaypoint.GOTO, lat, lng, alt));
                }
                time++;
            }
            if (wps.getMission().size() > 0) {
                wps.addWaypoint(new Waypoint(TypeWaypoint.LAND, lat, lng, 0.0));
            }
            return true;
        } catch (FileNotFoundException ex) {
            StandardPrints.printMsgWarning("Warning [FileNotFoundException]: readFileRoute()");
            return false;
        } catch (IOException ex) {
            StandardPrints.printMsgWarning("Warning [IOException]: readFileRoute()");
            return false;
        }
    }

    public void interpretCommand(String cmd) {
        if (cmd.contains(TypeInputCommand.CMD_TAKEOFF)) {
            Waypoint wpt = new Waypoint(TypeWaypoint.TAKEOFF, 0.0, 0.0, 3.0);
            dataAcquisition.setWaypoint(wpt);
        }
        if (cmd.contains(TypeInputCommand.CMD_LAND)) {
            Waypoint wpt = new Waypoint(TypeWaypoint.LAND_VERTICAL, 0.0, 0.0, 0.0);
            dataAcquisition.setWaypoint(wpt);
        }
        if (cmd.contains(TypeInputCommand.CMD_UP)) {
            double lat = drone.getGPS().lat;
            double lng = drone.getGPS().lng;
            double newAltRel = drone.getBarometer().alt_rel + 1;
            if (newAltRel > config.getMaxAltController()) {
                newAltRel = config.getMaxAltController();
            }
            Waypoint wpt = new Waypoint(TypeWaypoint.GOTO, lat, lng, newAltRel);
            dataAcquisition.setWaypoint(wpt);
        }
        if (cmd.contains(TypeInputCommand.CMD_DOWN)) {
            double lat = drone.getGPS().lat;
            double lng = drone.getGPS().lng;
            double newAltRel = drone.getBarometer().alt_rel - 1;
            if (newAltRel < config.getMinAltController()) {
                newAltRel = config.getMinAltController();
            }
            Waypoint wpt = new Waypoint(TypeWaypoint.GOTO, lat, lng, newAltRel);
            dataAcquisition.setWaypoint(wpt);
        }
        if (cmd.contains(TypeInputCommand.CMD_LEFT)) {
            double lat = drone.getGPS().lat;
            double newLon = drone.getGPS().lng - DISPLACEMENT_FACTOR * ONE_METER;
            double alt_rel = drone.getBarometer().alt_rel;
            Waypoint wpt = new Waypoint(TypeWaypoint.GOTO, lat, newLon, alt_rel);
            dataAcquisition.setWaypoint(wpt);
        }
        if (cmd.contains(TypeInputCommand.CMD_RIGHT)) {
            double lat = drone.getGPS().lat;
            double newLon = drone.getGPS().lng + DISPLACEMENT_FACTOR * ONE_METER;
            double alt_rel = drone.getBarometer().alt_rel;
            Waypoint wpt = new Waypoint(TypeWaypoint.GOTO, lat, newLon, alt_rel);
            dataAcquisition.setWaypoint(wpt);
        }
        if (cmd.contains(TypeInputCommand.CMD_FORWARD)) {
            double newLat = drone.getGPS().lat + DISPLACEMENT_FACTOR * ONE_METER;
            double lng = drone.getGPS().lng;
            double alt_rel = drone.getBarometer().alt_rel;
            Waypoint wpt = new Waypoint(TypeWaypoint.GOTO, newLat, lng, alt_rel);
            dataAcquisition.setWaypoint(wpt);
        }
        if (cmd.contains(TypeInputCommand.CMD_BACK)) {
            double newLat = drone.getGPS().lat - DISPLACEMENT_FACTOR * ONE_METER;
            double lng = drone.getGPS().lng;
            double alt_rel = drone.getBarometer().alt_rel;
            Waypoint wpt = new Waypoint(TypeWaypoint.GOTO, newLat, lng, alt_rel);
            dataAcquisition.setWaypoint(wpt);
        }
    }

}
