package jcli.core;

public class Main {

    public int maxProfit(int[] prices) {
        int maxProfit = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        int n = prices.length;
        for (int i = 0; i < n; i++) {
            min = Math.min(min, prices[i]);
            max = Math.max(max, prices[n - 1 - i]);
            maxProfit = Math.max(maxProfit,  (max - min));
        }
        return maxProfit;
    }

    public static void main(String[] args) {
        new Main().maxProfit(new int[] {3, 2, 6, 5, 0, 3});
    }
}
