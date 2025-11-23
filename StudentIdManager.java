import java.io.*;
import java.util.*;

public class Main {

    static int R, C, M, K;
    // Grid: y=0 is Bottom, y=R-1 is Top (Ceiling)
    static char[][] grid;
    static Map<Character, Integer> colorPoints;
    static long totalBonusScore = 0;

    // Directions: Up, Down, Left, Right
    static int[] dRow = {1, -1, 0, 0};
    static int[] dCol = {0, 0, -1, 1};

    public static void main(String[] args) {
        // Use a custom Tokenizer that handles the input stream robustly
        MyScanner sc = new MyScanner(System.in);
        
        if (!sc.hasNext()) return;

        try {
            // 1. Parse Dimensions
            String rStr = sc.next();
            if (rStr == null) return;
            R = Integer.parseInt(rStr);
            C = Integer.parseInt(sc.next());
            M = Integer.parseInt(sc.next());

            // 2. Initialize Grid (Row 0 is Top in matrix, mapped to R-1 in Cartesian)
            grid = new char[R][C];
            for (int i = 0; i < R; i++) Arrays.fill(grid[i], '.');

            // 3. Read Grid Content (M rows)
            // We read exactly M*C characters from the stream
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < C; j++) {
                    String token = sc.nextCharToken(); 
                    // Map input row 'i' (Top) to grid row 'R - 1 - i' (Ceiling)
                    grid[R - 1 - i][j] = token.charAt(0);
                }
            }

            // 4. Read Colors and Points
            List<Character> colors = new ArrayList<>();
            List<Integer> points = new ArrayList<>();
            
            // Read tokens. Identify transition from Color strings to Integer points.
            while (true) {
                String token = sc.next();
                if (isNumeric(token)) {
                    // Found the first point value
                    points.add(Integer.parseInt(token));
                    break; // Exit color reading loop
                } else {
                    // It is a color string (e.g., "R" or "RGB")
                    for(char c : token.toCharArray()) colors.add(c);
                }
            }
            
            // We have 1 point. We need (colors.size()) total points.
            int needed = colors.size() - points.size();
            for(int i=0; i<needed; i++) {
                points.add(Integer.parseInt(sc.next()));
            }
            
            colorPoints = new HashMap<>();
            for(int i=0; i<colors.size(); i++) {
                if (i < points.size()) {
                    colorPoints.put(colors.get(i), points.get(i));
                }
            }

            // 5. Shooter Params
            int startCol = Integer.parseInt(sc.next());
            K = Integer.parseInt(sc.next());

            runGame(startCol);
            System.out.print(totalBonusScore);

        } catch (Exception e) {
            return;
        }
    }
    
    static boolean isNumeric(String s) {
        if(s == null || s.isEmpty()) return false;
        // Check if it starts with a digit or a minus sign followed by digit
        char c = s.charAt(0);
        return Character.isDigit(c) || (s.length() > 1 && c == '-');
    }

    static void runGame(int startCol) {
        int r = 0; // Start at Bottom Row (y=0)
        int c = startCol;
        
        // Initial Velocity: 45-deg Left-Up
        // Up (y increases), Left (x decreases)
        int dr = 1; 
        int dc = -1; 
        
        int bounces = 0;
        int steps = 0;
        int MAX_STEPS = 5000000; // Prevent infinite loops

        while (bounces < K && steps < MAX_STEPS) {
            steps++;
            if (isGridEmpty()) break;

            int nr = r + dr;
            int nc = c + dc;
            
            // --- 1. Wall Collision ---
            boolean hitWall = false;
            
            // Side Walls
            if (nc < 0 || nc >= C) {
                dc = -dc; // Reflect Horizontal
                // Recalculate next position immediately for this step
                nc = c + dc;
                hitWall = true;
            }
            // Top Wall (Ceiling R)
            if (nr >= R) {
                dr = -dr; // Reflect Vertical
                nr = r + dr;
                hitWall = true;
            }
            // Bottom Wall (Floor -1)
            if (nr < 0) {
                dr = -dr;
                nr = r + dr;
                hitWall = true;
            }
            
            // Safety: If reflected pos is still invalid (corners), skip move
            if (!isValid(nr, nc)) {
                // Trapped or corner case, just rotate and wait
                r = nr; c = nc; 
                continue;
            }
            
            // --- 2. Bubble Collision ---
            // Neighbors relative to current (r,c)
            boolean vObs = isBubble(nr, c); // Vertical neighbor
            boolean hObs = isBubble(r, nc); // Horizontal neighbor
            boolean dObs = isBubble(nr, nc); // Diagonal neighbor
            
            boolean collision = false;
            Set<Character> hitColors = new HashSet<>();
            List<int[]> burstList = new ArrayList<>();
            
            // Priority: Corner > Face > Diagonal
            if (vObs && hObs) {
                // Corner Hit
                collision = true;
                dr = -dr;
                dc = -dc;
                addHit(nr, c, hitColors, burstList);
                addHit(r, nc, hitColors, burstList);
            } else if (vObs) {
                // Vertical Hit
                collision = true;
                dr = -dr;
                addHit(nr, c, hitColors, burstList);
            } else if (hObs) {
                // Horizontal Hit
                collision = true;
                dc = -dc;
                addHit(r, nc, hitColors, burstList);
            } else if (dObs) {
                // Diagonal Hit
                collision = true;
                dr = -dr;
                dc = -dc;
                addHit(nr, nc, hitColors, burstList);
            }
            
            if (collision) {
                bounces++;
                
                // Phase A: Burst
                for (int[] pos : burstList) {
                    if (grid[pos[0]][pos[1]] != '.') {
                        burst(pos[0], pos[1], grid[pos[0]][pos[1]]);
                    }
                }
                
                // Phase B: Gravity
                applyGravity(hitColors);
                
            } else {
                // No collision, move
                r = nr;
                c = nc;
            }
        }
    }
    
    static void addHit(int r, int c, Set<Character> colors, List<int[]> list) {
        if (isValid(r, c) && grid[r][c] != '.') {
            colors.add(grid[r][c]);
            list.add(new int[]{r, c});
        }
    }
    
    static boolean isBubble(int r, int c) {
        return isValid(r, c) && grid[r][c] != '.';
    }
    
    static boolean isValid(int r, int c) {
        return r >= 0 && r < R && c >= 0 && c < C;
    }
    
    static void burst(int r, int c, char color) {
        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{r, c});
        grid[r][c] = '.';
        
        while (!q.isEmpty()) {
            int[] curr = q.poll();
            for (int i = 0; i < 4; i++) {
                int nr = curr[0] + dRow[i];
                int nc = curr[1] + dCol[i];
                if (isValid(nr, nc) && grid[nr][nc] == color) {
                    grid[nr][nc] = '.';
                    q.add(new int[]{nr, nc});
                }
            }
        }
    }
    
    static void applyGravity(Set<Character> hitColors) {
        boolean[][] supported = new boolean[R][C];
        Queue<int[]> q = new LinkedList<>();
        
        // Roots: Ceiling (R-1)
        for (int j = 0; j < C; j++) {
            if (grid[R-1][j] != '.') {
                supported[R-1][j] = true;
                q.add(new int[]{R-1, j});
            }
        }
        
        while (!q.isEmpty()) {
            int[] curr = q.poll();
            int r = curr[0];
            int c = curr[1];
            char color = grid[r][c];
            
            // Propagate Down (Vertical Support - Any Color)
            // If (r,c) is supported, (r-1, c) depends on it.
            int downR = r - 1;
            if (downR >= 0 && grid[downR][c] != '.' && !supported[downR][c]) {
                supported[downR][c] = true;
                q.add(new int[]{downR, c});
            }
            
            // Propagate Side (Horizontal Support - Same Color Only)
            int[] sides = {-1, 1};
            for (int d : sides) {
                int sideC = c + d;
                if (isValid(r, sideC) && grid[r][sideC] != '.' && !supported[r][sideC]) {
                    if (grid[r][sideC] == color) {
                        supported[r][sideC] = true;
                        q.add(new int[]{r, sideC});
                    }
                }
            }
        }
        
        // Remove Unsupported
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                if (grid[i][j] != '.' && !supported[i][j]) {
                    // Fall
                    if (!hitColors.contains(grid[i][j])) {
                        totalBonusScore += colorPoints.getOrDefault(grid[i][j], 0);
                    }
                    grid[i][j] = '.';
                }
            }
        }
    }
    
    static boolean isGridEmpty() {
        for (int i = 0; i < R; i++) for (int j = 0; j < C; j++) if (grid[i][j] != '.') return false;
        return true;
    }

    // Custom Scanner to handle tokens robustly
    static class MyScanner {
        BufferedReader br;
        StringTokenizer st;
        
        public MyScanner(InputStream in) {
            br = new BufferedReader(new InputStreamReader(in));
        }
        
        String next() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    String line = br.readLine();
                    if (line == null) return null;
                    if (line.trim().isEmpty()) continue;
                    st = new StringTokenizer(line);
                } catch (IOException e) { return null; }
            }
            return st.nextToken();
        }
        
        // Special char reader for the grid
        List<String> charBuffer = new ArrayList<>();
        String nextCharToken() {
            if (charBuffer.isEmpty()) {
                String s = next();
                if (s == null) return null;
                for (char c : s.toCharArray()) charBuffer.add(String.valueOf(c));
            }
            return charBuffer.remove(0);
        }
        
        boolean hasNext() {
            while (st == null || !st.hasMoreTokens()) {
                try {
                    String line = br.readLine();
                    if (line == null) return false;
                    if (line.trim().isEmpty()) continue;
                    st = new StringTokenizer(line);
                } catch (IOException e) { return false; }
            }
            return true;
        }
    }
}