import processing.core.PApplet;
import processing.core.PVector;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.util.ArrayList;


public class ProcessingTest extends PApplet {
    static JWindow psThatSettingWindow;
    boolean psFrameVisibility = false;
    boolean psThisAddButtonWaiting = false;
    boolean psThisRemoveButtonWaiting = false;
    boolean psThisClearButtonWaiting = false;
    boolean psThisStrokeChecker = true;
    boolean psThisFillChecker = true;
    String psMessageDialogCaster = "nc";
    Object[] pbAddingComboModel = {"Single", "Double", "Tripple"};
    //-
//- ** local
    ArrayList<EcElement> pbTheElementList;
    ArrayList<PVector> pbTheCubeList;
    String pbImagePath = "nc";
    String pbImageFileName = "nc";
    int pbAmountPerAdding = 1;
    int pbAddingAmount = 1;

    //-
//-

    @Override
    public void settings() {
        size(320, 240);

    }

    @Override
    public void setup() {
        noStroke();
        frameRate(16);
        textAlign(LEFT, TOP);
        ellipseMode(CENTER);
        frame.setTitle("Test Swing with Processing!!");

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
            System.err.println(e);
        }
        //--
        //-- ** Local UI setting
        pbTheCubeList = new ArrayList<PVector>();
        pbTheElementList = new ArrayList<EcElement>();
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
        //--
        lpPathBox.ccTargetLayout(null, 10, 10);
        lpBrowseButton.ccTargetLayout(lpPathBox, 3, 0);
        lpSaveButton.ccTargetLayout(lpBrowseButton, 3, 0);
        lpQuitButton.ccTargetLayout(null, 10, 30);
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
        background(0);
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

//* *** *** *** *** ***
//*
//* Operate
//*
//* *** *** *** *** ***
//- --- --- ---

    public void fsComboDialog() {
        String lpSelected = (String) JOptionPane.showInputDialog(frame, "how much cube youd like to add per click", "Adding mode setting Dialog",
                JOptionPane.PLAIN_MESSAGE, null, pbAddingComboModel, pbAddingComboModel[pbAddingAmount - 1]);
        if (lpSelected == null) {
            return;
        }
        if (lpSelected.equals(pbAddingComboModel[0])) {
            pbAddingAmount = 1;
        }//...ALL IS NO SAFE AND CLEAN BUT ANYWAY!!
        if (lpSelected.equals(pbAddingComboModel[1])) {
            pbAddingAmount = 2;
        }
        if (lpSelected.equals(pbAddingComboModel[2])) {
            pbAddingAmount = 3;
        }
    }//+++

    public void fsSetFileNameByInputDialog() {
        String lpInputed = (String) JOptionPane.showInputDialog(frame, "Replace current file name:", "File Name Input Dialog",
                1, null, null, pbImageFileName);
        if (lpInputed != null) {
            if (!lpInputed.isEmpty()) {
                pbImageFileName = lpInputed;
            }
        }
    }//+++

    public void fsMessageDialog() {
        JOptionPane.showMessageDialog(frame, psMessageDialogCaster);
    }

    public void fsGetPathByFileChooser() {
        final JFileChooser lpChooser = new JFileChooser();
        lpChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int lpChooserFlag = lpChooser.showOpenDialog(frame);
        if (lpChooserFlag == JFileChooser.APPROVE_OPTION) {
            File lpFile = lpChooser.getSelectedFile();
            EcElement lpElement = pbTheElementList.get(4);//...THIS IS NO SAFE AND CLEAN BUT ANYWAY!!
            pbImagePath = lpFile.toString();
            ((EcTextBox) lpElement).ccSetText(pbImagePath);
        } else {
            println("--again, this should be shown nowhere cuz we dont need a cancel noice");
        }
        //--
    }//+++

    private void fsCreateSwingWindow() {
        //-- ** presetting
        psThatSettingWindow = new JWindow(frame);

        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (int) screensize.getWidth() / 2;
        int y = (int) screensize.getHeight() / 2;

        psThatSettingWindow.setLocation(x, y);

        JPanel lpPanel = new JPanel();
        lpPanel.setLayout(new GridLayout(0, 1));
        //-- **AddButonn
        JButton lpThisAddButton = new JButton("add");
        ActionListener lpThisAddButtonAction = new ActionListener() {
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
        psThatSettingWindow.setVisible(true);
        //--
    }//+++

    void fsPover() {
        exit();
    }//+++
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
        String title = "ProcessingTest";
        PApplet.main(new String[]{title});
    }
}
