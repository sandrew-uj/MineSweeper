package miner;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class MinerFrame extends JFrame {
    private final int CELL_LEN = 27;        //constants
    private final int IMG_COUNT = 18;
    private final int DEFAULT_WIDTH = 800;
    private final int DEFAULT_HEIGHT = 800;
    private final int BTHICKNESS = 5;       //thickness of border

    private final int EMPTY = 0;        //constants for cell states
    private final int MINE = 9;
    private final int COVERED = 10;
    private final int MARKED = 11;
    private final int WRONG_MARKED = 12;
    private final int EXPLODED_MINE = 13;

    private int ROWS = 16;      //board sizes
    private int COLUMNS = 16;
    private int MINES_COUNT = 40;

    private JPanel statusPanel;     //panel with mine count, smile, and timer
    private JTextField minesField;  //text field with mines count
    private JButton smile;          //button with smile
    private JTextField timerField;  //text field with timer
    private Board board;            //board where displays states of cells

    private int[][] cellStates;     //cells states under the buttons
    private int[][] visibleCellStates;  //cells states on the buttons
    ImageIcon[] imgs;   //array for images

    private boolean inGame = false;
    private int clickCount = 0;
    private int openCells = 0;
    private int setMarks = 0;       //current count of marks on board
    private long start = 0;         //stores time in millis, when timer starts
    private Timer timer;

    MinerFrame(){
        imgs = new ImageIcon[IMG_COUNT];
        for (int i = 0; i < IMG_COUNT; ++i){        //add images
            imgs[i] = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/"+i+".png")));
        }

        var menuBar = new JMenuBar();       //add menu bar
        var settings = new JMenu("Settings");
        menuBar.add(settings);
        setJMenuBar(menuBar);

        ActionListener modeListener = e->{      //add radio buttons with modes of game
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

        statusPanel = new JPanel();
        statusPanel.setBackground(Color.LIGHT_GRAY);

        minesField = new JTextField(3);
        minesField.setEditable(false);
        minesField.setForeground(Color.RED);
        minesField.setBackground(Color.BLACK);
        statusPanel.add(minesField, BorderLayout.WEST);

        smile = new JButton(imgs[14]);      //when you click on smile game resets
        smile.addActionListener(e->{
            setGame();
        });
        statusPanel.add(smile, BorderLayout.CENTER);

        timerField = new JTextField(3);
        timerField.setEditable(false);
        timerField.setForeground(Color.RED);
        timerField.setBackground(Color.BLACK);
        statusPanel.add(timerField, BorderLayout.EAST);

        String filename="/resources/BittypixCountdown-92M2.ttf";

        try{
            var font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream(filename));   //derive special font for text fields
            font = font.deriveFont(Font.BOLD,32);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            timerField.setFont(font);
            minesField.setFont(font);
        }catch(Exception e){
            System.out.println(e);
        }

        addMouseListener(new MouseMiner());     //add mouse listener to catch mouse clicks
        setGame();
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    void setGame(){                 //set start board
        inGame = true;
        clickCount = 0;
        openCells = 0;
        setMarks = 0;
        if (timer != null)
            timer.stop();


        statusPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY),
                BorderFactory.createMatteBorder(BTHICKNESS, BTHICKNESS, BTHICKNESS, BTHICKNESS, Color.GRAY)));
        minesField.setText(Integer.toString(MINES_COUNT));
        smile.setIcon(imgs[14]);                        //set default icon for smile
        timerField.setText("0");

        visibleCellStates = new int[ROWS][COLUMNS];
        cellStates = new int[ROWS][COLUMNS];
        for (int i = 0; i < ROWS; ++i)
            for (int j = 0; j < COLUMNS; ++j){      //set defaults for cell states
                visibleCellStates[i][j] = COVERED;
                cellStates[i][j] = EMPTY;
            }
        if (board != null){         //remove old board
            board.removeAll();
            board.setVisible(false);
        }

        board = new Board();        //add new board
        board.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY),
                BorderFactory.createMatteBorder(BTHICKNESS, BTHICKNESS, BTHICKNESS, BTHICKNESS, Color.GRAY)));
        board.setSize(COLUMNS*CELL_LEN, ROWS*CELL_LEN);

        setLayout(new GridBagLayout());     //set GridBagLayout with helper class GBC
        add(statusPanel, new GBC(0, 0, 1, 1).setFill(GBC.BOTH));
        add(board, new GBC(0, 1, 1, 7).setAnchor(GBC.CENTER));

        repaint();
    }

    private class Board extends JPanel{
        private JLabel[][] ComponentCells;      //array of labels, where we then place our images for cells

        Board(){
            setLayout(new GridLayout(ROWS, COLUMNS));       //set ComponentCells to default values
            ComponentCells = new JLabel[ROWS][COLUMNS];
            for (int i = 0; i < ROWS; ++i){
                for (int j = 0; j < COLUMNS; ++j){
                    add(ComponentCells[i][j] = new JLabel(""));
                }
            }
        }

        @Override
        public void paintComponent(Graphics g){     //paint images
            super.paintComponent(g);
            for (int i = 0; i < ROWS; ++i){
                for (int j = 0; j < COLUMNS; ++j){
                    ComponentCells[i][j].setIcon(imgs[visibleCellStates[i][j]]);
                }
            }

        }
    }

    void newGame(int firstI, int firstJ){       //invokes when new game starts by first click
        start = System.currentTimeMillis();     //and gets indexes in matrix of first clicked cell
        timer = new Timer(1000, e->{
            long time = (System.currentTimeMillis()-start)/1000;
            timerField.setText(Long.toString(time));
        });
        timer.start();      //start timer

        var random = new Random();                  //put mines in random sequence
        var positions = new ArrayList<Integer>();   //array, where we would store positions without bombs
        for(int i = 0; i < ROWS*COLUMNS; ++i)
            positions.add(i);
        positions.remove(firstI*COLUMNS+firstJ);    //remove from this array first clicked cell

        for (int count = 0; count < MINES_COUNT; ++count){
            int pos = (int)((positions.size()-1)*random.nextDouble());
            int i = positions.get(pos)/COLUMNS;
            int j = positions.get(pos)%COLUMNS;
            positions.remove(pos);

            cellStates[i][j] = MINE;

            for (int y = i-1; y <= i+1; ++y){           //add +1 for value in nearby cells
                for (int x = j-1; x <= j+1; ++x){
                    if (x >= 0 && x < COLUMNS && y >= 0 && y < ROWS && cellStates[y][x] != MINE)
                        cellStates[y][x]++;
                }
            }
        }
    }


    private class MouseMiner extends MouseAdapter{
        @Override
        public void mousePressed(MouseEvent e) {
            clickCount++;

            int x = e.getLocationOnScreen().x-board.getLocationOnScreen().x;    //coordinates of click in board
            int y = e.getLocationOnScreen().y-board.getLocationOnScreen().y;

            int i = y/CELL_LEN;     //indexes in matrix
            int j = x/CELL_LEN;


            if (i >= 0 && j >= 0 && i < ROWS && j < COLUMNS && inGame){
                if (clickCount == 1)        //if it is first click than start game
                    newGame(i, j);
                if (e.getButton() == MouseEvent.BUTTON3){       //if it is right button set or remove mark
                    if (visibleCellStates[i][j] == MARKED){
                        visibleCellStates[i][j] = COVERED;
                        setMarks--;
                    }
                    else if (visibleCellStates [i][j] == COVERED && setMarks < MINES_COUNT){
                        visibleCellStates[i][j] = MARKED;
                        setMarks++;
                    }
                    minesField.setText(Integer.toString(MINES_COUNT-setMarks));
                }else if (visibleCellStates[i][j] == COVERED){
                    switch(cellStates[i][j]){
                        case EMPTY:             //find all empty cells
                            find_empties(i, j);
                            break;
                        case MINE:          //we lose
                            for (int u = 0; u < ROWS; ++u){         //set visible cell states
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
                            smile.setIcon(imgs[16]);        //set dead smile
                            inGame = false;
                            timer.stop();
                            break;
                        default:
                            visibleCellStates[i][j] = cellStates[i][j];     //just open cell
                            ++openCells;
                            break;
                    }
                }
                if (inGame && openCells == ROWS*COLUMNS-MINES_COUNT){       //we win
                    inGame = false;
                    smile.setIcon(imgs[17]);        //set cool smile
                    timer.stop();
                }
                board.repaint();
            }
            if (inGame)
                smile.setIcon(imgs[15]);        //set smile with rounded mouth, when mouse pressed
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (inGame)
                smile.setIcon(imgs[14]);
        }

    }

    void find_empties(int i, int j){            //find all empties, that lay nearby first clicked empty cell
        if (visibleCellStates[i][j] == COVERED){
            visibleCellStates[i][j] = cellStates[i][j];
            openCells++;
        }

        if (cellStates[i][j] == EMPTY){
            for (int y = i-1; y <= i+1; ++y){       //search in nearby cells
                for (int x = j-1; x <= j+1; ++x){
                    if (y >= 0 && y < ROWS && x >= 0 && x < COLUMNS && visibleCellStates[y][x] != EMPTY){
                        find_empties(y, x);     //use recursive algorithm
                    }
                }
            }
        }
    }
}
