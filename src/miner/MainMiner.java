package miner;

import javax.swing.*;
import java.awt.*;

public class MainMiner {
    public static void main (String ... args){
        EventQueue.invokeLater(()->{
            var frame = new MinerFrame();
            frame.setTitle("Minesweeper");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setIconImage(frame.imgs[14]);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
