package app;

import com.sun.awt.AWTUtilities;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import model.Position;
import model.Region;
import model.Trajectory;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import processing.core.PApplet;
import processing.core.PImage;
import select.SelectHandle;
import util.*;


import java.awt.BorderLayout;
import java.awt.Dimension;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static org.lwjgl.opengl.GL.createCapabilities;
import static util.METHOD.FULL;
import static util.REGION.*;
import static util.REGION.O_D;

public class ProcessingDemo3_2 extends PApplet {

    private static JWindow controlWindow; // control component
    private boolean drawOrigion = false;
    private boolean drawDestination = false;
    private boolean drawWayPoint = false;
    private boolean fullData = false;
    private boolean ranData = false;
    private boolean vfgsData = false;

    private boolean selectDone = false;
    private boolean VFGS = false;
    private boolean mapChange = false;
    private boolean mouseDragged = false;
    private boolean mousePressed = false;
    private boolean screenShot = false;

    private int THREADNUM = 10;
    private String[][] trajIndexAry;
    private ArrayList<Integer> trajShowIdListFull = new ArrayList<>();
    private ArrayList<Integer> vfgsShowIdListFull = new ArrayList<>();
    private ArrayList<Integer> ranShowIdListFull = new ArrayList<>();

    //map
    private UnfoldingMap mapOverview;
    private UnfoldingMap mapFullData;
    private UnfoldingMap mapRandom;
    private UnfoldingMap mapVFGS;
    private UnfoldingMap[] mapList = new UnfoldingMap[4];
    private static final Location PORTO_CENTER = new Location(41.14, -8.639);//维度经度
    private static final Location PRESENT = PORTO_CENTER;

    private PImage mapImage = null;
    private boolean totalLoadMap0 = false;
    private boolean totalLoadMap1 = false;
    private boolean totalLoadMap2 = false;
    private boolean totalLoadMap3 = false;
    private boolean[] mapLoadListFlag;
    private int checkLevel = -1;
    private Location checkCenter = new Location(-1, -1);
    private boolean dragContral = false;

    private Region lastClickRegion = new Region();
    private Region dragRegion = new Region();
    private int regionId = -1;
    private int totalRegionNum = 0;
    private Position mouseClick;

    private boolean zoomPan = false;

    private int circleSize = 19;
    private static String totalTrajFilePath = "E:\\zcz\\dbgroup\\data\\Portugal\\GPS\\PortoFull.txt";
    private static String vfgsFilePath = "E:\\zcz\\dbgroup\\data\\Portugal\\VFGS_res\\vfgs_0.01_50.txt";

    private static void preProcess() {
        // init shared object total trajectory list


        List<Trajectory> trajTotal = new ArrayList<>();
        HashSet<Integer> vfgsSet = new HashSet<>();
        PreProcess.totalListInit(trajTotal, totalTrajFilePath);
        PreProcess.backgroundTrajInit(vfgsSet, vfgsFilePath);

        SharedObject.getInstance().initTrajList(trajTotal);
        SharedObject.getInstance().initBgSet(vfgsSet);

        System.out.println("pre-process done");
        System.out.println("total trajectory number: " + trajTotal.size());
    }

    public void settings() {
        size(1010, 810, P2D);
    }

    // map set
    @Override
    public void setup() {
        String mapStyle = "https://api.mapbox.com/styles/v1/pacemaker-yc/ck4gqnid305z61cp1dtvmqh5y/tiles/512/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoicGFjZW1ha2VyLXljIiwiYSI6ImNrNGdxazl1aTBsNDAzZW41MDhldmQyancifQ.WPAckWszPCEHWlyNmJfY0A";
        background(0);

        surface.setTitle("Processing with AWT");
        surface.setLocation(0, 0);

        trajIndexAry = new String[THREADNUM][];

        // **声明map
        //overview, 左上
        mapOverview = new UnfoldingMap(this, 0, 0, 500, 400, new MapBox.CustomMapBoxProvider(mapStyle));
        mapOverview.setZoomRange(1, 20);
        mapOverview.zoomAndPanTo(11, PRESENT);
        mapOverview.setBackgroundColor(255);
        mapOverview.setTweening(true);

        //**MapFullData, 右上
        mapFullData = new UnfoldingMap(this, 510, 0, 500, 400, new MapBox.CustomMapBoxProvider(mapStyle));
        mapFullData.setZoomRange(1, 20);
        mapFullData.zoomAndPanTo(11, PRESENT);
        mapFullData.setBackgroundColor(255);
        mapFullData.setTweening(true);


        //**MapRandom, 左下
        mapRandom = new UnfoldingMap(this, 0, 410, 500, 400, new MapBox.CustomMapBoxProvider(mapStyle));
        mapRandom.setZoomRange(1, 20);
        mapRandom.zoomAndPanTo(11, PRESENT);
        mapRandom.setBackgroundColor(255);
        mapRandom.setTweening(true);

        //**MapVFGS, 右下
        mapVFGS = new UnfoldingMap(this, 510, 410, 500, 400, new MapBox.CustomMapBoxProvider(mapStyle));
        mapVFGS.setZoomRange(1, 20);
        mapVFGS.zoomAndPanTo(11, PRESENT);
        mapVFGS.setBackgroundColor(255);
        mapVFGS.setTweening(true);

        mapList = new UnfoldingMap[]{mapOverview, mapFullData, mapRandom, mapVFGS};
        mapLoadListFlag = new boolean[]{totalLoadMap0, totalLoadMap1, totalLoadMap2, totalLoadMap3};

        EventDispatcher eventDispatcher = MapUtils.createDefaultEventDispatcher(this, mapOverview);

        SharedObject.getInstance().initMap(mapOverview);

        for (int i = 1; i < 4; i++) {
            UnfoldingMap map = mapList[i];
            eventDispatcher.register(map, "pan", mapOverview.getId());
            eventDispatcher.register(map, "zoom", mapOverview.getId());
        }

        try {
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();//设置外观风格，和os保持一致
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            println("--well yeah something went wrong but i dont think we needa know that");
        }

        //-- ** Swing UI setting
        createSwingWindow();
    }

    @Override
    public void draw() {
        if (screenShot)
            screenShot();
        for (int i = 0; i < 4; i++)
            drawMap(mapList[i], i);

        if (!zoomPan) {
            drawTraj(0, 500, 0, 400, 0, 0, trajShowIdListFull);
            drawTraj(0, 500, 0, 400, 510, 0, vfgsShowIdListFull);
            drawTraj(0, 500, 0, 400, 0, 410, ranShowIdListFull);
            drawRegion(COLOR.GREEN, SharedObject.getInstance().getRegionO());
            drawRegion(COLOR.BLUE, SharedObject.getInstance().getRegionD());
            for (Region r_w : SharedObject.getInstance().getRegionWList()) {
                drawRegion(COLOR.PINK, r_w);
            }
            if (!dragContral)
                stopLoop();
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'q')
            zoomPan = !zoomPan;
        else if (key == 'w') {
            saveRegion();
        } else if (key == 'e') {
            trajShowIdListFull.clear();
            vfgsShowIdListFull.clear();
            ranShowIdListFull.clear();
            loop();
        } else if (key == 'a')
            createConvas();
        if (zoomPan)
            loop();
        System.out.println(zoomPan);
    }

    private static String regionDirPath = "data/GPSRegion";
    private static String shotDir = "data/screenshot";

    private void saveRegion() {
        File dir = new File(regionDirPath);
        int post = Objects.requireNonNull(dir.listFiles()).length;
        String filePath = regionDirPath + "/region_" + post + ".txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
            writer.write("Order: left-top, right-top,right-btm, left-btm-------------\n" +
                    "-------------O-------------\n" +
                    "-------------W List-------------\n" +
                    "-------------D-------------\n\n\n");
            for (Region r : SharedObject.getInstance().getAllRegions()) {
                if (r == null)
                    writer.write("NULL\n");
                else {
                    Location l1 = mapOverview.getLocation(r.left_top.x, r.left_top.y);
                    Location l2 = mapOverview.getLocation(r.right_btm.x, r.left_top.y);
                    Location l3 = mapOverview.getLocation(r.right_btm.x, r.right_btm.y);
                    Location l4 = mapOverview.getLocation(r.left_top.x, r.right_btm.y);

                    writer.write(l1.toString() + ", " + l2.toString() + ", " + l3.toString() + ", " + l4.toString() + "\n");
                }
                writer.write("--------------------------------\n");

            }
            writer.close();

        } catch (IOException e) {
            System.err.println(e);
        }


    }

    @Override
    public void mousePressed() {
        mouseClick = new Position(mouseX, mouseY);
        {
            System.out.println(mapOverview.getLocation(mouseClick.x, mouseClick.y));
            new Thread() {
                @Override
                public void run() {
                    // init the region if finished
                    if (lastClickRegion.left_top == null) {
                        lastClickRegion.left_top = mouseClick;
                    } else {
                        Position l_t = lastClickRegion.left_top;
                        if (l_t.x < mouseClick.x) {//left
                            if (l_t.y < mouseClick.y) {//up
                                lastClickRegion.right_btm = mouseClick;
                            } else {//left_down
                                Position left_top = new Position(l_t.x, mouseClick.y);
                                Position right_btm = new Position(mouseClick.x, l_t.y);
                                lastClickRegion = new Region(left_top, right_btm);
                            }
                        } else {//right
                            if (l_t.y < mouseClick.y) {//up
                                Position left_top = new Position(mouseClick.x, l_t.y);
                                Position right_btm = new Position(l_t.x, mouseClick.y);
                                lastClickRegion = new Region(left_top, right_btm);
                            } else {
                                lastClickRegion = new Region(mouseClick, l_t);
                            }
                        }
                    }
                }
            }.start();
            mousePressed = false;
        }
        if (mouseButton == LEFT) {
            mousePressed = true;
            mouseDragged = !mouseDragged;
        } else if (mouseButton == RIGHT) {
            if (dragContral)//正在drag，停止
                dragContral = false;
            else {
                initClickRegion();
                dragContral = true;
                loop();
            }
        }
    }

    // swing set,control unit
    private void createSwingWindow() {
        controlWindow = new JWindow(frame);
//        controlWindow.setLocation(1030, 500);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        //button
        // ** origin draw
        JButton oButton = new JButton("Origin");
        ActionListener oButtonActionListen = new ActionListener() {//监听
            @Override
            public void actionPerformed(ActionEvent ae) {
                lastClickRegion.id = totalRegionNum++;
                SharedObject.getInstance().initRO(lastClickRegion);
                lastClickRegion = new Region();
                loop();
            }
        };
        oButton.addActionListener(oButtonActionListen);

        // ** destination draw
        JButton dButton = new JButton("Destination");
        ActionListener dButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lastClickRegion.id = totalRegionNum++;
                SharedObject.getInstance().initRD(lastClickRegion);
                lastClickRegion = new Region();
                loop();
            }
        };
        dButton.addActionListener(dButtonActionListen);

        // ** way point draw
        JButton wButton = new JButton("WayPoint");
        ActionListener wButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lastClickRegion.id = totalRegionNum++;
                SharedObject.getInstance().addRegionW(lastClickRegion);
                lastClickRegion = new Region();
                loop();
            }
        };
        wButton.addActionListener(wButtonActionListen);

        // ** full data draw,查看全集下所有结果
        JButton fullDataButton = new JButton("FullData");
        ActionListener fullDataButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fullData = !fullData;
                trajShowIdListFull.clear();
                for (int i = 0; i < SharedObject.getInstance().getTotalTraj().size(); i++) {
                    trajShowIdListFull.add(i);
                }
                vfgsShowIdListFull = new ArrayList<>(SharedObject.getInstance().getBgSet());
                ranShowIdListFull = SharedObject.getInstance().getRandomTrajId();

                loop();
            }
        };
        fullDataButton.addActionListener(fullDataButtonActionListen);

        // ** select region
        JButton finishSelectButton = new JButton("FinishSelect");
        ActionListener finishSelectButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectRegionTraj(FULL);
                selectRegionTraj(METHOD.VFGS);
                selectRegionTraj(METHOD.RANDOM);
                loop();
            }
        };
        finishSelectButton.addActionListener(finishSelectButtonActionListen);

        // ** screenShot
        JButton screenShotButton = new JButton("ScreenShot");
        ActionListener screenShotButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                screenShot = true;
                loop();
            }
        };
        screenShotButton.addActionListener(screenShotButtonActionListen);


        // clear all regions
        JButton clearRegionButton = new JButton("ClearAllRegions");
        ActionListener clearRegionActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("clear all!");
                SharedObject.getInstance().clearRegion();
                loop();
            }
        };
        clearRegionButton.addActionListener(clearRegionActionListen);

        //** exit
        JButton exitButton = new JButton("Exit");
        ActionListener exitButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };
        exitButton.addActionListener(exitButtonActionListen);

//        //** drag
//        JButton dragButton = new JButton("Drag");
//        ActionListener dragButtonActionListen = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                initClickRegion();
//            }
//        };
//        dragButton.addActionListener(dragButtonActionListen);

        //构建面板
        Container panel2 = controlWindow.getContentPane();
        panel2.add(new JLabel("Operate"), BorderLayout.PAGE_START);
        panel2.add(panel, BorderLayout.CENTER);
        panel.add(oButton);
        panel.add(dButton);
        panel.add(wButton);
        panel.add(finishSelectButton);
        panel.add(fullDataButton);
        panel.add(screenShotButton);
        panel.add(clearRegionButton);
//        panel.add(dragButton);
        panel.add(exitButton);

        //设置窗口属性
        controlWindow.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent me) {
                controlWindow.setLocation(me.getXOnScreen() - 5, me.getYOnScreen() - 5);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        controlWindow.pack();
        controlWindow.setAlwaysOnTop(true);
        controlWindow.setVisible(true);

    }

    private void initClickRegion() {
        Region[] allSharedRegion = SharedObject.getInstance().getAllRegions();

        for (Region r : allSharedRegion) {
            if (r == null)
                continue;
            if (mouseClick.x >= r.left_top.x - circleSize / 2 && mouseClick.x <= r.right_btm.x + circleSize / 2
                    && mouseClick.y >= r.left_top.y - circleSize / 2 && mouseClick.y <= r.right_btm.y + circleSize / 2) {
                regionId = r.id;
                System.out.println(regionId);
                break;
            }
        }
    }

    // **draw
    private void drawMap(UnfoldingMap map, int index) {
        map.draw();

        mapChange = checkLevel != map.getZoomLevel() || !checkCenter.equals(map.getCenter());
        if (mapChange) {
            mapLoadListFlag[index] = false;
            checkLevel = map.getZoomLevel();
            checkCenter = map.getCenter();
        }

        if (!mapLoadListFlag[index]) {
            if (!map.allTilesLoaded()) {
                if (mapImage == null) {
                    mapImage = map.mapDisplay.getInnerPG().get();
                }
                image(mapImage, 0, 0);
            } else {
                mapLoadListFlag[index] = true;
                System.out.println("total load map" + index + " done!");
                if (!zoomPan)
                    stopLoop();
            }
        }
    }

    private void drawRegion(COLOR color, Region r) {
        if (r == null || r.left_top == null || r.right_btm == null)
            return;

        Position l_t = r.left_top;
        Position r_b = r.right_btm;
        stroke(SharedObject.getInstance().getColors()[color.getValue()].getRGB());

        int length = Math.abs(l_t.x - r_b.x);
        int high = Math.abs(l_t.y - r_b.y);

        if (mouseDragged && r.id == regionId) {
            r.left_top = new Position(mouseX, mouseY);
            r.right_btm = new Position(mouseX + length, mouseY + high);
        }
        l_t = r.left_top;
        r_b = r.right_btm;
        noFill();
        strokeWeight(3);
        rect(l_t.x, l_t.y, length, high);
        rect(l_t.x + 510, l_t.y, length, high);
        rect(l_t.x, l_t.y + 410, length, high);
        strokeWeight(circleSize);
        point(l_t.x, l_t.y);
        point(l_t.x + 510, l_t.y);
        point(l_t.x, l_t.y + 410);
    }

    private void drawTraj(int minX, int maxX, int minY, int maxY, int xOff, int yOff, ArrayList<Integer> trajShowIdList) {
        noFill();
        stroke(255, 0, 0);
        strokeWeight(1);
        for (Integer e : trajShowIdList) {
            Trajectory traj = SharedObject.getInstance().getTotalTraj().get(e);
            int i = 0;
            while (i < traj.points.size()) {
                Location loc = traj.points.get(i);
                ScreenPosition pos = mapOverview.getScreenPosition(loc);
                while (i < traj.points.size() && !intoMap(pos, minX, maxX, minY, maxY)) {//找到第一个在内的
                    loc = traj.points.get(i);
                    pos = mapOverview.getScreenPosition(loc);
                    i += 1;
                }
                if (i == traj.points.size())
                    break;
                beginShape();
                while (i < traj.points.size() && intoMap(pos, minX, maxX, minY, maxY)) {//找到最后一个在内的
                    loc = traj.points.get(i);
                    pos = mapOverview.getScreenPosition(loc);
                    vertex(pos.x + xOff, pos.y + yOff);
                    i += 1;
                }
                endShape();
            }
        }
    }

    private boolean intoMap(ScreenPosition pos, int minX, int maxX, int minY, int maxY) {
        return (pos.x > minX && pos.x < maxX && pos.y > minY && pos.y < maxY);
    }

    //** screenshot
    public void screenShot() {
        File dir = new File(shotDir);
        String shotPath = shotDir + "/screenShot" + dir.listFiles().length + ".png";
        saveFrame(shotPath);
//        new Thread() {
//            @Override
//            public void run() {
//                String shotPath = "data/screenshot/screenShot.png";
//                saveFrame(shotPath);
//            }
//        }.start();
        System.out.println("screenshot done");
    }

    // ** region handle
    private void selectRegionTraj(METHOD dataset) {
        long start_time = System.currentTimeMillis();

        ExecutorService threadPool;
        threadPool = new ThreadPoolExecutor(THREADNUM, THREADNUM, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());

        SharedObject instance = SharedObject.getInstance();
        REGION inter;
        if (instance.getRegionA() != null)
            inter = ALLIN;
        else if (instance.getRegionWList().size() != 0) {
            if (instance.getRegionO() != null)//odw
                inter = O_D_W;
            else inter = WAY_POINT;
        } else inter = O_D;

        System.out.println(inter);
        List<Trajectory> total_traj = new ArrayList<>();

        if (dataset == FULL)
            total_traj = SharedObject.getInstance().getTotalTraj();
        else if (dataset == METHOD.VFGS)
            total_traj = SharedObject.getInstance().getVFGSTraj();
        else total_traj = SharedObject.getInstance().getRandomTraj();

        int thread_list_size = total_traj.size() / THREADNUM;
        try {
            for (int i = 0; i < THREADNUM - 1; i++) {
                SelectHandle sht = new SelectHandle(inter, total_traj.subList(i * thread_list_size, (i + 1) * thread_list_size));
                trajIndexAry[i] = threadPool.submit(sht).get().toString().split(",");
            }
            trajIndexAry[9] = threadPool.submit(new SelectHandle(inter, total_traj.subList(9 * thread_list_size, total_traj.size()))).get().toString().split(",");
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        } catch (ExecutionException | InterruptedException e) {
            System.err.println(e);
        }
        System.out.println("time: " + (System.currentTimeMillis() - start_time));
        System.out.println("ALL DONE");
        int totalTrajNum = 0;
        ArrayList<Integer> resList = new ArrayList<>();
        for (String[] indexList : trajIndexAry) {
            for (String id : indexList) {
                if (!id.equals("")) {
                    resList.add((Integer.parseInt(id)));
                    totalTrajNum++;
                }
            }
        }
        if (dataset == FULL) {
            trajShowIdListFull.clear();
            trajShowIdListFull.addAll(resList);
        } else if (dataset == METHOD.VFGS) {
            vfgsShowIdListFull.clear();
            vfgsShowIdListFull.addAll(resList);
        } else {
            ranShowIdListFull.clear();
            ranShowIdListFull.addAll(resList);
        }
        System.out.println("total traj num: " + totalTrajNum);
    }

    private void stopLoop() {
        boolean tmp = mapLoadListFlag[0];
        for (boolean f : mapLoadListFlag) {
            tmp = tmp && f;
            if (!tmp)
                break;
        }
        if (tmp)
            noLoop();
    }

    private void createConvas() {
        JLabel label = new JLabel("Label text");
        label.setOpaque(true);
        label.setBackground(new Color(255, 0, 0, 0));

        JFrame frame = new JFrame("test");
        frame.setSize(1000, 800);
        frame.setLocation(0, 0);
        frame.setUndecorated(true);

        AWTUtilities.setWindowOpaque(frame, false);

        GLData data = new GLData();
        data.samples = 4;
        data.swapInterval = 0;
        final AWTGLCanvas canvas;
        frame.add(canvas = new AWTGLCanvas(data) {
            private static final long serialVersionUID = 1L;

            public void initGL() {
                System.out.println("OpenGL version: " + effective.majorVersion + "." + effective.minorVersion + " (Profile: " + effective.profile + ")");
                createCapabilities();
            }

            public void paintGL() {
                int w = getWidth();
                int h = getHeight();
                float aspect = (float) w / h;
                double now = System.currentTimeMillis() * 0.001;
                float width = (float) Math.abs(Math.sin(now * 0.3));
                glClear(GL_COLOR_BUFFER_BIT);
                glViewport(0, 0, w, h);
                glBegin(GL_QUADS);
                glColor3f(0.4f, 0.6f, 0.8f);
                glVertex2f(-0.75f * width / aspect, 0.0f);
                glVertex2f(0, -0.75f);
                glVertex2f(+0.75f * width / aspect, 0);
                glVertex2f(0, +0.75f);
                glEnd();
                swapBuffers();
            }
        }, BorderLayout.CENTER);
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);

        Runnable renderLoop = new Runnable() {
            public void run() {
                if (!canvas.isValid())
                    return;
                canvas.render();
                SwingUtilities.invokeLater(this);
            }
        };
        SwingUtilities.invokeLater(renderLoop);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            totalTrajFilePath = args[0];
            vfgsFilePath = args[1];
            regionDirPath = args[2];
            shotDir = args[3];
        }
//        preProcess();
        String title = "app.ProcessingDemo3_2";

        PApplet.main(new String[]{title});
    }
}
