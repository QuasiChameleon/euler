package net.euler.problems;

import net.euler.utils.MathUtils;

/**
 * 2520 is the smallest number that can be divided by each of the numbers from 1 to 10 without any remainder.
 *
 * What is the smallest positive number that is evenly divisible by all of the numbers from 1 to 20?
 *
 * @author Kevin Crosby
 */
public class P005 {
  public static void main(String[] args) {
    long lcm = MathUtils.lcm(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

    System.out.println("The smallest positive number that is evenly divisible by all of the numbers from 1 to 20 is " + lcm);
  }
}
