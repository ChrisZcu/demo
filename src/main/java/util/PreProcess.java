package util;

import de.fhpotsdam.unfolding.geo.Location;
import model.Trajectory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PreProcess {
    public static void totalListInit(List<Trajectory> TrajTotal, String dataPath) {
        int trajId = 0;
        try {
            File theFile = new File(dataPath);
            LineIterator it = FileUtils.lineIterator(theFile, "UTF-8");
            String line;
            String[] data;
            try {
                while (it.hasNext()) {
                    line = it.nextLine();
                    Trajectory traj = new Trajectory(trajId);
                    trajId++;
                    String[] item = line.split(";");
                    data = item[1].split(",");
                    double score = Double.parseDouble(item[0]);
                    int j = 0;
                    for (; j < data.length - 2; j = j + 2) {
                        Location point = new Location(Double.parseDouble(data[j + 1]), Double.parseDouble(data[j]));
                        traj.points.add(point);
                    }
                    traj.setScore(score);
                    TrajTotal.add(traj);
                }
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void trajSortInit(List<ArrayList<Double>> sortList, String dataPath) {
        try {
            File theFile = new File(dataPath);
            LineIterator it = FileUtils.lineIterator(theFile, "UTF-8");
            String line;
            String[] data;
            try {
                while (it.hasNext()) {
                    line = it.nextLine();
                    String[] item = line.split(";");
                    data = item[1].split(",");
                    ArrayList<Double> listTmp = new ArrayList<>();
                    for (int j = 0; j < data.length - 2; j = j + 2) {
                        listTmp.add(Double.parseDouble(data[j + 1]));
                        listTmp.add(Double.parseDouble(data[j]));
                    }
                    sortList.add(listTmp);
                }
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void backgroundTrajInit(HashSet<Integer> bg_set, String file_path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file_path));
            String line;
            while ((line = reader.readLine()) != null) {
                bg_set.add(Integer.parseInt(line));
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}
