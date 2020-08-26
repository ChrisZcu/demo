package util;

import de.fhpotsdam.unfolding.UnfoldingMap;
import model.Region;
import model.Trajectory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static util.COLOR.*;

public class SharedObject {

    private static SharedObject instance = new SharedObject();

    private SharedObject() {
    }

    public static SharedObject getInstance() {
        return instance;
    }

    /**
     * regions s
     */
    private static Region regionA = null;
    private static Region regionO = null;
    private static Region regionD = null;
    private static Region regionW = null;

    /**
     * trajectory info
     */
    private static List trajTotal;
    private static HashSet<Integer> bgSet;
    private static List<ArrayList<Double>> trajSortByX;
    private static List<ArrayList<Double>> trajSortByY;

    private static UnfoldingMap map;

    private Color[] colors = new Color[]{
            new Color(255, 32, 28),
            new Color(124, 99, 28),
            new Color(8, 99, 28),
            new Color(22, 0, 103),
            new Color(137, 48, 163),
            new Color(255, 253, 0)};

    private COLOR[] all_color = {RED, BROWN, GREEN, BLUE, PINK, YELLOW};

    public void clearRegion() {
        regionA = regionD = regionO = regionW = null;
    }

    public void initTotalRegion(List tt, Region r_a, Region r_o, Region r_d, Region r_w) {
        trajTotal = tt;
        regionA = r_a;
        regionO = r_o;
        regionD = r_d;
        regionW = r_w;
    }

    public void initRegion(Region r_a, Region r_o, Region r_d, Region r_w) {
        regionA = r_a;
        regionO = r_o;
        regionD = r_d;
        regionW = r_w;
    }

    public void initTrajList(List tt) {
        trajTotal = tt;
    }

    public void initSortListX(List listX) {
        trajSortByX = listX;
    }

    public void initSortListY(List listY) {
        trajSortByY = listY;
    }

    public void initBgSet(HashSet<Integer> set) {
        bgSet = set;
    }

    public void initRA(Region r_a) {
        regionA = r_a;
    }

    public void initRO(Region r_o) {
        regionO = r_o;
    }

    public void initRD(Region r_d) {
        regionD = r_d;
    }

    public void initRW(Region r_w) {
        regionW = r_w;
    }

    public void initMap(UnfoldingMap map) {
        SharedObject.map = map;
    }

    public UnfoldingMap getMap() {
        return map;
    }

    public List<Trajectory> getTotalTraj() {
        return trajTotal;
    }

    public Region getRegionA() {
        return regionA;
    }

    public Region getRegionO() {
        return regionO;
    }

    public Region getRegionD() {
        return regionD;
    }

    public Region getRegionW() {
        return regionW;
    }

    public Region[] getAllRegions() {
        return new Region[]{regionA, regionO, regionD, regionW};
    }

    public Color[] getColors() {
        return colors;
    }

    public COLOR[] getColorList() {
        return all_color;
    }

    public HashSet<Integer> getBgSet() {
        return bgSet;
    }

    public List<ArrayList<Double>> getTrajSortByX() {
        return trajSortByX;
    }

    public List<ArrayList<Double>> getTrajSortByY() {
        return trajSortByY;
    }

}


