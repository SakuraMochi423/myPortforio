/*
テトリスプログラム
いわゆるテトリスのゲームです。通常のテトリスは４マスのテトリミノがいろいろな形で落ちてきて、横一列に並べば消えて、点数が増えますが、
こちらのテトリスのゲームは、1～5マスのテトリミノがランダムな数、形で落ちてきます。横一列に並べば消えて、点数が増えます。たくさんの列を消せば消すほど、
大きい点数が増えます。
操作方法；
キーボード左：左に移動　キーボード右：右に移動　キーボード下：押している間は通常の1.5倍の速度でテトリミノが落ちます。
キーボードZ：左回転　キーボードX：右回転
です。
2500点ごとにスコアが表示される下のRANKのスペースに、星マークがつきます。星マーク1つにつき、テトリミノの落下速度が20％早くなります。
ゲームの緊張感が上がって、スリルを感じられます。
*/
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

// テトリスゲームクラス
public class Tetris_WithComment extends JPanel implements ActionListener {

    // 定数（ボードやゲームの設定値）
    private static final int BOARD_WIDTH = 10; // ボードの横幅（ブロック数）
    private static final int BOARD_HEIGHT = 20; // ボードの縦幅（ブロック数）
    private static final int BLOCK_SIZE = 30; // 1ブロックのサイズ（ピクセル）
    private static final int RIGHT_SPACE_SIZE = 9; // 右側パネルの幅（ブロック数）
    private static final int BASE_SPEED = 500; // 初期スピード（ミリ秒）

    // ゲーム状態を管理する変数
    private Timer timer; // ゲームループ用のタイマー
    private Tetromino currentTetromino; // 現在のテトリミノ
    private Color[][] board = new Color[BOARD_HEIGHT][BOARD_WIDTH]; // ボードの状態（各マスの色）
    private boolean fastDrop = false; // 高速落下中かどうかのフラグ
    private int score = 0; // 現在のスコア
    private List<Point> explosionArea = new ArrayList<>(); // 爆発エリアを記録するリスト
    private Tetromino nextTetromino; // 次のテトリミノ
    private boolean nextFlg = false; // 次のテトリミノ生成フラグ

    // コンストラクタ
    public Tetris_WithComment() {
        // パネルのサイズと基本設定
        setPreferredSize(new Dimension((BOARD_WIDTH + RIGHT_SPACE_SIZE) * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        setBackground(Color.BLACK); // 背景色を黒に設定
        setFocusable(true); // キーボード入力を有効化

        // ゲームタイマーを初期化（500ms間隔で動作）
        timer = new Timer(BASE_SPEED, this);
        timer.start(); // タイマー開始

        spawnTetromino(); // 最初のテトリミノを生成

        // キーボード入力処理の設定
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentTetromino != null) {
                    // キー入力による動作を判定
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT: // 左移動
                            if (canMove(-1, 0)) currentTetromino.move(-1, 0);
                            break;
                        case KeyEvent.VK_RIGHT: // 右移動
                            if (canMove(1, 0)) currentTetromino.move(1, 0);
                            break;
                        case KeyEvent.VK_DOWN: // 高速落下
                            fastDrop = true;
                            break;
                        case KeyEvent.VK_X: // 右回転
                            currentTetromino.rotateR();
                            if (!isValidPosition()) currentTetromino.rotateBack();
                            break;
                        case KeyEvent.VK_Z: // 左回転
                            currentTetromino.rotateL();
                            if (!isValidPosition()) currentTetromino.rotateBack();
                            break;
                    }
                }
                repaint(); // 画面を再描画
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // キーリリース時の処理
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    fastDrop = false; // 高速落下解除
                }
            }
        });
    }

    // ゲームオーバー時のダイアログ表示。再挑戦を選択すれば、ゲームがリセットされてまた最初から。
    //降参を選択すれば、ゲーム終了。
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
            resetGame(); // ゲームリセット
        } else if (choice == JOptionPane.NO_OPTION) {
            System.exit(0); // プログラム終了
        }
    }

    // ゲームをリセットする処理
    private void resetGame() {
        // ボード状態をクリア
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                board[y][x] = null; // 各マスの色をリセット
            }
        }

        currentTetromino = null; // 現在のテトリミノをリセット
        score = 0; // スコアリセット
        timer.setDelay(BASE_SPEED); // スピードを初期値に設定

        spawnTetromino(); // 新しいテトリミノを生成
        timer.start(); // ゲーム再開
        repaint(); // 再描画
    }

    // ゲームスピードの更新
    private void updateSpeed() {
        int starCount = score / 100; // スコアに応じてランクを計算
        int newSpeed = (int) (BASE_SPEED * Math.pow(0.9, starCount)); // 新しいスピードを計算
        timer.setDelay(Math.max(newSpeed, 100)); // スピードを100ms以下にしない
    }

    // 新しいテトリミノを生成
    private void spawnTetromino() {
        if (nextTetromino == null) {
            currentTetromino = Tetromino.randomTetromino(); // ランダムテトリミノを生成
        } else {
            currentTetromino = nextTetromino; // 次のテトリミノを使用
        }

        //テトリミノが天井まで積みあがったら、ゲーム終了
        if (!isValidPosition()) {
            timer.stop(); // ゲーム終了
            showGameOverDialog();
        }

        nextTetromino = Tetromino.randomTetromino(); // 次のテトリミノを準備
        nextFlg = false; // フラグをリセット
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBoard(g); // ボード描画
        drawTetromino(g); // 現在のテトリミノ描画
        drawRightPanelBackground(g); // 右パネルの背景を描画
        drawScore(g); // スコア描画
        drawStar(g, score); // ランク（星）を描画
        drawNextTetromino(g, nextTetromino); // 次のテトリミノ描画

        if (!explosionArea.isEmpty()) {
            drawExplosion(g); // 爆発エリアを描画
        }
    }

    // ボードを描画するメソッド
    private void drawBoard(Graphics g) {
        g.setColor(Color.GRAY); // 枠線の色
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != null) {
                    g.setColor(board[y][x]); // マスの色を取得
                    g.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE); // 塗りつぶし
                }
            }
        }
    }

    // 現在のテトリミノを描画するメソッド
    private void drawTetromino(Graphics g) {
        if (currentTetromino != null) {
            g.setColor(currentTetromino.getColor());
            for (Point block : currentTetromino.getShape()) {
                g.fillRect(block.x * BLOCK_SIZE, block.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE); // 各ブロックを描画
            }
        }
    }

    // 爆発エリアを描画するメソッド
    private void drawExplosion(Graphics g) {
        g.setColor(Color.WHITE); // 爆発エフェクトの色を白に設定
        for (Point p : explosionArea) {
            g.fillRect(p.x * BLOCK_SIZE, p.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE); // 各爆発エリアを塗りつぶし
        }
    }

    // 右側パネルの背景を描画するメソッド
    private void drawRightPanelBackground(Graphics g) {
        int panelX = BOARD_WIDTH * BLOCK_SIZE; // 右側パネルの開始X座標
        int panelY = 0; // 開始Y座標
        int panelWidth = RIGHT_SPACE_SIZE * BLOCK_SIZE; // パネルの幅
        int panelHeight = BOARD_HEIGHT * BLOCK_SIZE; // パネルの高さ

        g.setColor(new Color(50, 50, 50)); // パネルの背景色を設定
        g.fillRect(panelX, panelY, panelWidth, panelHeight); // パネル全体を塗りつぶし

        g.setColor(Color.DARK_GRAY); // 枠線の色を設定
        g.drawRect(panelX, panelY, panelWidth, panelHeight); // パネルの枠を描画
    }

    // スコアを描画するメソッド
    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE); // スコアの文字色を白に設定
        g.setFont(g.getFont().deriveFont(32f)); // フォントサイズを変更
        g.drawString("Score: " + score, BOARD_WIDTH * BLOCK_SIZE + 10, 150 + BLOCK_SIZE); // スコアの描画位置を設定
    }

    // スコアに応じた星（ランク）を描画するメソッド
    private void drawStar(Graphics g, int s) {
        int starX = BOARD_WIDTH * BLOCK_SIZE + 10; // 星の描画開始X座標
        int starY = 240 + BLOCK_SIZE; // 星の描画開始Y座標
        int starCount = s / 2500; // スコアに基づいて星の数を計算

        g.setFont(g.getFont().deriveFont(28f)); // フォントサイズを変更
        g.setColor(Color.WHITE);
        g.drawString("Rank:", starX, 200 + BLOCK_SIZE); // "Rank"のラベルを描画

        for (int i = 0; i < starCount; i++) {
            if (i > 0 && i % 5 == 0) {
                starY += 40; // 星が5個ごとに次の行に移動
                starX = BOARD_WIDTH * BLOCK_SIZE + 10; // X座標をリセット
            }
            g.setColor(getStarColor(i)); // 星の色を取得
            g.drawString("★", starX, starY); // 星を描画
            starX += 40; // 次の星の描画位置を設定
        }
    }

    // 星の色を決定するメソッド
    private Color getStarColor(int rank) {
        if (rank < 6) return Color.YELLOW; // ランク6未満は黄色
        else if (rank < 11) return Color.RED; // ランク11未満は赤
        else if (rank < 16) return Color.GREEN; // ランク16未満は緑
        else if (rank < 21) return Color.BLUE; // ランク21未満は青
        else if (rank < 26) return Color.CYAN; // ランク26未満は水色
        return Color.ORANGE; // その他はオレンジ
    }

    // 次のテトリミノを描画するメソッド
    private void drawNextTetromino(Graphics g, Tetromino nextTetromino) {
        g.setColor(Color.WHITE);
        g.drawString("Next:", BOARD_WIDTH * BLOCK_SIZE + 10, BLOCK_SIZE); // "Next"のラベルを描画

        if (nextTetromino != null) {
            for (Point block : nextTetromino.getShape()) {
                int x = 6 + block.x; // 次のテトリミノを右側に表示するためにオフセット
                int y = 1 + block.y; // 次のテトリミノのY位置オフセット
                g.setColor(nextTetromino.getColor()); // テトリミノの色を設定
                g.fillRect(x * BLOCK_SIZE + 20, y * BLOCK_SIZE + 20, BLOCK_SIZE, BLOCK_SIZE); // テトリミノを描画
            }
        }
    }

    // テトリミノが移動可能か確認するメソッド
    private boolean canMove(int dx, int dy) {
        for (Point block : currentTetromino.getShape()) {
            int newX = block.x + dx;
            int newY = block.y + dy;

            // 移動先がボードの範囲外または既に埋まっている場合は移動不可
            if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT || (newY >= 0 && board[newY][newX] != null)) {
                return false;
            }
        }
        return true; // 全てのブロックが移動可能であればtrue
    }

    // テトリミノが現在の位置に置けるか確認するメソッド
    private boolean isValidPosition() {
        for (Point block : currentTetromino.getShape()) {
            int x = block.x;
            int y = block.y;

            // ボード範囲外や既に埋まっている位置であれば無効
            if (x < 0 || x >= BOARD_WIDTH || y >= BOARD_HEIGHT || (y >= 0 && board[y][x] != null)) {
                return false;
            }
        }
        return true; // 全ての位置が有効であればtrue
    }

    // テトリミノをボードに固定するメソッド
    private void lockTetromino() {
        if (currentTetromino.isBomb()) {
            // ボムの場合、周囲を爆発させる
            for (Point block : currentTetromino.getShape()) {
                int x = block.x;
                int y = block.y;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < BOARD_WIDTH && ny >= 0 && ny < BOARD_HEIGHT) {
                            explosionArea.add(new Point(nx, ny)); // 爆発範囲に追加
                        }
                    }
                }
            }

            // 爆発エリアの処理をタイマーで遅延実行
            Timer explosionTimer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (Point p : explosionArea) {
                        board[p.y][p.x] = null; // 爆発範囲をクリア
                    }
                    explosionArea.clear(); // 爆発範囲をリセット
                    clearFullLines(); // ライン消去を実行
                    repaint(); // 再描画
                }
            });
            explosionTimer.setRepeats(false); // タイマーを1回だけ実行
            explosionTimer.start(); // 爆発処理開始

        } else {
            // 通常のテトリミノの場合、ボードに固定
            for (Point block : currentTetromino.getShape()) {
                if (block.y >= 0) {
                    board[block.y][block.x] = currentTetromino.getColor(); // ブロックの色を設定
                }
            }
            clearFullLines(); // ライン消去を実行
        }
    }

    // 完全に埋まったラインを消去するメソッド
    private void clearFullLines() {
        int linesCleared = 0; // 消去したライン数

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean fullLine = true; // ラインが完全に埋まっているかを判定
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == null) {
                    fullLine = false; // 空のマスがあれば完全ではない
                    break;
                }
            }
            if (fullLine) {
                linesCleared++; // 消去ラインをカウント
                for (int row = y; row > 0; row--) {
                    System.arraycopy(board[row - 1], 0, board[row], 0, BOARD_WIDTH); // 上のラインを下にコピー
                }
                board[0] = new Color[BOARD_WIDTH]; // 一番上のラインを空に
            }
        }

        // スコアを加算（ライン数に応じた倍率）
        switch (linesCleared) {
            case 1: score += 100; break;
            case 2: score += 300; break;
            case 3: score += 1200; break;
            case 4: score += 3000; break;
            case 5: score += 5000; break;
            default: break;
        }

        updateSpeed(); // ゲームスピードを更新
        repaint(); // 再描画
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // タイマーイベントごとに実行される処理
        if (currentTetromino != null) {
            int speed = fastDrop ? 75 : 500; // 高速落下時のスピード
            timer.setDelay(speed); // タイマーの間隔を調整

            if (canMove(0, 1)) {
                currentTetromino.move(0, 1); // テトリミノを下に移動
            } else {
                lockTetromino(); // テトリミノを固定
                spawnTetromino(); // 次のテトリミノを生成
            }
            repaint(); // 再描画
        }
    }

    // メインメソッド（ゲームのエントリポイント）
    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris"); // ゲームウィンドウを作成
        Tetris tetris = new Tetris(); // テトリスゲームを初期化
        frame.add(tetris); // パネルをフレームに追加
        frame.pack(); // フレームサイズを自動調整
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ウィンドウ閉鎖時の動作
        frame.setVisible(true); // フレームを表示
    }

    // テトリミノクラス（内部クラス）
    private static class Tetromino {
        private Point[] shape; // テトリミノの形状を表すブロック配列
        private Color color; // テトリミノの色
        private int x = BOARD_WIDTH / 2, y = 0; // 初期位置
        private boolean isBomb = false; // ボムであるかどうかのフラグ

        // コンストラクタ
        public Tetromino(Point[] shape, Color color, boolean isBomb) {
            this.shape = shape;
            this.color = color;
            this.isBomb = isBomb;
        }

        public boolean isBomb() {
            return isBomb; // ボムかどうかを返す
        }
        
        //テトリミノを生成する。probability変数に乱数をセットし、その値に応じて、ランダムなかたちのテトリミノを発生させる。
        public static Tetromino randomTetromino() {
            Random rand = new Random();
            double probability = rand.nextDouble();
            if (probability < 0.03) { // 3%の確率でボムを生成
                return new Tetromino(new Point[]{new Point(0, 0)}, Color.WHITE, true); // ボムは1マス
            } else if (probability < 0.10) {
                return new Tetromino(new Point[]{new Point(0, 0)}, Color.RED, false);//1マスのテトリミノ、赤色
            } else if (probability < 0.15) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0)}, Color.GREEN, false);//2マス直線のテトリミノ。緑色。
            } else if (probability < 0.20) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(0, 1)}, Color.BLUE, false);//3マスL型のテトリミノ。青色
            } else if (probability < 0.30) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0)}, Color.CYAN, false); // 3マス直線のテトリミノ。シアン。
            } else if (probability < 0.35) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(1, 1)}, Color.CYAN, false);// 4マスT型のテトリミノ。シアン。
            } else if (probability < 0.40) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0)}, Color.MAGENTA, false); // 4マス直線のテトリミノ。マジェンタ。
            } else if (probability < 0.45) {
                return new Tetromino(new Point[]{new Point(0, 1), new Point(0, 2), new Point(0, 3), new Point(1, 3)}, Color.YELLOW, false); // ４マスL字型のテトリミノ。右型。黄色
            } else if (probability < 0.50) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2)}, Color.RED, false); // ４マスL字型のテトリミノ。左型。赤色
            } else if (probability < 0.55) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2)}, Color.ORANGE, false); // ４マス佐渡島型のテトリミノ。左型オレンジ
            } else if (probability < 0.60) {
                return new Tetromino(new Point[]{new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(0, 2)}, Color.PINK, false); // ４マス佐渡島型のテトリミノ。右型オレンジ
            } else if (probability < 0.65) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(0, 2)}, Color.ORANGE, false); // 5マスP字型のテトリミノ、オレンジ
            } else if (probability < 0.70) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(0, 2), new Point(1, 2)}, Color.PINK, false); // 5マスq字型のテトリミノ、ピンク
            } else if (probability < 0.75) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2)}, Color.GRAY, false); // 5マスU字型のテトリミノ、灰色
            } else if (probability < 0.80) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2)}, Color.ORANGE, false); // 5マス佐渡島左型のテトリミノ、オレンジ
            } else if (probability < 0.85) {
                return new Tetromino(new Point[]{new Point(2, 0), new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2)}, Color.PINK, false); // 5マス佐渡島右型のテトリミノ、ピンク
            } else if (probability < 0.90) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 1), new Point(2, 2)}, Color.ORANGE, false); // 5マスL右型のテトリミノ、オレンジ
            } else if (probability < 0.95) {
                return new Tetromino(new Point[]{new Point(2, 0), new Point(2, 1), new Point(2, 2), new Point(1, 2), new Point(0, 2)}, Color.PINK, false); //  5マスL左型のテトリミノ、ピンク
            } else if (probability < 0.98) {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0), new Point(4, 0)}, Color.WHITE, false); // 5マスI型のテトリミノ、白色
            } else {
                return new Tetromino(new Point[]{new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1)}, Color.YELLOW, false); //4正方形型のテトリミノ、黄色
            }
        }
        public Point[] getShape() {
            Point[] blocks = new Point[shape.length];
            for (int i = 0; i < shape.length; i++) {
                blocks[i] = new Point(shape[i].x + x, shape[i].y + y); // 現在の位置に基づいてブロック座標を計算
            }
            return blocks;
        }

        public Color getColor() {
            return color; // テトリミノの色を返す
        }

        public void move(int dx, int dy) {
            x += dx; // X方向に移動
            y += dy; // Y方向に移動
        }

        public void rotateR() {
            // 右回転（90度回転）
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].x;
                shape[i].x = -shape[i].y;
                shape[i].y = temp;
            }
        }

        public void rotateL() {
            // 左回転（90度回転）
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].y;
                shape[i].y = -shape[i].x;
                shape[i].x = temp;
            }
        }

        public void rotateBack() {
            // 回転を元に戻す
            for (int i = 0; i < shape.length; i++) {
                int temp = shape[i].x;
                shape[i].x = shape[i].y;
                shape[i].y = -temp;
            }
        }
    }
}

