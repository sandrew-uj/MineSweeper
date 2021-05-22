package miner;

import javax.swing.*;
import java.awt.*;

public class MainMiner {                //execute our minesweeper app
    public static void main (String ... args){
        EventQueue.invokeLater(()->{
            var frame = new MinerFrame();
            frame.setTitle("Minesweeper");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setIconImage(frame.imgs[14].getImage());
            frame.setLocationRelativeTo(null);          //set frame on the center of the screen
            frame.setVisible(true);
        });
    }
}
