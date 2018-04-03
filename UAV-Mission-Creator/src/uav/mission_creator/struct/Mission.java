package uav.mission_creator.struct;

import uav.mission_creator.struct.geom.Line3D;
import uav.mission_creator.struct.geom.Poly3D;
import uav.mission_creator.struct.geom.LineGeo;
import uav.mission_creator.struct.geom.Point3D;
import uav.mission_creator.struct.geom.PolyGeo;
import uav.mission_creator.struct.geom.PointGeo;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jesimar S. Arantes
 */
public class Mission {
    
    private final List<Point3D> listPoint3D = new ArrayList<>();
    private final List<PointGeo> listPointGeo = new ArrayList<>();
    private final List<LineGeo> listLineGeo = new ArrayList<>();
    private final List<Line3D> listLine3D = new ArrayList<>();
    private final List<PolyGeo> listPolyGeo = new ArrayList<>();
    private final List<Poly3D> listPoly3D = new ArrayList<>();
    private PointGeo pointBase;
    
    public Mission(){
        
    }
    
    public void pointGeoTo3D(){
        specifyBase();
        for (PolyGeo poly : listPolyGeo){
            List<Point3D> listPoint = new ArrayList<>();
            for (int i = 0; i < poly.getNpoints(); i++){
                Point3D point = new Point3D(pointBase, poly.getPointGeo(i));
                listPoint.add(point);
            }
            Poly3D poly3d = new Poly3D(poly.getName(), listPoint);
            listPoly3D.add(poly3d);
        }
        for (LineGeo line : listLineGeo){
            List<Point3D> listPoint = new ArrayList<>();
            for (int i = 0; i < line.getNpoints(); i++){
                Point3D point = new Point3D(pointBase, line.getPointGeo(i));
                listPoint.add(point);
            }
            Line3D line3d = new Line3D(line.getName(), listPoint);
            listLine3D.add(line3d);
        }
        for (PointGeo point : listPointGeo){
            Point3D p3d = new Point3D(pointBase, point);
            listPoint3D.add(p3d);
        }
    }
    
    public void addPoint(PointGeo point){
        listPointGeo.add(point);
    }
    
    public void addFirstPoint(PointGeo point){
        listPointGeo.add(0, point);
    }
    
    public void addLine(LineGeo line){
        listLineGeo.add(line);
    }
    
    public void addFirstLine(LineGeo line){
        listLineGeo.add(0, line);
    }
    
    public void addPoly(PolyGeo poly){
        listPolyGeo.add(poly);
    }
    
    public void addFirstPoly(PolyGeo poly){
        listPolyGeo.add(0, poly);
    }
    
    public List<PointGeo> getListPoint() {
        return listPointGeo;
    }
    
    public List<Point3D> getListPoint3D() {
        return listPoint3D;
    }

    public List<LineGeo> getListLine() {
        return listLineGeo;
    }
    
    public List<Line3D> getListLine3D() {
        return listLine3D;
    }

    public List<PolyGeo> getListPoly() {
        return listPolyGeo;
    }   
    
    public List<Poly3D> getListPoly3D() {
        return listPoly3D;
    } 
    
    public int getSizeListPolyGeo() {
        return listPolyGeo.size();
    } 
    
    public int getSizeListLineGeo() {
        return listLineGeo.size();
    }
    
    public PointGeo getPointBase(){
        return pointBase;
    }
    
    public int getSizeObstacle() {
        int size = 0;
        for (PolyGeo p : listPolyGeo){
            if (p.getName().contains(KeyWords.MAP_OBSTACLE)){
                size++;
            }
        }
        return size;
    } 
    
    public int getSizeBonus() {
        int size = 0;
        for (PolyGeo p : listPolyGeo){
            if (p.getName().contains(KeyWords.MAP_BONUS)){
                size++;
            }
        }
        return size;
    } 
    
    public int getSizePenalty() {
        int size = 0;
        for (PolyGeo p : listPolyGeo){
            if (p.getName().contains(KeyWords.MAP_PENALTY)){
                size++;
            }
        }
        return size;
    }        
    
    public int getSizePattern(String pattern) {
        int size = 0;
        for (PolyGeo p : listPolyGeo){
            if (p.getName().contains(pattern)){
                size++;
            }
        }
        return size;
    }        
    
    public String getPolyVetX(int i){
        String str = "";
        for (int j = 0; j < listPolyGeo.get(i).getNpoints()-1; j++){
            str += listPolyGeo.get(i).getVetx()[j] + ",";
        }
        str += listPolyGeo.get(i).getVetx()[listPolyGeo.get(i).getNpoints()-1];
        return str;
    }
    
    public String getPoly3DVetX(int i){
        String str = "";
        for (int j = 0; j < listPoly3D.get(i).getNpoints()-1; j++){
            str += listPoly3D.get(i).getVetx()[j] + ",";
        }
        str += listPoly3D.get(i).getVetx()[listPoly3D.get(i).getNpoints()-1];
        return str;
    }
    
    public String getPolyVetY(int i){
        String str = "";
        for (int j = 0; j < listPolyGeo.get(i).getNpoints()-1; j++){
            str += listPolyGeo.get(i).getVety()[j] + ",";
        }
        str += listPolyGeo.get(i).getVety()[listPolyGeo.get(i).getNpoints()-1];
        return str;
    }
    
    public String getPoly3DVetY(int i){
        String str = "";
        for (int j = 0; j < listPoly3D.get(i).getNpoints()-1; j++){
            str += listPoly3D.get(i).getVety()[j] + ",";
        }
        str += listPoly3D.get(i).getVety()[listPoly3D.get(i).getNpoints()-1];
        return str;
    }
    
    private void specifyBase(){        
        for (PointGeo point : listPointGeo) {
            if (point.getName().contains(KeyWords.GEO_BASE)) {
                double lon = point.getLng();                
                double lat = point.getLat();
                double alt = point.getAlt();
                pointBase = new PointGeo(KeyWords.GEO_BASE, lon, lat, alt);
            }
        }
    }
    
    public int getSizeWaypoints() {
        int size = 0;
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.WAYPOINT)){
                size++;
            }
        }
        return size;
    }
    
    public String getWaypointsGeo(){
        String str = "";
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.WAYPOINT)){
                str += point.toString();
            }
        }
        return str;
    }
    
    public String getWaypoints3D(){
        String str = "";
        for (Point3D point : listPoint3D){
            if (point.getName().contains(KeyWords.WAYPOINT)){
                str += point.toString();
            }
        }
        return str;
    }
    
    public String getWaypointsGeoLabel(){
        String str = "";
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.WAYPOINT)){                
                str += point.getName() + "\n" + point.toString2();
            }
        }
        return str;
    }
    
    public String getWaypointsBuzzer(){
        String str = "";
        int i = 1;
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.WAYPOINT)){
                str += "turn-on-the-buzzer_" + i + "\n" + point.toString2();
                i++;
            }
        }
        return str;
    } 
    
    public String getWaypointsCameraPhoto(){
        String str = "";
        int i = 1;
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.PHOTO)){
                str += "take-a-picture_" + i + "\n" + point.toString2();
                i++;
            }
        }
        return str;
    }
    
    public double getHeightFly() {
        double heightFly = 0;
        for (PolyGeo poly : getListPoly()) {
            if (poly.getName().contains("fly")) {
                heightFly = poly.getVetz()[0];
            }
        }
        return heightFly;
    }
    
    public boolean hasCameraPhoto(){
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.PHOTO)){
                return true;
            }
        }
        return false;
    }
    
    public boolean hasFrontier(){
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.FRONTIER)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasPointFailure(){
        for (PointGeo point : listPointGeo){
            if (point.getName().contains(KeyWords.POINT_FAILURE)) {
                return true;
            }
        }
        return false;
    }
    
}