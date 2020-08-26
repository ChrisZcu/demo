package app;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import draw.CanvasRun;
import javafx.geometry.Pos;
import model.Position;
import model.Trajectory;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import processing.core.PApplet;
import processing.core.PImage;
import util.PreProcess;
import util.SharedObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;


public class demo1 extends PApplet {
    private static JWindow controlWindow; // control component
    private static JFrame trajFrame;
    private boolean drawOrigion = false;
    private boolean drawDestination = false;
    private boolean drawWayPoint = false;
    private boolean drawDone = false;


    //map
    private UnfoldingMap map;
    private static final Location PORTO_CENTER = new Location(41.14, -8.639);//维度经度
    private static final Location PRESENT = PORTO_CENTER;

    private PImage mapImage = null;
    private boolean totalLoad = false;
    private int checkLevel = -1;
    private Location checkCenter = new Location(-1, -1);

    //lwjgl
    private AWTGLCanvas canvas;

    // pre-process for data
    private static void preProcess() {
        // init shared object total trajectory list
        String totalTrajFilePath = "data/portoScore.txt";
        List<Trajectory> trajTotal = new ArrayList<>();
        PreProcess.totalListInit(trajTotal, totalTrajFilePath);

        SharedObject.getInstance().initTrajList(trajTotal);
        System.out.println("pre-process done");
        System.out.println("total trajectory number: " + trajTotal.size());
    }

    // map set
    @Override
    public void setup() {
        noStroke();
        frameRate(16); // fps
        textAlign(LEFT, TOP);
        ellipseMode(CENTER);
        surface.setTitle("Test Swing with Processing!!");
        surface.setSize(1000, 800);
        surface.setLocation(0, 0);

        map = new UnfoldingMap(this);

        String mapStyle = "https://api.mapbox.com/styles/v1/pacemaker-yc/ck4gqnid305z61cp1dtvmqh5y/tiles/512/{z}/{x}/{y}@2x?access_token=pk.eyJ1IjoicGFjZW1ha2VyLXljIiwiYSI6ImNrNGdxazl1aTBsNDAzZW41MDhldmQyancifQ.WPAckWszPCEHWlyNmJfY0A";

        map = new UnfoldingMap(this, "CHI Demo", new MapBox.CustomMapBoxProvider(mapStyle));
        map.setZoomRange(1, 20);

        map.zoomAndPanTo(11, PRESENT);
        map.setBackgroundColor(255);

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
        drawMap();
        //TODO add region selection
        if (drawOrigion) {
            drawOrigion = false;
        } else if (drawDestination) {
            drawDestination = false;
        } else if (drawWayPoint) {
            drawWayPoint = false;
        } else if (drawDone) {
            drawDone = false;
            trajLayer();
        }
    }

    private void drawMap() {
        if (checkLevel != map.getZoomLevel() || !checkCenter.equals(map.getCenter())) {
            totalLoad = false;
            checkLevel = map.getZoomLevel();
            checkCenter = map.getCenter();
//            loop();

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

    private ArrayList<ArrayList<ScreenPosition>> GPS2ScreenLoc(ArrayList<Integer> trajIdList) {
        List<Trajectory> trajTotal = SharedObject.getInstance().getTotalTraj();
        ArrayList<ArrayList<ScreenPosition>> res = new ArrayList<>();

        for (Integer e : trajIdList) {
            Trajectory traj = trajTotal.get(e);
            ArrayList<ScreenPosition> tmp = new ArrayList<>();
            for (Location loc : traj.points) {
                ScreenPosition pos = map.getScreenPosition(loc);
                tmp.add(pos);
            }
            res.add(tmp);
        }
        System.out.println("change done");
        return res;
    }

    // swing set
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

        // ** finish draw
        JButton finishButton = new JButton("DrawDone");
        ActionListener finishButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawDone = true;
            }
        };
        finishButton.addActionListener(finishButtonActionListen);

        //** exit
        JButton exitButton = new JButton("Exit");
        ActionListener exitButtonActionListen = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        };
        exitButton.addActionListener(exitButtonActionListen);


        //构建面板
        Container panel2 = controlWindow.getContentPane();
        panel2.add(new JLabel("Operate"), BorderLayout.PAGE_START);
        panel2.add(panel, BorderLayout.CENTER);
        panel.add(oButton);
        panel.add(dButton);
        panel.add(wButton);
        panel.add(finishButton);
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

    Thread td;
    boolean stopRender = false;

    private void trajLayer() {
        trajFrame = new JFrame("traj");
        trajFrame.setSize(1000, 800);
        trajFrame.setResizable(false);
        trajFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initCanvas();
        trajFrame.add(canvas);
        trajFrame.setVisible(false);

        trajFrame.setUndecorated(true);
        trajFrame.setOpacity(0.7f);

        trajFrame.setVisible(true);
        trajFrame.setAlwaysOnTop(true);
        trajFrame.setLocation(3, 26);

        controlWindow.setAlwaysOnTop(true);

        // 渲染

//        new Thread() {
//            @Override
//            public void run() {
//                Runnable renderLoop = new Runnable() {
//                    public void run() {
//                        if (!canvas.isValid())
//                            return;
//
//                        canvas.render();
//                        SwingUtilities.invokeLater(this);
//                    }
//                };
//                SwingUtilities.invokeLater(renderLoop);
//            }
//        }.start();

        td = new CanvasRun(canvas);
        td.start();
        SwingUtilities.invokeLater(td);
    }

    private void stopRender(boolean stopRender) {
        System.out.println(stopRender);
        if (stopRender) {
            System.out.println("!!!!!!!!!!");
            td.interrupt();
        }
    }

    private void initCanvas() {
        /*
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data/vfgs_0.01_50.txt"));
            String line;
            int num = 0;

            while ((line = reader.readLine()) != null) {
                trajShowIdList.add(Integer.parseInt(line));
//                num++;
//                if (num > 10)
//                    break;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
*/
/*
        ArrayList<Integer> trajShowIdList = new ArrayList<>();
        for (int i = 0; i < 200000; i++) {
            trajShowIdList.add(i);
        }
        final ArrayList<ArrayList<ScreenPosition>> posList = GPS2ScreenLoc(trajShowIdList);
        System.out.println(posList.size());
*/
        /*
        for (int i = 0; i < posList.size(); i++) {
            for (ScreenPosition pos : posList.get(i)) {
                float x = getNormalizeX(pos.x, 1000);
                float y = getNormalizeY(pos.y, 800);

                if (x <= 1 && x >= -1 && y <= 1 && y >= -1) {
                    System.out.println(i);
                    break;
                }
            }
        }
        */

        /*

        //debug for coordinate exchange
        for (ScreenPosition pos : posList.get(0)) {
            float x = getNormalizeX(posList.get(0).get(0).x, 1000);
            float y = getNormalizeY(posList.get(0).get(0).y, 800);
            if (x > 1 || y > 1) {
                System.out.println("----------coordinate------------");
                System.out.println(posList.get(0).get(0).x + ", " + posList.get(0).get(0).y);
                System.out.println(x + ", " + y);
                System.out.println("--------------------------------");
            }
        }
*/
        GLData data = new GLData();
        data.samples = 4;
        data.swapInterval = 0;
        canvas = new AWTGLCanvas(data) {
            @Override
            public void initGL() {
                createCapabilities();
                glClearColor(0.3f, 0.4f, 0.5f, 1);
            }

            @Override
            public void paintGL() {
                stopRender(stopRender);
                System.out.println(1);
                /*

                int w = 1000;
                int h = 800;
                glClear(GL_COLOR_BUFFER_BIT);

                glViewport(0, 0, w, h);

                for (ArrayList<ScreenPosition> posListTmp : posList) {
                    glBegin(GL_LINE_STRIP);
                    glColor3f(1.0f, 0.99f, 0.0f);//color
                    for (ScreenPosition pos : posListTmp) {
                        float x = getNormalizeX(pos.x, w);
                        float y = getNormalizeY(pos.y, h);
                        if (x <= 1 && x >= -1 && y <= 1 && y >= -1) {
                            glVertex2f(x, y);
                        }
                    }
                    glEnd();
                    swapBuffers();
                }
                */
                System.out.println(2);
                stopRender = true;
            }
        };

    }

    private float getNormalizeX(float x, int width) {
        return (float) (-1.0 + 2.0 * (double) x / width);
    }

    private float getNormalizeY(float y, int height) {
        return (float) (1.0 - 2.0 * (double) y / height);
    }

    class EcElement {
        int cmX, cmY, cmW, cmH;
        int cmID;
        String cmName;

        EcElement() {
            cmX = cmY = cmW = cmH = 9;
            cmID = 3001;
            cmName = "n/c";
        }

        //--
        void ccUpdate() {
            fill(0x99);
            rect(cmX, cmY, cmW, cmH);
        }

        //--
        boolean ccIsMouseOver() {
            return (mouseX > cmX) && (mouseX < (cmX + cmW)) &&
                    (mouseY > cmY) && (mouseY < (cmY + cmH));
        }

        //--
        void ccTargetLayout(EcElement pxFollow, int pxOffsetX, int pxOffsetY) {
            if (pxFollow == null) {
                cmX = pxOffsetX;
                cmY = pxOffsetY;
                return;
            }
            cmX = pxFollow.cmX + pxOffsetX + (pxOffsetY == 0 ? pxFollow.cmW : 0);
            cmY = pxFollow.cmY + pxOffsetY + (pxOffsetX == 0 ? pxFollow.cmH : 0);
        }
        //--
    }//+++

    class EcButton extends EcElement {
        EcButton(int pxW, int pxH, String pxName, int pxID) {
            super();
            cmW = pxW;
            cmH = pxH;
            cmName = pxName;
            cmID = pxID;
        }

        //--
        @Override
        void ccUpdate() {
            fill(ccIsMouseOver() ? color(0xEE, 0xEE, 0x11, 0xCC) : color(0xEE, 0xCC));
            rect(cmX, cmY, cmW, cmH);
            fill(0x11);
            textAlign(CENTER, CENTER);
            text(cmName, cmX + cmW / 2, cmY + cmH / 2);
            textAlign(LEFT, TOP);
        }

        //--
        int ccTellClickedID() {
            return ccIsMouseOver() ? cmID : 9999;
        }
    }//+++

    public static void main(String[] args) {
//        preProcess();

        String title = "app.demo1";
        PApplet.main(new String[]{title});
    }
}
