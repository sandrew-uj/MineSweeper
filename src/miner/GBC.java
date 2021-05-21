package miner;

import java.awt.*;

public class GBC extends GridBagConstraints{        //convenient class for comfortable using GridBagConstraints
    public GBC(int gridx, int gridy){           //constructor, that sets gridx, gridy
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public GBC(int gridx, int gridy, int width, int height){        //constructor, that sets gridx, gridy, width, height
        this.gridx = gridx;
        this.gridy = gridy;
        this.gridwidth = width;
        this.gridheight = height;
    }

    public GBC setAnchor(int anchor){
        this.anchor = anchor;
        return this;
    }

    public GBC setFill(int fill) {
        this.fill = fill;
        return this;
    }

    public GBC setWeight(double weightx, double weighty){
        this.weightx = weightx;
        this.weighty = weighty;
        return this;
    }

    public GBC setInsets(int distance){                                             //sets one distance on the top, left, bottom, right
        this.insets = new Insets(distance, distance, distance, distance);
        return this;
    }

    public GBC setInsets(int top, int right, int bottom, int left){     //sets various values of insets
        this.insets = new Insets(top, left, bottom, right);
        return this;
    }

    public GBC setIpad(int ipadx, int ipady){           //sets padding
        this.ipadx = ipadx;
        this.ipady = ipady;
        return this;
    }

}
