package net.euler.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.euler.utils.MathUtils.*;

/**
 * Prime number generator (64 bit) and related methods.
 */
public class Primes implements Iterable<Long> {
  private static Primes instance = null;
  private List<Long> primes;

  private Primes() {
    primes = Lists.newArrayList(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L); // required for isPrime
  }

  public static Primes getInstance() {
    if (instance == null) {
      synchronized (Primes.class) {
        if (instance == null) {
          instance = new Primes();
        }
      }
    }
    return instance;
  }

  /**
   * Returns an iterator over a set of elements of type Long.
   *
   * @return an Iterator.
   */
  @Override
  public Iterator<Long> iterator() {
    return new PrimeIterator();
  }

  /**
   * Generate primes using a slightly improved Sieve of Eratosthenes
   *
   * @param limit Number to generate primes up to.
   */
  public void generate(final long limit) {
    assert limit > 0 : "Limit must be positive!";
    int noPrimes = primes.size();
    long maxPrime = noPrimes > 0 ? primes.get(noPrimes - 1) : 0L;
    if (limit < maxPrime) return;

    primes = Lists.newArrayList(2L); // start over, for now
    BitSet sieve = new BitSet(); // fill with false (inverted logic), for n >= 3;
    long prime = 3;
    for (int bit = sieve.nextClearBit(0); bit >= 0 && prime <= limit / prime;
         bit = sieve.nextClearBit(bit + 1), prime = (bit << 1) + 3) {
      primes.add(prime);
      int setBit = bit;
      for (long multiple = prime * prime; multiple <= limit; multiple += 2L * prime) {
        setBit += Long.valueOf(prime).intValue();
        sieve.set(setBit); // set to composite  // TODO: find formula or algorithm that does not depend on integer index
      }
    }
  }

  public long get(final int index) {
    assert index >= 0 : "Index must be non-negative!";
    if (index >= primes.size()) {
      synchronized (this) {
        if (index >= primes.size()) {
          for (int i = primes.size(); i <= index; i++) { // TODO:  replace nextOdd with primorial function?
            long nextOdd = (primes.get(primes.size() - 1) + 1L) | 1L; // get next odd (works for number 2)
            while (!isPrime(nextOdd)) {
              nextOdd += 2;
            }
            primes.add(nextOdd);
            //System.out.println("Generating prime #" + i + ": " + nextOdd);
          }
        }
      }
    }
    return primes.get(index);
  }

  private boolean contains(final long number) { // uses prime counting function for faster lookups
    assert number > 0 : "Number must be positive!";
    int maxIndex = primes.size() - 1;
    if (number > primes.get(maxIndex)) return false;

    Double pi = 1.25506 * number / Math.log(number); // approximate number of primes less than or equal to number
    int index = pi.intValue() - 1;
    if (index > primes.size() - 1) index = maxIndex;
    long prime = primes.get(index);
    if (number > prime) {
      while (number > prime && index < maxIndex) prime = primes.get(++index);
    } else if (number < prime) {
      while (number < prime && index > 0) prime = primes.get(--index);
    }
    return number == prime;
  }

  /**
   * Test if a number is prime using the Miller–Rabin Primality Test, which is guaranteed to correctly distinguish
   * composites and primes up to 3,317,044,064,679,887,385,961,981 using the first 13 prime numbers.
   *
   * @param n Number to be tested.
   * @return True only if prime.
   */
  public boolean isPrime(final long n) {
    if (n <= 3) return n > 1;
    if (n % 2 == 0) return false;
    if (n <= primes.get(primes.size() - 1)) return contains(n);
    long d = n - 1;
    int s = 0;
    while (d % 2 == 0) {
      d >>= 1;
      s++;
    }
    for (int i = 0; i < 9; i++) {
      long a = get(i);
      if (modPow(a, d, n) != 1) {
        boolean composite = true;
        for (long r = 0, p = 1; r < s; r++, p <<= 1) { // p = 2^r
          if (modPow(a, p * d, n) == n - 1) {
            composite = false;
            break; // inconclusive
          }
        }
        if (composite) return false;
      }
    }
    return true;
  }

  public boolean isPerfectCube(final long number) {
    return isPerfectPowerOf(number, 3);
  }

  public boolean isPerfectSquare(final long number) {
    return isPerfectPowerOf(number, 2);
  }

  public boolean isPerfectPowerOf(final long number, final long degree) {
    return degree > 1 && degree(number) % degree == 0;
  }

  public boolean isPerfectPower(final long number) {
    return degree(number) > 1;
  }

  public long degree(final long power) {
    assert power > 0 : "Number must be positive!";
    List<Long> factors = factor(power);
    long degree = 0;
    for (long factor : Sets.newHashSet(factors)) {
      int exponent = Collections.frequency(factors, factor);
      degree = gcd(degree, exponent);
    }
    return degree;
  }

  public List<Long> factor(long number) {
    List<Long> factors = Lists.newArrayList();
    if (number < 2L) {
      return factors;
    }

    int i = 0;
    long prime;
    long root = sqrt(number);
    boolean updated;
    do {
      updated = false;
      prime = get(i++);
      while (number % prime == 0) {
        number /= prime;
        factors.add(prime);
        updated = true;
      }
      if (updated) {
        root = sqrt(number);
      }
    } while (prime <= root);
    if (number > 1L) {
      factors.add(number);
    }

    return factors;
  }

  public List<Long> divisors(final long number) {
    // factor number
    List<Long> factors = factor(number);
    if (factors.isEmpty()) {
      return number == 1 ? Lists.newArrayList(1L) : Lists.<Long>newArrayList();
    }

    // construct lists of powers
    List<List<Long>> lists = Lists.newArrayList();
    for (long factor : Sets.newHashSet(factors)) {
      List<Long> powers = Lists.newArrayList();
      for (int exponent = 1; exponent <= Collections.frequency(factors, factor); exponent++) {
        powers.add(pow(factor, exponent));
      }
      lists.add(powers);
    }

    // take Kronecker product of lists of powers
    List<Long> divisors = Lists.newArrayList(1L);
    for (List<Long> powers : lists) {
      List<Long> products = Lists.newArrayList();
      for (long power : powers) {
        for (long divisor : divisors) {
          products.add(power * divisor);
        }
      }
      divisors.addAll(products);
    }
    Collections.sort(divisors);
    return divisors;
  }

  public long countDivisors(final long number) { // Highly composite number formula
    if (number < 1L) {
      return 0L;
    }
    long count = 1;
    List<Long> factors = factor(number);
    for (long factor : Sets.newHashSet(factors)) {
      int exponent = Collections.frequency(factors, factor);
      count *= exponent + 1;
    }
    return count;
  }

  public long sumDivisors(final long number) {
    if (number < 1L) {
      return 0L;
    }
    long sum = 1;
    List<Long> factors = factor(number);
    for (long factor : Sets.newHashSet(factors)) {
      int exponent = Collections.frequency(factors, factor);
      long numerator = pow(factor, exponent + 1) - 1;
      long denominator = factor - 1;
      sum *= numerator / denominator;
    }
    return sum;
  }

  public long aliquotSum(final long number) { // a.k.a. sum proper divisors
    return sumDivisors(number) - number; // make it proper
  }

  /**
   * Euler's totient or phi function, φ(n), is an arithmetic function that counts the totatives of n, that is, the
   * positive integers less than or equal to n that are relatively prime to n. Thus, if n is a positive integer, then
   * φ(n) is the number of integers k in the range 1 ≤ k ≤ n for which the greatest common divisor gcd(n, k) = 1.
   *
   * @param number Positive whole number to take totient of.
   * @return Euler's totient.
   */
  public long totient(final long number) {
    assert number > 0 : "Number must be positive!";
    List<Long> factors = factor(number);
    long phi = number;
    for (long factor : Sets.newHashSet(factors)) {
      phi = phi / factor * (factor - 1);
    }
    return phi;
  }


  private class PrimeIterator implements Iterator<Long> {
    private int index;

    public PrimeIterator() {
      index = 0;
    }

    public boolean hasNext() {
      return true;
    } // always has a next prime

    public Long next() {
      return Primes.this.get(index++);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static void main(String[] args) {
    {
      Primes primes = Primes.getInstance();
      for (long number : Lists.newArrayList(105L, 10053L, 1005415L, 10054033243L)) {
        System.out.println(number + " is " + (primes.isPrime(number) ? "prime!" : "composite!"));
      }
      for (long number : Lists.newArrayList(997L, 40487L, 53471161L, 1645333507L, 188748146801L)) {
        System.out.println(number + " is " + (primes.isPrime(number) ? "prime!" : "composite!"));
      }
    }

    for (int limit : Lists.newArrayList(10, 20, 30, 15)) {
      Primes primes = Primes.getInstance();
      int i = 1;
      for (long prime : primes) {
        System.out.print(prime + " ");
        if (i++ == limit) {
          System.out.println();
          break;
        }
      }
    }

    Primes primes = Primes.getInstance();
    System.out.println("100th prime is " + primes.get(100));

    for (int n : Lists.newArrayList(1003, 1009)) { // false, true
      System.out.println("Is " + n + " prime? " + primes.isPrime(n));
    }
  }
}
