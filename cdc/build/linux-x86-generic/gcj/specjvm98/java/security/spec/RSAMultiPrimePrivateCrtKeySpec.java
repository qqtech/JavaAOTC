/*
 * @(#)RSAMultiPrimePrivateCrtKeySpec.java	1.5 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security.spec;

import java.math.BigInteger;

/**
 * This class specifies an RSA multi-prime private key, as defined in the 
 * PKCS#1 v2.1, using the Chinese Remainder Theorem (CRT) information 
 * values for efficiency.
 *
 * @author Valerie Peng
 *
 * @version 1.5 03/01/23
 *
 * @see java.security.Key
 * @see java.security.KeyFactory
 * @see KeySpec
 * @see PKCS8EncodedKeySpec
 * @see RSAPrivateKeySpec
 * @see RSAPublicKeySpec
 * @see RSAOtherPrimeInfo
 *
 * @since 1.4
 */

public class RSAMultiPrimePrivateCrtKeySpec extends RSAPrivateKeySpec {

    private BigInteger modulus;
    private BigInteger publicExponent;
    private BigInteger privateExponent;
    private BigInteger primeP;
    private BigInteger primeQ;
    private BigInteger primeExponentP;
    private BigInteger primeExponentQ;
    private BigInteger crtCoefficient;
    private RSAOtherPrimeInfo otherPrimeInfo[];

   /**
    * Creates a new <code>RSAMultiPrimePrivateCrtKeySpec</code>
    * given the modulus, publicExponent, privateExponent,
    * primeP, primeQ, primeExponentP, primeExponentQ,
    * crtCoefficient, and otherPrimeInfo as defined in PKCS#1 v2.1.
    *
    * <p>Note that <code>otherPrimeInfo</code> is cloned when constructing
    * this object.
    *
    * @param modulus the modulus n.
    * @param publicExponent the public exponent e.
    * @param privateExponent the private exponent d.
    * @param primeP the prime factor p of n.
    * @param primeQ the prime factor q of n.
    * @param primeExponentP this is d mod (p-1).
    * @param primeExponentQ this is d mod (q-1).
    * @param crtCoefficient the Chinese Remainder Theorem
    * coefficient q-1 mod p.
    * @param otherPrimeInfo triplets of the rest of primes, null can be
    * specified if there are only two prime factors (p and q).
    * @exception NullPointerException if any of the parameters, i.e. 
    * <code>modulus</code>, 
    * <code>publicExponent</code>, <code>privateExponent</code>, 
    * <code>primeP</code>, <code>primeQ</code>, 
    * <code>primeExponentP</code>, <code>primeExponentQ</code>,
    * <code>crtCoefficient</code>, is null.
    * @exception IllegalArgumentException if an empty, i.e. 0-length,
    * <code>otherPrimeInfo</code> is specified.
    */
    public RSAMultiPrimePrivateCrtKeySpec(BigInteger modulus,
				BigInteger publicExponent,
				BigInteger privateExponent,
				BigInteger primeP,
				BigInteger primeQ,
				BigInteger primeExponentP,
				BigInteger primeExponentQ,
				BigInteger crtCoefficient,
				RSAOtherPrimeInfo[] otherPrimeInfo) {
	super(modulus, privateExponent);
	if (modulus == null) {
	    throw new NullPointerException("the modulus parameter must be " +
					    "non-null");
	}
	if (publicExponent == null) {
	    throw new NullPointerException("the publicExponent parameter " +
					    "must be non-null");
	}
	if (privateExponent == null) {
	    throw new NullPointerException("the privateExponent parameter " +
					    "must be non-null");
	}
	if (primeP == null) {
	    throw new NullPointerException("the primeP parameter " +
					    "must be non-null");
	}
	if (primeQ == null) {
	    throw new NullPointerException("the primeQ parameter " +
					    "must be non-null");
	}
	if (primeExponentP == null) {
	    throw new NullPointerException("the primeExponentP parameter " +
					    "must be non-null");
	}
	if (primeExponentQ == null) {
	    throw new NullPointerException("the primeExponentQ parameter " +
					    "must be non-null");
	}
	if (crtCoefficient == null) {
	    throw new NullPointerException("the crtCoefficient parameter " +
					    "must be non-null");
	} 
	this.publicExponent = publicExponent;
	this.primeP = primeP;
	this.primeQ = primeQ;
	this.primeExponentP = primeExponentP;
	this.primeExponentQ = primeExponentQ;
	this.crtCoefficient = crtCoefficient;
	if (otherPrimeInfo == null)  {
	    this.otherPrimeInfo = null;
	} else if (otherPrimeInfo.length == 0) {
	    throw new IllegalArgumentException("the otherPrimeInfo " +
						"parameter must not be empty");
	} else {
	    this.otherPrimeInfo = (RSAOtherPrimeInfo[])otherPrimeInfo.clone();
	}
    }

    /**
     * Returns the public exponent.
     *
     * @return the public exponent.
     */
    public BigInteger getPublicExponent() {
	return this.publicExponent;
    }

    /**
     * Returns the primeP.
     *
     * @return the primeP.
     */
    public BigInteger getPrimeP() {
	return this.primeP;
    }

    /**
     * Returns the primeQ.
     *
     * @return the primeQ.
     */
    public BigInteger getPrimeQ() {
	return this.primeQ;
    }

    /**
     * Returns the primeExponentP.
     *
     * @return the primeExponentP.
     */
    public BigInteger getPrimeExponentP() {
	return this.primeExponentP;
    }

    /**
     * Returns the primeExponentQ.
     *
     * @return the primeExponentQ.
     */
    public BigInteger getPrimeExponentQ() {
	return this.primeExponentQ;
    }

    /**
     * Returns the crtCoefficient.
     *
     * @return the crtCoefficient.
     */
    public BigInteger getCrtCoefficient() {
	return this.crtCoefficient;
    }
    
    /**
     * Returns a copy of the otherPrimeInfo or null if there are 
     * only two prime factors (p and q).
     *
     * @return the otherPrimeInfo.
     */
    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
	if (otherPrimeInfo == null) return null;
	return (RSAOtherPrimeInfo[]) otherPrimeInfo.clone();
    }
}
