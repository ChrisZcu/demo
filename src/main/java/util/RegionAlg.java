package util;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import model.Position;
import model.Region;
import model.Trajectory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RegionAlg {

    public static List<Integer> getODTraj(List<Trajectory> list, Region r_o, Region r_d) {
        List<Integer> res = new ArrayList<>();

        for (Trajectory traj : list) {
            if (inCheck(r_o, traj.points.get(0)) && inCheck(r_d, traj.points.get(traj.points.size() - 1)))
                res.add(traj.getTrajId());
        }
        return res;
    }

    public static List<Integer> getODWTraj(List<Trajectory> list, Region r_o, Region r_d, ArrayList<Region> rWList) {
        List<Integer> res = new ArrayList<>();

        for (Trajectory traj : list) {
            if (inCheck(r_o, traj.points.get(0)) && inCheck(r_d, traj.points.get(traj.points.size() - 1)))
                for (int i = 1; i < traj.points.size() - 1; i++) {
                    //TODO add logic
                }
        }
        return res;
    }

    public static List<Integer> getWayPointTraj(List<Trajectory> list, ArrayList<Region> r_list) {
        List<Integer> res = new ArrayList<>();
        HashSet<Integer> resHash = new HashSet<>();
        for (Trajectory traj : list) {
            Location f_loc = traj.points.get(0);
            Location l_loc = traj.points.get(traj.points.size() - 1);
            boolean exist = false;
            for (Region r : r_list)
                if (inCheck(r, f_loc) || inCheck(r, l_loc)) {
                    exist = true;
                    break;
                }

            if (!exist)//首尾不存在
            //TODO add logic
            {
            }
        }
        return res;
    }


    public static List<Integer> getAllIn(List<Trajectory> list, Region r) {
        List<Integer> res = new ArrayList<>();
        for (Trajectory traj : list) {
            boolean in_flag = true;
            for (Location loc : traj.getPoints()) {
                if (!inCheck(r, loc)) {
                    in_flag = false;
                    break;
                }
            }
            if (in_flag)
                res.add(traj.getTrajId());
        }
        return res;
    }

    public static List<Integer> getAllIn2(List<Trajectory> list, Region r) {
        List<Integer> res = new ArrayList<>();

        Position left_top = r.left_top;
        Position right_btm = r.right_btm;

        for (Trajectory traj : list) {
            int trajId = traj.getTrajId();
            ArrayList<Double> listTmpX = SharedObject.getInstance().getTrajSortByX().get(trajId);
            ArrayList<Double> listTmpY = SharedObject.getInstance().getTrajSortByY().get(trajId);

            double xMin = listTmpX.get(0);
            double xMax = listTmpX.get(listTmpX.size() - 2);
            double yMin = listTmpY.get(0);
            double yMax = listTmpY.get(listTmpY.size() - 2);

            if (xMin >= left_top.x && xMax <= right_btm.x
                    && yMin >= left_top.y && yMax <= right_btm.y)
                res.add(trajId);
        }
        return res;
    }

    private static boolean inCheck(Region r, Location loc) {
        if (r == null)
            return true;
        UnfoldingMap map = SharedObject.getInstance().getMap();
        double px = map.getScreenPosition(loc).x;
        double py = map.getScreenPosition(loc).y;

        Position left_top = r.left_top;
        Position right_btm = r.right_btm;
        return (px >= left_top.x && px <= right_btm.x) && (py >= left_top.y && py <= right_btm.y);
    }

//    private static boolean intoWayPoint(Trajectory traj, Region r) {
//        Position left_top = r.left_top;
//        Position right_btm = r.right_btm;
//
//        UnfoldingMap map = SharedObject.getInstance().getMap();
//        double xMin = map.getLocation(left_top.x, left_top.y).x;
//        double xMax = map.getLocation(right_btm.x, right_btm.y).x;
//        double yMin = map.getLocation(left_top.x, left_top.y).y;
//        double yMax = map.getLocation(right_btm.x, right_btm.y).y;
//
//        int trajId = traj.getTrajId();
//
//        ArrayList<Double> tmpList = SharedObject.getInstance().getTrajSortByX().get(trajId);
//
//        if (tmpList.get(0) > xMax || tmpList.get(tmpList.size() - 2) < xMin
//                || SharedObject.getInstance().getTrajSortByY().get(trajId).get(1) > yMax ||
//                SharedObject.getInstance().getTrajSortByY().get(trajId).get(tmpList.size() - 1) < yMin) {
//            return false;
//        }
//
//        double[] ary = new double[tmpList.size() / 2];
//        for (int i = 0; i < tmpList.size(); i += 2) {
//            ary[i / 2] = tmpList.get(i);
//        }
//
//        int index1 = 0, index2 = 0;
//        int end1 = ary.length, end2 = ary.length;
//        boolean getId1 = false, getId2 = false;
//
//        while (!(getId1 && getId2)) {
//            int mid1 = (index1 + end1) / 2;
//            int mid2 = (index2 + end2) / 2;
//
//            if (index1 == end1)
//                getId1 = true;
//            if (index2 == end2)
//                getId2 = true;
//
//            if (ary[mid1] >= ary[index1]) {
//                end1 = mid1;
//            } else index1 = mid1;
//
//            if (ary[mid2] >= ary[index2]) {
//                end2 = mid2;
//            } else index2 = mid2;
//        }
//
//        for (int i = index1 * 2; i < index2 * 2 + 1; i += 2) {
//            double x = tmpList.get(i);
//            double y = tmpList.get(i + 1);
//            if (y > yMin)
//        }
//
//
//    }


}
