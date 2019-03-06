package com.xrimarako.apps.colordiscblockpuzzle;

class Arc {

    int start;
    int size;
    int color;

    Arc(int start, int size, int color){

        this.start = start;
        this.size = size;
        this.color = color;
    }

    boolean hasConflict(Arc arc){

        if (this.size==0 || arc.size==0){
            return false;
        }

        return this.start + this.size > arc.start &&
                arc.start + arc.size > this.start;

    }
}