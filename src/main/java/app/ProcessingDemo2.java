package app;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import model.Position;
import model.Region;
import model.Trajectory;
import processing.core.PApplet;
import processing.core.PImage;
import select.SelectHandle;
import util.COLOR;
import util.PreProcess;
import util.REGION;
import util.SharedObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static util.REGION.*;
import static util.REGION.O_D;

public class ProcessingDemo2 extends PApplet {

    private static JWindow controlWindow; // control component
    private boolean drawOrigion = false;
    private boolean drawDestination = false;
    private boolean drawWayPoint = false;
    private boolean fullData = false;
    private boolean selectDone = false;
    private boolean VFGS = false;
    private boolean mapChange = false;
    private boolean mouseDragged = false;
    private boolean mousePressed = false;
    private boolean screenShot = false;

    private int THREADNUM = 10;
    private String[][] trajIndexAry;
    private ArrayList<Integer> trajShowIdList = new ArrayList<>();
    //map
    private UnfoldingMap map;
    private static final Location PORTO_CENTER = new Location(41.14, -8.639);//维度经度
    private static final Location PRESENT = PORTO_CENTER;

    private PImage mapImage = null;
    private boolean totalLoad = false;
    private int checkLevel = -1;
    private Location checkCenter = new Location(-1, -1);

    private Region lastClickRegion = new Region();
    private Region dragRegion = new Region();
    private int regionId = -1;
    private int totalRegionNum = 0;
    private Position mouseClick;

    private static void preProcess() {
        // init shared object total trajectory list
        String totalTrajFilePath = "E:\\zcz\\dbgroup\\data\\Portugal\\GPS\\Porto20w.txt";
        List<Trajectory> trajTotal = new ArrayList<>();
        PreProcess.totalListInit(trajTotal, totalTrajFilePath);

        SharedObject.getInstance().initTrajList(trajTotal);
        System.out.println("pre-process done");
        System.out.println("total trajectory number: " + trajTotal.size());
    }

    // map set
    @Override
    public void setup() {
        frameRate(16); // fps

        surface.setTitle("Processing with AWT");
        surface.setSize(1000, 800);
        surface.setLocation(0, 0);

        trajIndexAry = new String[THREADNUM][];


        String mapStyle = "https://api.mapbox.com/styles/v1/pacemaker-yc/ck4gqnid305z61cp1dtvmqh5y/tiles/512/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoicGFjZW1ha2VyLXljIiwiYSI6ImNrNGdxazl1aTBsNDAzZW41MDhldmQyancifQ.WPAckWszPCEHWlyNmJfY0A";
        map = new UnfoldingMap(this, "CHI Demo", new MapBox.CustomMapBoxProvider(mapStyle));
        map.setZoomRange(1, 20);
        map.zoomAndPanTo(11, PRESENT);
        map.setBackgroundColor(255);

        SharedObject.getInstance().initMap(map);
        MapUtils.createDefaultEventDispatcher(this, map);

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
        noFill();
        drawMap();
        if (mousePressed) {
            System.out.println(mouseClick);
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
        if (drawOrigion) {
            lastClickRegion.id = totalRegionNum++;
            SharedObject.getInstance().initRO(lastClickRegion);
            lastClickRegion = new Region();
            drawOrigion = false;
        } else if (drawDestination) {
            lastClickRegion.id = totalRegionNum++;
            SharedObject.getInstance().initRD(lastClickRegion);
            lastClickRegion = new Region();
            drawDestination = false;
        } else if (drawWayPoint) {
            lastClickRegion.id = totalRegionNum++;
            SharedObject.getInstance().addRegionW(lastClickRegion);
            lastClickRegion = new Region();
            drawWayPoint = false;
        } else if (selectDone) {
            selectRegionTraj();
            selectDone = false;
        }
        drawTraj();

        drawRegion(COLOR.GREEN, SharedObject.getInstance().getRegionO());
        drawRegion(COLOR.BLUE, SharedObject.getInstance().getRegionD());
        for (Region r_w : SharedObject.getInstance().getRegionWList()) {
            drawRegion(COLOR.PINK, r_w);
        }
    }

    @Override
    public void mousePressed() {
        mouseClick = new Position(mouseX, mouseY);
        if (mouseButton == LEFT) {
            mousePressed = true;
            mouseDragged = true;
        } else if (mouseButton == RIGHT) {
            initClickRegion();
            mouseDragged = false;
            System.out.println(22);
        }
    }


    private void initClickRegion() {
        Region[] allSharedRegion = SharedObject.getInstance().getAllRegions();

        for (Region r : allSharedRegion) {
            if (r == null)
                continue;
            if (mouseClick.x >= r.left_top.x - 12 && mouseClick.x <= r.right_btm.x + 12
                    && mouseClick.y >= r.left_top.y - 12 && mouseClick.y <= r.right_btm.y + 12) {
                regionId = r.id;
                break;
            }
        }
    }

    // **draw
    private void drawMap() {
        mapChange = checkLevel != map.getZoomLevel() || !checkCenter.equals(map.getCenter());
        if (mapChange) {
            totalLoad = false;
            checkLevel = map.getZoomLevel();
            checkCenter = map.getCenter();
        }

        if (!totalLoad) {
            if (!map.allTilesLoaded()) {
                if (mapImage == null) {
                    mapImage = map.mapDisplay.getInnerPG().get();
                }
                image(mapImage, 500, 40);
            } else {
                totalLoad = true;
                System.out.println("total load map done!");
            }
            map.draw();
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
//        rect(l_t.x, l_t.y, length, high);

        strokeWeight(25);
        point(l_t.x, l_t.y);

        strokeWeight(3);
        beginShape();
        vertex(l_t.x, l_t.y);
        vertex(r_b.x, l_t.y);
        vertex(r_b.x, r_b.y);
        vertex(l_t.x, r_b.y);
        vertex(l_t.x, l_t.y);
        endShape();
    }

    private void drawTraj() {
        stroke(255, 0, 0);
        strokeWeight(1);
        for (Integer e : trajShowIdList) {
            Trajectory traj = SharedObject.getInstance().getTotalTraj().get(e);
            beginShape();
            for (Location loc : traj.points) {
                ScreenPosition pos = map.getScreenPosition(loc);
                vertex(pos.x, pos.y);
            }
            endShape();
        }
    }

    //** screenshot
    public void screenShot() {
        new Thread() {
            @Override
            public void run() {
                String shotPath = "data/screenshot/screenShot.png";
                saveFrame(shotPath);
            }
        }.start();
    }

    // ** region handle
    private void selectRegionTraj() {
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
        List<Trajectory> total_traj = SharedObject.getInstance().getTotalTraj();

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
        trajShowIdList.clear();
        for (String[] indexList : trajIndexAry) {
            for (String id : indexList) {
                if (!id.equals("")) {
                    trajShowIdList.add((Integer.parseInt(id)));
                    totalTrajNum++;
                }
            }
        }
        System.out.println("total traj num: " + totalTrajNum);
    }

    // swing set,control unit
    private void createSwingWindow() {
        controlWindow = new JWindow(frame);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        //button
        // ** origin draw
        JButton oButton = new JButton("Origin");
        ActionListener oButtonActionListen = new ActionListener() {//监听
            @Override
            public void actionPerformed(ActionEvent ae) {
                //-- ** ** action start from here::
                drawOrigion = true;
            }
        };
        oButton.addActionListener(oButtonActionListen);

        // ** destination draw
        JButton dButton = new JButton("Destination");
        ActionListener dButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawDestination = true;
            }
        };
        dButton.addActionListener(dButtonActionListen);

        // ** way point draw
        JButton wButton = new JButton("WayPoint");
        ActionListener wButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawWayPoint = true;
            }
        };
        wButton.addActionListener(wButtonActionListen);

        // ** full data draw
        JButton fullDataButton = new JButton("FullData");
        ActionListener fullDataButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fullData = true;
                trajShowIdList.clear();
                for (int i = 0; i < SharedObject.getInstance().getTotalTraj().size(); i++) {
                    trajShowIdList.add(i);
                }
            }
        };
        fullDataButton.addActionListener(fullDataButtonActionListen);

        // ** select region
        JButton finishSelectButton = new JButton("FinishSelect");
        ActionListener finishSelectButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDone = true;
            }
        };
        finishSelectButton.addActionListener(finishSelectButtonActionListen);

        // ** screenShot
        JButton screenShotButton = new JButton("ScreenShot");
        ActionListener screenShotButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                screenShot = true;
                screenShot();
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
                totalLoad = false;
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

        //** drag
        JButton dragButton = new JButton("Drag");
        ActionListener dragButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initClickRegion();
            }
        };
        dragButton.addActionListener(dragButtonActionListen);

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
        panel.add(dragButton);
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


    public static void main(String[] args) {
        preProcess();
        String title = "app.ProcessingDemo2";

        PApplet.main(new String[]{title});
    }
}
