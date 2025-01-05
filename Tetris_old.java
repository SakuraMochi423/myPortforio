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

public class Tetris_old extends JPanel implements ActionListener {
    // Constants for the board dimensions and block size
    private static final int BOARD_WIDTH = 10; // Number of columns on the board
    private static final int BOARD_HEIGHT = 20; // Number of rows on the board
    private static final int BLOCK_SIZE = 30; // Size of each block in pixels
    private static final int RIGHT_SPACE_SIZE = 9;
    private static final int BASE_SPEED = 500; // 初期スピード（ミリ秒）


    // Timer for game loop updates
    private Timer timer;
    private Tetromino currentTetromino; // The currently falling Tetromino
    //private boolean[][] board = new boolean[BOARD_HEIGHT][BOARD_WIDTH]; // Board state
    private Color[][] board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
    private boolean fastDrop = false; // Whether the piece is dropping quickly
    private int score = 0; // 現在のスコア
    private String star = "";
 // 爆発エリアを記録するリスト
    private List<Point> explosionArea = new ArrayList<>();
	private Tetromino nextTetromino;
	boolean nextFlg = false;
    

    public Tetris_old() {
        // Set up the game panel
        //setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
    	setPreferredSize(new Dimension((BOARD_WIDTH + RIGHT_SPACE_SIZE) * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));

        setBackground(Color.BLACK);
        setFocusable(true);

        // Initialize the game timer
        timer = new Timer(500, this);
        timer.start();

        // Spawn the first Tetromino
        spawnTetromino();

        // Set up keyboard controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentTetromino != null) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            // Move left if possible
                            if (canMove(-1, 0)) currentTetromino.move(-1, 0);
                            break;
                        case KeyEvent.VK_RIGHT:
                            // Move right if possible
                            if (canMove(1, 0)) currentTetromino.move(1, 0);
                            break;
                        case KeyEvent.VK_DOWN:
                            // Enable fast drop
                            fastDrop = true;
                            break;
                        case KeyEvent.VK_X:
                            // Rotate the Tetromino
                            currentTetromino.rotateR();
                            if (!isValidPosition()) currentTetromino.rotateBack();
                            break;
                        case KeyEvent.VK_Z:
                            // Rotate the Tetromino
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
                    // Disable fast drop when the down key is released
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
        // Create a new random Tetromino
    		currentTetromino = nextTetromino;
    	}

        // Check if the initial position is valid; end the game if not
        if (!isValidPosition()) {
            timer.stop();
            
            for(int i = 0; i < BOARD_HEIGHT; i++) {
            	for(int j = 0; j < BOARD_WIDTH; j++) {
            		board[i][j] = Color.WHITE;
            	}
            }
            
            repaint();
            
            // 初期位置が無効な場合、ゲームオーバー
            if (!isValidPosition()) {
                timer.stop(); // ゲームを停止
                showGameOverDialog(); // ダイアログを表示
            }
            
        }
        
        nextTetromino = Tetromino.randomTetromino();
        nextFlg = false;
    }
    

    //@Override
    /*public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the game board and the current Tetromino
        drawBoard(g);
        drawTetromino(g);
    }*/
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // ボードとテトリミノを描画
        drawBoard(g);
        drawTetromino(g);
        
        // 右側スペースの背景を描画
        drawRightPanelBackground(g);

        // スコアを描画
        drawScore(g);
        
        //　ランクを描画
        drawStar(g, score);
        
        if( !nextFlg ) {
        	//次に落ちてくるテトリミノを取得
        	nextTetromino = Tetromino.randomTetromino();
        	nextFlg = true;
        }
        
        //次に落ちてくるテトリミノを右側スペースに描画
        drawNextTetromino(g, nextTetromino);
       
     // 爆発エフェクトを描画
        if (!explosionArea.isEmpty()) {
            drawExplosion(g);
        }
    }
    
    private void drawRightPanelBackground(Graphics g) {
        // 右側スペースの座標とサイズを計算
        int panelX = BOARD_WIDTH * BLOCK_SIZE;
        int panelY = 0;
        int panelWidth = RIGHT_SPACE_SIZE * BLOCK_SIZE; // 右側のスペース幅
        int panelHeight = BOARD_HEIGHT * BLOCK_SIZE;

        // 背景色を設定して矩形を塗りつぶし
        g.setColor(new Color(50, 50, 50)); // 濃い灰色
        g.fillRect(panelX, panelY, panelWidth, panelHeight);
        
        g.setColor(Color.DARK_GRAY); // 境界線の色
        g.drawRect(panelX, panelY, panelWidth, panelHeight);
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(32f)); // フォントサイズを32pxに設定
        g.drawString("Score: " + score, BOARD_WIDTH * BLOCK_SIZE + 10, 150 + BLOCK_SIZE); // スコアを描画
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
    	int starX = BOARD_WIDTH * BLOCK_SIZE + 10; // 開始位置のX座標
        int starY = 240 + BLOCK_SIZE; // 開始位置のY座標
        int starCount = 0;
        final int POINT = 100;
        
        starCount = s / POINT;
        
        g.setFont(g.getFont().deriveFont(28f)); // フォントサイズを28pxに設定
        g.setColor(Color.WHITE);
        g.drawString("Rank:", starX, 200 + BLOCK_SIZE); 
        //g.setColor(Color.YELLOW);
        
        for (int i = 0; i < starCount; i++) {
            if (i > 0 && i % 5 == 0) {
                starY += 40; // 5つ星ごとに新しい行に移動
                starX = BOARD_WIDTH * BLOCK_SIZE + 10; // X座標をリセット
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
            //g.fillOval(starX, starY, 30, 30); // 星を丸で描画（後で画像に変更可能）
            starX += 40; // 次の星の位置に移動
        }
        
        

    	/*if (s < 3) {
    		g.setColor(Color.RED);
    	} else if (s < 6) {
    		g.setColor(Color.CYAN);
    	} else if ( s < 10 ) {
    		g.setColor(Color.MAGENTA);
    	} else {
    		g.setColor(Color.YELLOW);
    	}
        g.setFont(g.getFont().deriveFont(28f)); // フォントサイズを28pxに設定
        g.drawString("Lank:" + star, BOARD_WIDTH * BLOCK_SIZE + 10, 100); // 星を描画
        */
    }

    private void drawNextTetromino(Graphics g, Tetromino nextTetromino) {
    	
    	g.drawString("Next:",BOARD_WIDTH * BLOCK_SIZE + 10, BLOCK_SIZE );
    	
        if (nextTetromino != null) {
            
            for (Point block : nextTetromino.getShape()) {
                //int x = BOARD_WIDTH + 1 + block.x; // ボードの右隣に描画
            	int x = 6 + block.x; // ボードの右隣に描画
                int y = 1 + block.y;
                //g.setColor(Color.BLACK);
                //g.fillRect(320 , 10, 120, 120);
                g.setColor(nextTetromino.getColor());
                g.fillRect(x * BLOCK_SIZE + 20, y * BLOCK_SIZE + 20, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
    }
    
    private void drawBoard(Graphics g) {
        g.setColor(Color.GRAY);
        // Iterate through the board array and draw filled cells
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != null) {
                	g.setColor(board[y][x]); // 色を設定
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
        // Check if the Tetromino can move in the specified direction
        for (Point block : currentTetromino.getShape()) {
            int newX = block.x + dx;
            int newY = block.y + dy;
            // Ensure the block stays within bounds and doesn't collide with existing blocks
            if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT || (newY >= 0 && board[newY][newX] != null)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidPosition() {
        // Check if the current Tetromino is in a valid position
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
            // 爆発エリアを記録
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

            // エフェクトを100ms表示してから周囲を削除
            Timer explosionTimer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 爆発エリアを削除
                    for (Point p : explosionArea) {
                        board[p.y][p.x] = null;
                    }
                    explosionArea.clear(); // 爆発エリアをリセット
                    clearFullLines(); // ボードを更新
                    repaint();
                }
            });
            explosionTimer.setRepeats(false); // 一度だけ実行
            explosionTimer.start();

            repaint(); // エフェクトを描画
        } else {
            // 通常のテトリミノ固定処理
            for (Point block : currentTetromino.getShape()) {
                if (block.y >= 0) {
                    board[block.y][block.x] = currentTetromino.getColor();
                }
            }
            clearFullLines();
        }
    }



    /*private void clearFullLines() {
        // Remove any full lines from the board
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean fullLine = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == null) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                // Shift all rows above the cleared line down
                for (int row = y; row > 0; row--) {
                    System.arraycopy(board[row - 1], 0, board[row], 0, BOARD_WIDTH);
                }
                board[0] = new Color[BOARD_WIDTH];
            }
        }
    }*/
    
    private void clearFullLines() {
        int linesCleared = 0; // 消えた行数をカウント

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean fullLine = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == null) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                // 行を削除
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
        
        /*
        int i = 0;
        i = score / 100;
        for ( int j = 0; j < i; j++ ) {
        	star = star + "★";
        }
		*/
		
        updateSpeed(); // スピードを更新
        repaint(); // 再描画
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentTetromino != null) {
            // Adjust the timer delay based on whether fast drop is enabled
            int speed = fastDrop ? 75 : 500;
            timer.setDelay(speed);

            // Move the Tetromino down or lock it in place if it can't move further
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
        // Create the game window and start the game
        JFrame frame = new JFrame("Tetris");
        Tetris_old tetris = new Tetris_old();
        frame.add(tetris);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static class Tetromino {
        private Point[] shape; // Array of blocks representing the Tetromino shape
        private Color color; // Color of the Tetromino
        private int x = BOARD_WIDTH / 2, y = 0; // Initial position of the Tetromino
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
            // Generate a random Tetromino with predefined probabilities
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
            // Return the blocks of the Tetromino adjusted to its current position
            Point[] blocks = new Point[shape.length];
            for (int i = 0; i < shape.length; i++) {
                blocks[i] = new Point(shape[i].x + x, shape[i].y + y);
            }
            return blocks;
        }

        public Color getColor() {
            // Return the color of the Tetromino
            return color;
        }

        public void move(int dx, int dy) {
            // Move the Tetromino by the specified amounts
            x += dx;
            y += dy;
        }

        public void rotateR() {
            // Rotate the Tetromino clockwise
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].x;
                shape[i].x = -shape[i].y;
                shape[i].y = temp;
            }
        }
        
        public void rotateL() {
            // Rotate the Tetromino clockwise
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].y;
                shape[i].y = -shape[i].x;
                shape[i].x = temp;
            }
        }

        public void rotateBack() {
            // Undo the rotation (counterclockwise)
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].x;
                shape[i].x = shape[i].y;
                shape[i].y = -temp;
            }
        }
    }
}
