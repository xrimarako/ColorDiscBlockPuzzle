package com.xrimarako.apps.colordiscblockpuzzle;

public class Piece {

    Arc arc0;
    Arc arc1;
    Arc arc2;
    Arc arc3;
    Arc arc4;
    Arc arc5;

    private int pieceID;
    float deg;
    boolean entered = false;

    public Piece(Arc arc0, Arc arc1, Arc arc2, Arc arc3, Arc arc4, Arc arc5, int pieceID, float deg){

        this.arc0 = arc0;
        this.arc1 = arc1;
        this.arc2 = arc2;
        this.arc3 = arc3;
        this.arc4 = arc4;
        this.arc5 = arc5;

        this.pieceID = pieceID;
        this.deg = deg;
    }

    public int getPieceID(){
        return pieceID;
    }

    public Arc[] getArcs(){

        return new Arc[]{arc0, arc1, arc2, arc3, arc4, arc5};
    }

    public int getScore(){

        int total = 0;

        for (Arc arc: getArcs()){
            total+=arc.size*10;
        }

        return total;
    }
}
