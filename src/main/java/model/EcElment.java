package model;


import processing.core.PApplet;

class EcElement extends PApplet {
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
