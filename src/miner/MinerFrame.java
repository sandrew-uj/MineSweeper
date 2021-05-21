package miner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class MinerFrame extends JFrame {
    private final int CELL_LEN = 27;
    private final int IMG_COUNT = 18;
    private final int DEFAULT_WIDTH = 800;
    private final int DEFAULT_HEIGHT = 800;

    private final int EMPTY = 0;
    private final int MINE = 9;
    private final int COVERED = 10;
    private final int MARKED = 11;
    private final int WRONG_MARKED = 12;
    private final int EXPLODED_MINE = 13;

    private int ROWS = 16;
    private int COLUMNS = 16;
    private int MINES_COUNT = 40;

    private JTextField minesField;
    private JButton smile;
    private JTextField timerField;
    private Board board;

    private int[][] cellStates;
    private int[][] visibleCellStates;
    Image[] imgs;

    private boolean inGame = false;
    private int clickCount = 0;
    private int openCells = 0;
    private int setMarks = 0;

    MinerFrame(){
        imgs = new Image[IMG_COUNT];
        for (int i = 0; i < IMG_COUNT; ++i){
            imgs[i] = new ImageIcon("src/resources/"+i+".png").getImage();
        }

        var menuBar = new JMenuBar();
        var settings = new JMenu("Settings");
        menuBar.add(settings);
        setJMenuBar(menuBar);

        ActionListener modeListener = e->{
            String mode = ((JRadioButtonMenuItem)e.getSource()).getText();
            switch(mode){
                case "Easy":
                    ROWS = 9;
                    COLUMNS = 9;
                    MINES_COUNT = 10;
                    break;
                case "Normal":
                    ROWS = 16;
                    COLUMNS = 16;
                    MINES_COUNT = 40;
                    break;
                case "Expert":
                    ROWS = 16;
                    COLUMNS = 30;
                    MINES_COUNT = 99;
                    break;
            }
            setGame();
        };

        var group = new ButtonGroup();
        var easy = new JRadioButtonMenuItem("Easy");
        easy.addActionListener(modeListener);

        var normal = new JRadioButtonMenuItem("Normal");
        normal.addActionListener(modeListener);
        normal.setSelected(true);

        var expert = new JRadioButtonMenuItem("Expert");
        expert.addActionListener(modeListener);

        group.add(easy);
        group.add(normal);
        group.add(expert);
        settings.add(easy);
        settings.add(normal);
        settings.add(expert);

        var statusPanel = new JPanel();
        //statusPanel.setBorder(BorderFactory.createBevelBorder(3));

        minesField = new JTextField(Integer.toString(MINES_COUNT));
        minesField.setEditable(false);
        statusPanel.add(minesField, BorderLayout.WEST);

        smile = new JButton(new ImageIcon(imgs[14]));
        smile.addActionListener(e->{
            setGame();
        });
        statusPanel.add(smile, BorderLayout.CENTER);

        timerField = new JTextField("000");
        timerField.setEditable(false);
        statusPanel.add(timerField, BorderLayout.EAST);
        add(statusPanel, BorderLayout.NORTH);

        addMouseListener(new MouseMiner());
        setGame();
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    void setGame(){
        clickCount = 0;
        openCells = 0;
        setMarks = 0;
        board = null;

        visibleCellStates = new int[ROWS][COLUMNS];
        cellStates = new int[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; ++i)
            for (int j = 0; j < COLUMNS; ++j){
                visibleCellStates[i][j] = COVERED;
                cellStates[i][j] = EMPTY;
            }


        board = new Board();
        //board.setBorder(BorderFactory.createBevelBorder(3));
        add(board, BorderLayout.CENTER);
        repaint();
    }

    void newGame(int firstI, int firstJ){
        inGame = true;
        long start = System.currentTimeMillis();
        var timer = new Timer(1000, e->{
            long time = (System.currentTimeMillis()-start)/1000;
            timerField.setText(Long.toString(time));
        });

        var random = new Random();
        var positions = new ArrayList<Integer>();
        for(int i = 0; i < ROWS*COLUMNS; ++i)
            positions.add(i);
        positions.remove(firstI*COLUMNS+firstJ);

        for (int count = 0; count < MINES_COUNT; ++count){
            int pos = (int)((positions.size()-1)*random.nextDouble());
            int i = positions.get(pos)/COLUMNS;
            int j = positions.get(pos)%COLUMNS;
            positions.remove(pos);

            cellStates[i][j] = MINE;

            for (int y = i-1; y <= i+1; ++y){
                for (int x = j-1; x <= j+1; ++x){
                    if (x > 0 && x < COLUMNS && y > 0 && y < ROWS && cellStates[y][x] != MINE)
                        cellStates[y][x]++;
                }
            }
        }

        /*for (int y = 0; y < ROWS; ++y){
            for (int x = 0; x < COLUMNS; ++x)
                System.out.print(cellStates[y][x]+" ");
            System.out.println();
        }*/
    }

    private class Board extends JPanel{
        @Override
        public void paintComponent(Graphics g){
            for (int i = 0; i < ROWS; ++i){
                for (int j = 0; j < COLUMNS; ++j){
                    g.drawImage(imgs[visibleCellStates[i][j]], -this.getX()+j*CELL_LEN, -this.getY()+i*CELL_LEN, this);
                }
            }
        }
    }

    private class MouseMiner extends MouseAdapter{
        @Override
        public void mousePressed(MouseEvent e) {
            clickCount++;

            int x = e.getX()-board.getX();
            int y = e.getY()-board.getY();

            int i = y/CELL_LEN;
            int j = x/CELL_LEN;

            if (clickCount == 1)
                newGame(i, j);

            if (i > 0 && j > 0 && i < ROWS && j < COLUMNS){
                if (e.getButton() == MouseEvent.BUTTON3){
                    if (visibleCellStates[i][j] == MARKED){
                        visibleCellStates[i][j] = COVERED;
                        setMarks--;
                    }
                    else if (visibleCellStates [i][j] == COVERED && setMarks < MINES_COUNT){
                        visibleCellStates[i][j] = MARKED;
                        setMarks++;
                    }
                    minesField.setText(Integer.toString(MINES_COUNT-setMarks));
                }else if (visibleCellStates[i][j] != MARKED){
                    ++openCells;
                    switch(cellStates[i][j]){
                        case EMPTY:
                            find_empties(i, j);
                            break;
                        case MINE:
                            for (int u = 0; u < ROWS; ++u){
                                for (int v = 0; v < COLUMNS; ++v){
                                    if (cellStates[u][v] == MINE){
                                        if (u == i && v == j)
                                            visibleCellStates[u][v] = EXPLODED_MINE;
                                        else if (visibleCellStates[u][v] != MARKED)
                                            visibleCellStates[u][v] = MINE;
                                    }else if (visibleCellStates[u][v] == MARKED)
                                        visibleCellStates[u][v] = WRONG_MARKED;
                                }
                            }
                            smile.setIcon(new ImageIcon(imgs[16]));
                            inGame = false;
                            break;
                        default:
                            visibleCellStates[i][j] = cellStates[i][j];
                            break;
                    }
                }

                if (inGame && openCells == ROWS*COLUMNS-MINES_COUNT){
                    inGame = false;
                    smile.setIcon(new ImageIcon(imgs[17]));
                }
                board.repaint();
            }
            if (inGame)
                smile.setIcon(new ImageIcon(imgs[15]));

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (inGame)
                smile.setIcon(new ImageIcon(imgs[14]));
        }

    }

    void find_empties(int i, int j){
        visibleCellStates[i][j] = cellStates[i][j];

        if (cellStates[i][j] == EMPTY){
            for (int y = i-1; y <= i+1; ++y){
                for (int x = j-1; x <= j+1; ++x){
                    if (y > 0 && y < ROWS && x > 0 && x < COLUMNS && visibleCellStates[y][x] != EMPTY){
                        find_empties(y, x);
                    }
                }
            }
        }

    }
}