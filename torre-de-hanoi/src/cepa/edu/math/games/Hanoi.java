package cepa.edu.math.games;

public class Hanoi {

    private int discs = 0;

    public Hanoi (int n) {
        this.discs = n;
        solve(discs, 1, 3, 2);
    }

    private void solve (int n, int source, int dest, int by) {
        if (n == 1) moveDisc(source,dest);
        else {
            solve(n - 1, source, by, dest);
            solve(1, source, dest, by);
            solve(n - 1, by, dest, source);
        }
    }

    private void moveDisc (int from, int to) {
        System.out.println("Mova o disco do pino " + from + " para o pino " + to + "." );
    }
}