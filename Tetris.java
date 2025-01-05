package experiment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Tetris extends JPanel implements ActionListener {
    
    private static final int BOARD_WIDTH = 10; // 縦
    private static final int BOARD_HEIGHT = 20; // 横
    private static final int BLOCK_SIZE = 30; // テトリミノ1つのサイズ
    private static final int RIGHT_SPACE_SIZE = 9;
    private static final int BASE_SPEED = 500; // 初期スピード（ミリ秒）


    
    private Timer timer;
    private Tetromino currentTetromino; 
    //private boolean[][] board = new boolean[BOARD_HEIGHT][BOARD_WIDTH]; // Board state
    private Color[][] board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
    private boolean fastDrop = false; 
    private int score = 0; // 現在のスコア
    private String star = "";
 // 爆発エリアを記録するリスト
    private List<Point> explosionArea = new ArrayList<>();
	private Tetromino nextTetromino;
	boolean nextFlg = false;
    

    public Tetris() {
      
    	setPreferredSize(new Dimension((BOARD_WIDTH + RIGHT_SPACE_SIZE) * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));

        setBackground(Color.BLACK);
        setFocusable(true);

        
        timer = new Timer(500, this);
        timer.start();

        
        spawnTetromino();

        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentTetromino != null) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            
                            if (canMove(-1, 0)) currentTetromino.move(-1, 0);
                            break;
                        case KeyEvent.VK_RIGHT:
                            
                            if (canMove(1, 0)) currentTetromino.move(1, 0);
                            break;
                        case KeyEvent.VK_DOWN:
                            
                            fastDrop = true;
                            break;
                        case KeyEvent.VK_X:
                            
                            currentTetromino.rotateR();
                            if (!isValidPosition()) currentTetromino.rotateBack();
                            break;
                        case KeyEvent.VK_Z:
                           
                            currentTetromino.rotateL();
                            if (!isValidPosition()) currentTetromino.rotateBack();
                            break;
                    }
                }
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    
                    fastDrop = false;
                }
            }
        });
    }

    private void showGameOverDialog() {
        int choice = JOptionPane.showOptionDialog(
            this,
            "ゲームオーバーです。再挑戦しますか？",
            "ゲームオーバー",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new Object[]{"再挑戦", "降参"},
            "再挑戦"
        );

        if (choice == JOptionPane.YES_OPTION) {
            resetGame(); // 再挑戦の処理
        } else if (choice == JOptionPane.NO_OPTION) {
            System.exit(0); // 降参の処理（アプリ終了）
        }
    }

    
    private void resetGame() {
        // ボードの初期化
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                board[y][x] = null; // 色情報をリセット
            }
        }

        // 現在のテトリミノをリセット
        currentTetromino = null;
        //スコアを初期化
        score = 0;
        
     // スピードをリセット
        timer.setDelay(BASE_SPEED);

        // ゲームを再開
        spawnTetromino(); // 新しいテトリミノを生成
        timer.start();    // タイマーを再開
        repaint();        // 再描画
    }
    
    private void updateSpeed() {
        int starCount = score / 100; // 星の数（ランク）を計算
        int newSpeed = (int) (BASE_SPEED * Math.pow(0.9, starCount)); // ランクごとに5%減少
        timer.setDelay(Math.max(newSpeed, 100)); // スピードの最小値を100msに設定
    }


    private void spawnTetromino() {
    	if ( nextTetromino == null ) {
    		currentTetromino = Tetromino.randomTetromino();
    	} else {
        
    		currentTetromino = nextTetromino;
    	}

        
        if (!isValidPosition()) {
            timer.stop();
            
            for(int i = 0; i < BOARD_HEIGHT; i++) {
            	for(int j = 0; j < BOARD_WIDTH; j++) {
            		board[i][j] = Color.WHITE;
            	}
            }
            
            repaint();
            
            
            if (!isValidPosition()) {
                timer.stop(); 
                showGameOverDialog(); 
            }
            
        }
        
        nextTetromino = Tetromino.randomTetromino();
        nextFlg = false;
    }
    
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        
        drawBoard(g);
        drawTetromino(g);
        
        
        drawRightPanelBackground(g);

        
        drawScore(g);
        
        
        drawStar(g, score);
        
        if( !nextFlg ) {
        	nextTetromino = Tetromino.randomTetromino();
        	nextFlg = true;
        }
        
        
        drawNextTetromino(g, nextTetromino);
       
    
        if (!explosionArea.isEmpty()) {
            drawExplosion(g);
        }
    }
    
    private void drawRightPanelBackground(Graphics g) {
       
        int panelX = BOARD_WIDTH * BLOCK_SIZE;
        int panelY = 0;
        int panelWidth = RIGHT_SPACE_SIZE * BLOCK_SIZE; 
        int panelHeight = BOARD_HEIGHT * BLOCK_SIZE;

        
        g.setColor(new Color(50, 50, 50)); 
        g.fillRect(panelX, panelY, panelWidth, panelHeight);
        
        g.setColor(Color.DARK_GRAY); 
        g.drawRect(panelX, panelY, panelWidth, panelHeight);
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(32f)); 
        g.drawString("Score: " + score, BOARD_WIDTH * BLOCK_SIZE + 10, 150 + BLOCK_SIZE); 
    }
    
    private Color getStarColor(int rank) {
        if (rank < 6) return Color.YELLOW;
        else if (rank < 11) return Color.RED;
        else if (rank < 16) return Color.GREEN;
        else if (rank < 21) return Color.BLUE;
        else if (rank < 26) return Color.CYAN;
        return Color.ORANGE;
    }
    
    private void drawStar(Graphics g, int s) {
    	int starX = BOARD_WIDTH * BLOCK_SIZE + 10; 
        int starY = 240 + BLOCK_SIZE; 
        int starCount = 0;
        final int POINT = 2500;
        
        starCount = s / POINT;
        
        g.setFont(g.getFont().deriveFont(28f)); 
        g.setColor(Color.WHITE);
        g.drawString("Rank:", starX, 200 + BLOCK_SIZE); 
        //g.setColor(Color.YELLOW);
        
        for (int i = 0; i < starCount; i++) {
            if (i > 0 && i % 5 == 0) {
                starY += 40; 
                starX = BOARD_WIDTH * BLOCK_SIZE + 10; 
            }
            g.setColor(getStarColor(i));
            if(i < 6) {
            	g.setColor(Color.YELLOW);
            } else if(i < 11) {
            	g.setColor(Color.RED);
            } else if(i < 16) {
            	g.setColor(Color.GREEN);
            } else if (i < 21) {
            	g.setColor(Color.BLUE);
            } else if ( i < 26 ) {
            	g.setColor(Color.CYAN);
            } else {
            	g.setColor(Color.ORANGE);
        	}
            g.drawString("★", starX, starY); 
            
            starX += 40; // 次の星の位置に移動
        }
        
        
    }

    private void drawNextTetromino(Graphics g, Tetromino nextTetromino) {
    	
    	g.drawString("Next:",BOARD_WIDTH * BLOCK_SIZE + 10, BLOCK_SIZE );
    	
        if (nextTetromino != null) {
            
            for (Point block : nextTetromino.getShape()) {
                
            	int x = 6 + block.x; 
                int y = 1 + block.y;
                
                g.setColor(nextTetromino.getColor());
                g.fillRect(x * BLOCK_SIZE + 20, y * BLOCK_SIZE + 20, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
    }
    
    private void drawBoard(Graphics g) {
        g.setColor(Color.GRAY);
        
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != null) {
                	g.setColor(board[y][x]); 
                    g.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
    }

    private void drawTetromino(Graphics g) {
    	if (currentTetromino != null) {
            if (currentTetromino.isBomb()) {
                Color color = (System.currentTimeMillis() / 500 % 2 == 0) ? Color.WHITE : Color.BLACK; // 点滅
                g.setColor(color);
            } else {
                g.setColor(currentTetromino.getColor());
            }
            for (Point block : currentTetromino.getShape()) {
                g.fillRect(block.x * BLOCK_SIZE, block.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
      }
    }
    
    private void drawExplosion(Graphics g) {
        g.setColor(Color.WHITE); // 爆発エフェクトの色
        for (Point p : explosionArea) {
            g.fillRect(p.x * BLOCK_SIZE, p.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        }
    }


    private boolean canMove(int dx, int dy) {
       
        for (Point block : currentTetromino.getShape()) {
            int newX = block.x + dx;
            int newY = block.y + dy;
            
            if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT || (newY >= 0 && board[newY][newX] != null)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidPosition() {
        
        for (Point block : currentTetromino.getShape()) {
            int x = block.x;
            int y = block.y;
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT || (y >= 0 && board[y][x] != null)) {
                return false;
            }
        }
        return true;
    }

    private void lockTetromino() {
        if (currentTetromino.isBomb()) {
            
            for (Point block : currentTetromino.getShape()) {
                int x = block.x;
                int y = block.y;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < BOARD_WIDTH && ny >= 0 && ny < BOARD_HEIGHT) {
                            explosionArea.add(new Point(nx, ny));
                        }
                    }
                }
            }

           
            Timer explosionTimer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 爆発エリアを削除
                    for (Point p : explosionArea) {
                        board[p.y][p.x] = null;
                    }
                    explosionArea.clear(); 
                    clearFullLines(); 
                    repaint();
                }
            });
            explosionTimer.setRepeats(false); 
            explosionTimer.start();

            repaint(); 
        } else {
          
            for (Point block : currentTetromino.getShape()) {
                if (block.y >= 0) {
                    board[block.y][block.x] = currentTetromino.getColor();
                }
            }
            clearFullLines();
        }
    }



    
    private void clearFullLines() {
        int linesCleared = 0; 

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean fullLine = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == null) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                
                linesCleared++;
                for (int row = y; row > 0; row--) {
                    System.arraycopy(board[row - 1], 0, board[row], 0, BOARD_WIDTH);
                }
                board[0] = new Color[BOARD_WIDTH];
            }
        }

        // スコア計算（行数に応じた加算）
        switch (linesCleared) {
            case 1:
                score += 100;
                break;
            case 2:
                score += 300;
                break;
            case 3:
                score += 1200;
                break;
            case 4:
                score += 3000;
                break;
            case 5:
                score += 5000;
                break;
            default:
                break;
        }
        
       
		
        updateSpeed();
        repaint(); 
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentTetromino != null) {
            
            int speed = fastDrop ? 75 : 500;
            timer.setDelay(speed);

            
            if (canMove(0, 1)) {
                currentTetromino.move(0, 1);
            } else {
                lockTetromino();
                spawnTetromino();
            }
            repaint();
        }
    }

    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Tetris");
        Tetris tetris = new Tetris();
        frame.add(tetris);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static class Tetromino {
        private Point[] shape;
        private Color color;
        private int x = BOARD_WIDTH / 2, y = 0; 
        private boolean isBomb = false;

        public Tetromino(Point[] shape, Color color, boolean isBomb) {
            this.shape = shape;
            this.color = color;
            this.isBomb = isBomb;
        }
        
        public boolean isBomb() {
            return isBomb;
        }

        public static Tetromino randomTetromino() {
            Random rand = new Random();
            double probability = rand.nextDouble();
            if (probability < 0.03) { // 3%の確率でボムを生成
                return new Tetromino(new Point[]{new Point(0, 0)}, Color.WHITE, true); // ボムは1マス
            } else if (probability < 0.10) {
                return new Tetromino(new Point[]{new Point(0, 0)}, Color.RED, false);
            } else if (probability < 0.15) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0)}, Color.GREEN, false);
            } else if (probability < 0.20) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(0, 1)}, Color.BLUE, false);
            } else if (probability < 0.30) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0)}, Color.CYAN, false); // I-shape
            } else if (probability < 0.35) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(1, 1)}, Color.CYAN, false); // T-shape
            } else if (probability < 0.40) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0)}, Color.MAGENTA, false); // I-shape
            } else if (probability < 0.45) {
                return new Tetromino(new Point[]{new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(1, 3)}, Color.YELLOW, false); // L-shape-R
            } else if (probability < 0.50) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2)}, Color.RED, false); // L-shape-L
            } else if (probability < 0.55) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}, Color.ORANGE, false); // Sado Right 4-shape
            } else if (probability < 0.60) {
                return new Tetromino(new Point[]{new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 1)}, Color.PINK, false); // Sado Left 4-shape
            } else if (probability < 0.65) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(0, 2)}, Color.ORANGE, false); // P-shape
            } else if (probability < 0.70) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(0, 2), new Point(1, 2)}, Color.PINK, false); // q-shape
            } else if (probability < 0.75) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(1, 1), new Point(1, 2)}, Color.GRAY, false); // U-shape
            } else if (probability < 0.80) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)}, Color.ORANGE, false); // Sado Right
            } else if (probability < 0.85) {
                return new Tetromino(new Point[]{new Point(2, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2)}, Color.PINK, false); // Sado Left
            } else if (probability < 0.90) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 1), new Point(2, 2)}, Color.ORANGE, false); // L-shape-5t
            } else if (probability < 0.95) {
                return new Tetromino(new Point[]{new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(1, 2), new Point(0, 2)}, Color.PINK, false); // R-shape-5
            } else if (probability < 0.98) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0), new Point(4, 0)}, Color.WHITE, false); // I-shape-5
            } else {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1)}, Color.YELLOW, false);
            }
        }

        public Point[] getShape() {
            Point[] blocks = new Point[shape.length];
            for (int i = 0; i < shape.length; i++) {
                blocks[i] = new Point(shape[i].x + x, shape[i].y + y);
            }
            return blocks;
        }

        public Color getColor() {
           
            return color;
        }

        public void move(int dx, int dy) {
            
            x += dx;
            y += dy;
        }

        public void rotateR() {
            
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].x;
                shape[i].x = -shape[i].y;
                shape[i].y = temp;
            }
        }
        
        public void rotateL() {
           
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].y;
                shape[i].y = -shape[i].x;
                shape[i].x = temp;
            }
        }

        public void rotateBack() {
            
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].x;
                shape[i].x = shape[i].y;
                shape[i].y = -temp;
            }
        }
    }
}
