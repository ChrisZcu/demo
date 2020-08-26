package model;

public class Region {
    public Position left_top;
    public Position right_btm;

    public Region() {

    }

    public Region(Position lt, Position rb) {
        this.left_top = lt;
        this.right_btm = rb;
    }


}
