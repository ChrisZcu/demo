import com.sun.awt.AWTUtilities;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MapBox;
import de.fhpotsdam.unfolding.utils.MapUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;


public class ProcessingLwjglTest1
        extends PApplet {
    private static JWindow psThatSettingWindow;
    private boolean psFrameVisibility = false;
    private boolean psThisAddButtonWaiting = false;
    private boolean psThisRemoveButtonWaiting = false;
    private boolean psThisClearButtonWaiting = false;
    private boolean psThisStrokeChecker = true;
    private boolean psThisFillChecker = true;
    private String psMessageDialogCaster = "nc";
    private Object[] pbAddingComboModel = {"Single", "Double", "Tripple"};
    //-
//- ** local
    private ArrayList<EcElement> pbTheElementList;
    private ArrayList<PVector> pbTheCubeList;
    private String pbImagePath = "nc";
    private String pbImageFileName = "nc";
    private int pbAmountPerAdding = 1;
    private int pbAddingAmount = 1;

    // map
    private UnfoldingMap map;
    private static final Location PORTO_CENTER = new Location(41.14, -8.639);//维度经度
    private static final Location PRESENT = PORTO_CENTER;

    private PImage mapImage = null;
    private boolean totalLoad = false;
    private int checkLevel = -1;
    private Location checkCenter = new Location(-1, -1);


//    @Override
//    public void settings() {
//        size(1000, 800);
//    }

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

        map.zoomAndPanTo(12, PRESENT);
        map.setBackgroundColor(255);

        MapUtils.createDefaultEventDispatcher(this, map);


        //--
        //-- ** Swing Theme
        try {
            /* alternate available:
             *   "javax.swing.plaf.metal.MetalLookAndFeel"
             *   "javax.swing.plaf.nimbus.NimbusLookAndFeel"
             *   "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
             *   "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
             *   "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"
             * or use the next line to check what is available on your system:
             *   for(UIManager.LookAndFeelInfo it:UIManager.getInstalledLookAndFeels()){println(it.getClassName());}
             */
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();//设置外观风格，和os保持一致
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            println("--well yeah something went wrong but i dont think we needa know that");
        }
        //--
        //-- ** Local UI setting
        pbTheCubeList = new ArrayList<PVector>();
        pbTheElementList = new ArrayList<EcElement>();//自定义ui类，储存ui元素
        //--
        EcButton lpBrowseButton = new EcButton(50, 20, "browse", 0);
        pbTheElementList.add(lpBrowseButton);
        EcButton lpSaveButton = new EcButton(40, 20, "save", 1);
        pbTheElementList.add(lpSaveButton);
        EcButton lpOperateButton = new EcButton(50, 20, "operate", 2);
        pbTheElementList.add(lpOperateButton);
        EcButton lpQuitButton = new EcButton(50, 20, "quit", 3);
        pbTheElementList.add(lpQuitButton);
        EcTextBox lpPathBox = new EcTextBox(200, 20, "path:", 4);
        pbTheElementList.add(lpPathBox);
        EcButton lpFileNameButton = new EcButton(60, 20, "FileName", 5);
        pbTheElementList.add(lpFileNameButton);
        EcButton lpAddingButton = new EcButton(60, 20, "Adding", 6);
        pbTheElementList.add(lpAddingButton);

        //--设置位置
        lpPathBox.ccTargetLayout(null, 10, 10);
        lpBrowseButton.ccTargetLayout(lpPathBox, 3, 0);
        lpSaveButton.ccTargetLayout(lpBrowseButton, 3, 0);
        lpQuitButton.ccTargetLayout(null, 5, 200);
        lpOperateButton.ccTargetLayout(lpQuitButton, 5, 0);
        lpFileNameButton.ccTargetLayout(lpOperateButton, 5, 0);
        lpAddingButton.ccTargetLayout(lpFileNameButton, 55, 0);
        //--
        //-- ** Swing UI setting
        fsCreateSwingWindow();
        //--
    }//+++

    @Override
    public void draw() {
        drawMap();
        //--
        //-- ** receving request from swing window
        if (psThisAddButtonWaiting) {
            for (int i = 0; i < pbAddingAmount; i++) {
                pbTheCubeList.add(new PVector(random(width), random(height)));
            }
            psThisAddButtonWaiting = false;
        }
        if (psThisRemoveButtonWaiting) {
            if (!pbTheCubeList.isEmpty()) {
                pbTheCubeList.remove(0);
            }
            psThisRemoveButtonWaiting = false;
        }
        if (psThisClearButtonWaiting) {
            pbTheCubeList.clear();
            psThisClearButtonWaiting = false;
        }
        //--
        //-- ** receving setting from swing window
        if (psThisStrokeChecker) {
            stroke(0xCC);
        } else {
            noStroke();
        }
        if (psThisFillChecker) {
            fill(0x99);
        } else {
            noFill();
        }
        //--
        //-- ** draw local elements
        for (PVector it : pbTheCubeList) {
            rect(it.x, it.y, ceil(random(4, 8)), ceil(random(4, 8)));
        }
        noStroke();
        for (EcElement it : pbTheElementList) {
            it.ccUpdate();
        }
        //--
    }//+++


    @Override
    public void keyPressed() {
        switch (key) {
            //--
            case 'q':
                fsPover();
            case 'w':
                System.out.println(key);
                creatAwtLwjglWindow();
                break;
            case 'e':
                System.out.println(key);
                windowLayer();
                break;
            default:
                break;
            //--
        }
    }//+++

    @Override
    public void mousePressed() {
        int lpID = 3001;
        for (EcElement it : pbTheElementList) {
            if (it instanceof EcButton) {
                lpID = ((EcButton) it).ccTellClickedID();
                if (lpID < 3000) {
                    break;
                }
            }
        }
        //--
        switch (lpID) {
            //--
            case 0:
                thread("fsGetPathByFileChooser");
                break;
            //--
            case 1: {
                File lpFile = new File(pbImagePath);
                if (lpFile.isDirectory()) {
                    saveFrame(pbImagePath + "\\" + pbImageFileName + "-######.png");
                } else {
                    psMessageDialogCaster = "File path illegal.";
                    thread("fsMessageDialog");
                }
            }
            break;
            case 2:
                psFrameVisibility = !psFrameVisibility;
                psThatSettingWindow.setVisible(psFrameVisibility);
                psThatSettingWindow.setEnabled(psFrameVisibility);
                psThatSettingWindow.setLocation(frame.getLocation().x + width + 10, frame.getLocation().y);
                break;
            case 5:
                thread("fsSetFileNameByInputDialog");
                break;
            case 6:
                thread("fsComboDialog");
                break;
            case 3:
                fsPover();
                break;
            default:
                break;
        }
        //--
    }//+++

//< <<< <<< <<< <<< <<< Overrided


    //draw map
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
            }
            map.draw();
        }
    }

//* *** *** *** *** ***
//*
//* Operate
//*
//* *** *** *** *** ***
//- --- --- ---


    private void fsCreateSwingWindow() {
        //-- ** presetting
        psThatSettingWindow = new JWindow(frame);


        JPanel lpPanel = new JPanel();
        lpPanel.setLayout(new GridLayout(0, 1));//grid
        //-- **AddButonn
        JButton lpThisAddButton = new JButton("add");
        ActionListener lpThisAddButtonAction = new ActionListener() {//监听
            @Override
            public void actionPerformed(ActionEvent ae) {
                //-- ** ** action start from here::
                psThisAddButtonWaiting = true;
            }
        };
        lpThisAddButton.addActionListener(lpThisAddButtonAction);
        //--
        //-- **RemoveButonn
        JButton lpThisRemoveButton = new JButton("remove");
        ActionListener lpThisRemoveButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //-- ** ** action start from here::
                psThisRemoveButtonWaiting = true;
            }
        };
        lpThisRemoveButton.addActionListener(lpThisRemoveButtonAction);
        //--
        //-- **ClearButonn
        JButton lpThisClearButton = new JButton("clear");
        ActionListener lpThisClearButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //-- ** ** action start from here::
                psThisClearButtonWaiting = true;
            }
        };
        lpThisClearButton.addActionListener(lpThisClearButtonAction);
        //--
        //-- **StrokeChecker
        JCheckBox lpThisStrokeChecker = new JCheckBox("stroke");
        ActionListener lpThisStrokeCheckerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //-- ** ** action start from here::
                Object lpSouce = ae.getSource();
                if (lpSouce instanceof JCheckBox) {
                    psThisStrokeChecker = ((JCheckBox) lpSouce).isSelected();
                }
            }
        };
        lpThisStrokeChecker.addActionListener(lpThisStrokeCheckerActionListener);
        lpThisStrokeChecker.setSelected(true);
        //--
        //-- **FillChecker
        JCheckBox lpThisFillChecker = new JCheckBox("fill");
        ActionListener lpThisFillCheckerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //-- ** ** action start from here::
                Object lpSouce = ae.getSource();
                if (lpSouce instanceof JCheckBox) {
                    psThisFillChecker = ((JCheckBox) lpSouce).isSelected();
                }
            }
        };
        lpThisFillChecker.addActionListener(lpThisFillCheckerActionListener);
        lpThisFillChecker.setSelected(true);
        //--
        //-- ** layuout setting
        Container lpPane = psThatSettingWindow.getContentPane();

        //构建面板
        lpPane.add(new JLabel("[+] operate"), BorderLayout.PAGE_START);
        lpPane.add(lpPanel, BorderLayout.CENTER);
        lpPanel.add(lpThisAddButton);
        lpPanel.add(lpThisRemoveButton);
        lpPanel.add(lpThisClearButton);
        lpPanel.add(lpThisStrokeChecker);
        lpPanel.add(lpThisFillChecker);
        //--
        //-- ** window setting
        psThatSettingWindow.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent me) {
                psThatSettingWindow.setLocation(me.getXOnScreen() - 5, me.getYOnScreen() - 5);
            }

            //--
            @Override
            public void mouseMoved(java.awt.event.MouseEvent me) {
                ;
            }
        });
        psThatSettingWindow.pack();
//        psThatSettingWindow.setSize(1000,800);
        psThatSettingWindow.setVisible(true);
        //--
    }//+++

    private JFrame trajFrame = new JFrame("traj");

    private void windowLayer() {
        trajFrame.setSize(1000, 800);
        trajFrame.setResizable(false);
        trajFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        trajFrame.setLayout(new BorderLayout());

//        AWTGLCanvas canvas;

        initCanvas();
        trajFrame.add(canvas, BorderLayout.CENTER);

        trajFrame.setVisible(false);

        trajFrame.setUndecorated(true);
        trajFrame.setOpacity(0.7f);

        trajFrame.setVisible(true);
        trajFrame.setAlwaysOnTop(true);
        trajFrame.setLocation(3, 26);
//        trajFrame.transferFocus();

        new Thread() {
            @Override
            public void run() {
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
        }.start();
    }

    private AWTGLCanvas canvas;

    private void creatAwtLwjglWindow() {
        System.out.println(frame.getSize());
        System.out.println(frame.getX());
        frame.setLayout(new BorderLayout());
//        frame.setPreferredSize(new Dimension(1000, 600));
        GLData data = new GLData();
        data.samples = 4;
        data.swapInterval = 0;
        initCanvas();
        frame.add(canvas, BorderLayout.CENTER);
        frame.setBackground(Color.BLUE);
//        frame.setSize(500, 400);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);

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

    private void initCanvas() {
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
                int w = getWidth();
                int h = getHeight();
                glClear(GL_COLOR_BUFFER_BIT);
                glViewport(0, 0, w, h);
                glBegin(GL_LINE_STRIP);
                glColor3f(1.0f, 0.99f, 0.0f);//color
                glVertex2f(-0.75f, 0.0f);
                glVertex2f(0, -0.75f);
                glVertex2f(+0.75f, 0);
                glVertex2f(0, +0.75f);
                glEnd();
                swapBuffers();
            }
        };


    }

    private void fsPover() {
        exit();
    }

//< <<< <<< <<< <<< <<< operate

//* *** *** *** *** ***
//*
//* Class
//*
//* *** *** *** *** ***

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

    class EcTextBox extends EcElement {
        String cmText;
        String cmBoxText;
        int cmMax;
        boolean cmIsCutted;

        EcTextBox(int pxW, int pxH, String pxName, int pxID) {
            super();
            cmW = pxW;
            cmH = pxH;
            cmName = pxName;
            cmID = pxID;
            cmText = "n/c";
            cmBoxText = cmText;
            cmMax = cmW / 8;
            cmIsCutted = false;
        }

        //--
        @Override
        void ccUpdate() {
            //--
            stroke(0x11, 0xEE, 0x11);
            fill(0x33, 0xCC);
            rect(cmX, cmY, cmW, cmH);
            noStroke();
            fill(0x11, 0xEE, 0x11);
            text(cmBoxText, cmX + 2, cmY + 2);
            //--
            if (ccIsMouseOver() && cmIsCutted) {
                fill(0xEE);
                text(cmText, mouseX - 64, mouseY + 16);
            }
        }

        //--
        void ccSetText(String pxText) {
            cmText = pxText;
            int lpLength = cmText.length();
            if (lpLength > cmMax) {
                cmBoxText = "~" + cmText.substring(lpLength - cmMax, lpLength);
                cmIsCutted = true;
            } else {
                cmBoxText = cmText;
                cmIsCutted = false;
            }
        }
        //--
    }//+++

    public static void main(String[] args) {
        String title = "ProcessingLwjglTest1";
        PApplet.main(new String[]{title});
    }
}