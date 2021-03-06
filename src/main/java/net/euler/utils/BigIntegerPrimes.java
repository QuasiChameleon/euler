package net.euler.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;


/**
 * Prime number generator (BigInteger) and related methods.
 */
public class BigIntegerPrimes implements Iterable<BigInteger> {
  private static BigIntegerPrimes instance = null;
  private List<BigInteger> primes;
  private static final BigInteger TWO = BigInteger.valueOf(2L);
  private static final BigInteger THREE = BigInteger.valueOf(3L);
  private static final BigInteger LIMIT = new BigInteger("3317044064679887385961981");

  private BigIntegerPrimes() {
    primes = LongStream.of(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L)
        .mapToObj(BigInteger::valueOf)
        .collect(Collectors.toList()); // required for isPrime
  }

  public static BigIntegerPrimes getInstance() {
    if (instance == null) {
      synchronized (BigIntegerPrimes.class) {
        if (instance == null) {
          instance = new BigIntegerPrimes();
        }
      }
    }
    return instance;
  }

  /**
   * Returns an iterator over a set of elements of type Integer.
   *
   * @return an Iterator.
   */
  @Override
  public Iterator<BigInteger> iterator() {
    return new BigIntegerPrimeIterator();
  }

  /**
   * Generate primes using a slightly improved Sieve of Eratosthenes
   *
   * @param limit Number to generate primes up to.
   */
  public void generate(final BigInteger limit) {
    assert limit.compareTo(ZERO) ==  1 : "Limit must be positive!";
    int noPrimes = primes.size();
    BigInteger maxPrime = noPrimes > 0 ? primes.get(noPrimes - 1) : ZERO;
    if (limit.compareTo(maxPrime) == -1) return; // i.e. if limit < maxPrime

    primes = Lists.newArrayList(TWO); // start over, for now
    BigInteger sieve = ZERO; // fill with false (inverted logic), for n >= 3;
    int bit = -1;
    for (BigInteger odd = THREE; odd.compareTo(limit) < 1; odd = odd.add(TWO)) {
      if (!sieve.testBit(++bit)) { // i.e. if is prime
        primes.add(odd);
        int setBit = bit;
        for (BigInteger multiple = odd.multiply(odd); multiple.compareTo(limit) < 1;
             multiple = multiple.add(TWO.multiply(odd))) {
          setBit += odd.intValue();
          sieve = sieve.setBit(setBit); // i.e. set to composite   // TODO: find formula or algorithm that does not depend on integer index
        }
      }
    }
  }

  public BigInteger get(final int index) {
    assert index >= 0 : "Index must be non-negative!";
    if (index >= primes.size()) {
      synchronized (this) {
        if (index >= primes.size()) {
          for (int i = primes.size(); i <= index; i++) {
            BigInteger nextOdd = primes.get(primes.size() - 1).add(ONE).or(ONE); // get next odd (works for number 2)
            while (!isPrime(nextOdd)) {
              nextOdd = nextOdd.add(TWO);
            }
            primes.add(nextOdd);
            //System.out.println("Generating prime #" + i + ": " + nextOdd);
          }
        }
      }
    }
    return primes.get(index);
  }

  private boolean contains(final BigInteger number) { // uses prime counting function for faster lookups
    assert number.compareTo(ZERO) ==  1 : "Number must be positive!";
    int maxIndex = primes.size() - 1;
    if (number.compareTo(primes.get(maxIndex)) == 1) return false;

    Double pi = 1.25506 * number.doubleValue() / Math.log(number.doubleValue()); // approximate number of primes less than or equal to number
    int index = pi.intValue() - 1;
    if (index > primes.size() - 1) index = maxIndex;
    BigInteger prime = primes.get(index);
    if (number.compareTo(prime) == 1) {
      while (number.compareTo(prime) == 1 && index < maxIndex) prime = primes.get(++index);
    } else if (number.compareTo(prime) == -1) {
      while (number.compareTo(prime) == -1 && index > 0) prime = primes.get(--index);
    }
    return number.equals(prime);
  }

  /**
   * Test if a number is prime using the Miller–Rabin Primality Test, which is guaranteed to correctly distinguish
   * composites and primes up to 3,317,044,064,679,887,385,961,981 using the first 13 prime numbers.
   *
   * @param n Number to be tested.
   * @return True only if prime.
   */
  public boolean isPrime(final BigInteger n) { // TODO:  add pseudoprime checks above LIMIT???
    if (n.compareTo(LIMIT) == 1) System.err.println("WARNING!  Primality check not guaranteed for number " + n);
    if (n.compareTo(THREE) != 1) return n.compareTo(ONE) == 1;
    if (n.mod(TWO).equals(ZERO)) return false;
    if (n.compareTo(primes.get(primes.size() - 1)) != 1) return contains(n); // gt == 1, le != 1
    BigInteger d = n.subtract(ONE);
    int s = 0;
    while (d.mod(TWO).equals(ZERO)) {
      d = d.shiftRight(1);
      s++;
    }
    for (int i = 0; i < 9; i++) {
      BigInteger a = get(i);
      if (a.modPow(d, n).compareTo(ONE) != 0) {
        boolean composite = true;
        BigInteger p = ONE; // p = 2^r
        for (long r = 0; r < s; r++) {
          if (a.modPow(p.multiply(d), n).compareTo(n.subtract(ONE)) == 0) {
            composite = false;
            break; // inconclusive
          }
          p = p.shiftLeft(1);
        }
        if (composite) return false;
      }
    }
    return true;
  }

  public boolean isPerfectCube(final BigInteger number) {
    return isPerfectPowerOf(number, 3);
  }

  public boolean isPerfectSquare(final BigInteger number) {
    return isPerfectPowerOf(number, 2);
  }

  public boolean isPerfectPowerOf(final BigInteger number, final long degree) {
    return degree > 1 && degree(number) % degree == 0;
  }

  public boolean isPerfectPower(final BigInteger number) {
    return degree(number) > 1;
  }

  public Long degree(final BigInteger power) {
    assert power.compareTo(ZERO) ==  1 : "Number must be positive!";
    List<BigInteger> factors = factor(power);
    long degree = 0;
    for (BigInteger factor : Sets.newHashSet(factors)) {
      int exponent = Collections.frequency(factors, factor);
      degree = MathUtils.gcd(degree, exponent);
    }
    return degree;
  }

  public List<BigInteger> factor(BigInteger number) {
    List<BigInteger> factors = Lists.newArrayList();
    if (number.compareTo(TWO) == -1) {
      return factors;
    }

    int i = 0;
    BigInteger prime;
    BigInteger root = MathUtils.sqrt(number);
    do {
      prime = get(i++);
      while (number.mod(prime).equals(ZERO)) {
        number = number.divide(prime);
        factors.add(prime);
      }
    } while (prime.compareTo(root) != 1); // i.e. if prime <= sqrt(number)
    if (!number.equals(ONE)) {
      factors.add(number);
    }

    return factors;
  }

  public List<BigInteger> divisors(final BigInteger number) {
    // factor number
    List<BigInteger> factors = factor(number);
    if (factors.isEmpty()) {
      return number.compareTo(ONE) == 0 ? Lists.newArrayList(ONE) : Lists.<BigInteger>newArrayList();
    }

    // construct lists of powers
    List<List<BigInteger>> lists = Lists.newArrayList();
    for (BigInteger factor : Sets.newHashSet(factors)) {
      List<BigInteger> powers = Lists.newArrayList();
      for (int exponent = 1; exponent <= Collections.frequency(factors, factor); exponent++) {
        powers.add(factor.pow(exponent));
      }
      lists.add(powers);
    }

    // take Kronecker product of lists of powers
    List<BigInteger> divisors = Lists.newArrayList(ONE);
    for (List<BigInteger> powers : lists) {
      List<BigInteger> products = Lists.newArrayList();
      for (BigInteger power : powers) {
        for (BigInteger divisor : divisors) {
          products.add(power.multiply(divisor));
        }
      }
      divisors.addAll(products);
    }
    Collections.sort(divisors);
    return divisors;
  }

  public BigInteger countDivisors(final BigInteger number) { // Highly composite number formula
    if (number.compareTo(ONE) == -1) {
      return ZERO;
    }
    BigInteger count = ONE;
    List<BigInteger> factors = factor(number);
    for (BigInteger factor : Sets.newHashSet(factors)) {
      int exponent = Collections.frequency(factors, factor);
      count = count.multiply(BigInteger.valueOf(exponent + 1));
    }
    return count;
  }

  public BigInteger sumDivisors(final BigInteger number) {
    if (number.compareTo(ONE) == -1) {
      return ZERO;
    }
    BigInteger sum = ONE;
    List<BigInteger> factors = factor(number);
    for (BigInteger factor : Sets.newHashSet(factors)) {
      int exponent = Collections.frequency(factors, factor);
      BigInteger numerator = factor.pow(exponent + 1).subtract(ONE);
      BigInteger denominator = factor.subtract(ONE);
      sum = sum.multiply(numerator).divide(denominator);
    }
    return sum;
  }

  public BigInteger aliquotSum(final BigInteger number) { // a.k.a. sum proper divisors
    return sumDivisors(number).subtract(number);  // make it proper
  }


  /**
   * Euler's totient or phi function, φ(n), is an arithmetic function that counts the totatives of n, that is, the
   * positive integers less than or equal to n that are relatively prime to n. Thus, if n is a positive integer, then
   * φ(n) is the number of integers k in the range 1 ≤ k ≤ n for which the greatest common divisor gcd(n, k) = 1.
   *
   * @param number Positive whole number to take totient of.
   * @return Euler's totient.
   */
  public BigInteger totient(final BigInteger number) {
    assert number.compareTo(ZERO) ==  1 : "Number must be positive!";
    List<BigInteger> factors = factor(number);
    BigInteger phi = number;
    for (BigInteger factor : Sets.newHashSet(factors)) {
      phi = phi.divide(factor).multiply(factor.subtract(ONE));
    }
    return phi;
  }

  private class BigIntegerPrimeIterator implements Iterator<BigInteger> {
    private int index;

    public BigIntegerPrimeIterator() {
      index = 0;
    }

    public boolean hasNext() {
      return true;
    } // always has a next prime

    public BigInteger next() {
      return BigIntegerPrimes.this.get(index++);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static void main(String[] args) {
    {
      BigIntegerPrimes primes = BigIntegerPrimes.getInstance();
      for (String string : Lists.newArrayList("105", "10053", "1005415", "10054033243", "10054033321154111121")) {
        BigInteger number = new BigInteger(string);
        System.out.println(number + " is " + (primes.isPrime(number) ? "prime!" : "composite!"));
      }
      for (String string : Lists.newArrayList("997", "40487", "53471161", "1645333507", "188748146801", "99194853094755497")) {
        BigInteger number = new BigInteger(string);
        System.out.println(number + " is " + (primes.isPrime(number) ? "prime!" : "composite!"));
      }
    }

    for (int limit : Lists.newArrayList(10, 20, 30, 15)) {
      BigIntegerPrimes primes = BigIntegerPrimes.getInstance();
      int i = 1;
      for (BigInteger prime : primes) {
        System.out.print(prime + " ");
        if (i++ == limit) {
          System.out.println();
          break;
        }
      }
    }

    BigIntegerPrimes primes = BigIntegerPrimes.getInstance();
    System.out.println("100th prime is " + primes.get(100));

    for (int n : Lists.newArrayList(1003, 1009)) { // false, true
      System.out.println("Is " + n + " prime? " + primes.isPrime(BigInteger.valueOf(n)));
    }
  }
}
