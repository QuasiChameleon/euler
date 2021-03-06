package net.euler.problems;

import net.euler.utils.Primes;

import static net.euler.utils.MathUtils.sqrt;

/**
 * Pentagonal numbers are generated by the formula, Pn=n(3n−1)/2. The first ten pentagonal numbers are:
 * <p/>
 * 1, 5, 12, 22, 35, 51, 70, 92, 117, 145, ...
 *
 * It can be seen that P4 + P7 = 22 + 70 = 92 = P8. However, their difference, 70 − 22 = 48, is not pentagonal.
 *
 * Find the pair of pentagonal numbers, Pj and Pk, for which their sum and difference are pentagonal and D = |Pk − Pj| is minimised; what is the value of D?
 *
 * @author Kevin Crosby
 */
public class P044 {
  private static Primes primes = Primes.getInstance();

  private static int pentagonal(int n) {
    return n * (3 * n - 1) / 2;
  }

  private static boolean isPentagonal(int x) {
    int y = 24 * x + 1;
    return primes.isPerfectSquare(y) && sqrt(y) % 6 == 5;
  }

  private static int inversePentagonal(int x) {
    assert isPentagonal(x) : "Cannot take inverse of non-pentagonal number " + x;
    return (1 + Long.valueOf(sqrt(24 * x + 1)).intValue()) / 6;
  }

  public static void main(String[] args) {
    boolean loop = true;
    int d = 0;
    for (int k = 1; loop; k++) {
      int pk = pentagonal(k);
      for (int j = 1; j < k; j++) {
        int pj = pentagonal(j);
        if (isPentagonal(pk - pj) && isPentagonal(pk + pj)) {
          int sum = inversePentagonal(pk + pj);
          int diff = inversePentagonal(pk - pj);
          loop = false;
          System.out.println("P" + sum + " = P" + k + " + P" + j);
          System.out.println("P" + diff + " = P" + k + " - P" + j);
          d = pk - pj;
          break;
        }
      }
    }
    System.out.print("The pair of pentagonal numbers, Pj and Pk");
    System.out.println(" for which their sum and difference are pentagonal and D = |Pk − Pj| is minimised;");
    System.out.println("  the value of D is " + d);
  }
}
