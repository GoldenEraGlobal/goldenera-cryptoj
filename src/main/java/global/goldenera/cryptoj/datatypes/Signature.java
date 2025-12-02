/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025-2030 The GoldenEraGlobal Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package global.goldenera.cryptoj.datatypes;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.DelegatingBytes;
import org.web3j.crypto.Sign;

import global.goldenera.cryptoj.exceptions.CryptoJException;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;

/**
 * Secure 65-byte wrapper for ECDSA signature (r, s, v).
 * Format: [32 bytes R] [32 bytes S] [1 byte V]
 */
public class Signature extends DelegatingBytes {

	public static final int SIZE = 65;
	private static final BigInteger HALF_CURVE_ORDER = Sign.CURVE_PARAMS.getN().shiftRight(1);
	private static final Bytes ZERO_BYTES = Bytes.wrap(new byte[SIZE]);
	public static final Signature ZERO = new Signature(ZERO_BYTES, true);

	protected Signature(final Bytes bytes, boolean skipValidation) {
		super(bytes);
		checkArgument(bytes != null, "Bytes cannot be null");
		checkArgument(bytes.size() == SIZE, "Signature must be exactly %s bytes, got %s", SIZE, bytes.size());
		if (!skipValidation && !isStructurallyValid()) {
			throw new CryptoJFailedException(
					"Signature is structurally invalid (v, r, s values are not in the valid range)");
		}
	}

	/**
	 * Wrap a 65-byte array into a Signature object.
	 * Enforces exactly 65 bytes.
	 *
	 * @param value 65-byte array.
	 * @return Signature object.
	 */
	public static Signature wrap(final Bytes value) {
		checkArgument(value != null, "Bytes cannot be null");
		checkArgument(value.size() == SIZE, "Signature must be exactly %s bytes, got %s", SIZE, value.size());

		if (value.equals(ZERO_BYTES)) {
			return ZERO;
		}

		if (value instanceof Signature) {
			return (Signature) value;
		}

		return new Signature(value.copy(), false);
	}

	/**
	 * Wrap a 65-byte array into a Signature object.
	 * Enforces exactly 65 bytes.
	 *
	 * @param value 65-byte array.
	 * @return Signature object.
	 */
	public static Signature wrap(final byte[] value) {
		checkArgument(value != null, "Bytes cannot be null");
		checkArgument(value.length == SIZE, "Signature must be exactly %s bytes, got %s", SIZE, value.length);
		return wrap(Bytes.wrap(value));
	}

	// --- Utility Methods ---

	/**
	 * Extract the 'r' component (first 32 bytes).
	 */
	public byte[] getR() {
		return this.slice(0, 32).toArray();
	}

	/**
	 * Extract the 's' component (middle 32 bytes).
	 */
	public byte[] getS() {
		return this.slice(32, 32).toArray();
	}

	/**
	 * Extract the 'v' component (last byte).
	 */
	public byte getV() {
		return this.get(64);
	}

	/**
	 * Convert this object to a 'SignatureData' object from the Web3j library,
	 * which is needed for 'Sign.signedMessageToKey'.
	 *
	 * @return Sign.SignatureData
	 */
	public Sign.SignatureData toSignatureData() {
		return new Sign.SignatureData(getV(), getR(), getS());
	}

	/**
	 * Recover the address that signed the given hash.
	 *
	 * @param messageHash 32-byte hash that was signed.
	 * @return 20-byte Address.
	 * @throws CryptoJException if the signature is invalid.
	 */
	public Address recoverAddress(Hash messageHash) throws CryptoJException {
		if (!this.isStructurallyValid()) {
			throw new CryptoJException("Signature is structurally invalid (v, r, s values");
		}

		try {
			BigInteger publicKey = Sign.signedMessageHashToKey(messageHash.toArray(), this.toSignatureData());
			return Address.fromPublicKey(publicKey);
		} catch (Exception e) {
			throw new CryptoJException("Signature validation failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Check if this signature corresponds to the hash and address.
	 *
	 * @param messageHash     32-byte hash that was signed.
	 * @param expectedAddress Address that we expect.
	 * @return true if the signature is valid and corresponds to the address.
	 */
	public boolean validate(Hash messageHash, Address expectedAddress) {
		try {
			Address recovered = recoverAddress(messageHash);
			return recovered.equals(expectedAddress);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * !!! THIS DOES NOT CHECK THE SIGNATURE AGAINST THE HASH !!!
	 * 
	 * Check if the signature is structurally valid.
	 * Checks if 'v' is correct (27/28) and if 'r' and 's' are
	 * in the valid range (including "low-S" requirement of Ethereum).
	 *
	 * !!! THIS DOES NOT CHECK THE SIGNATURE AGAINST THE HASH !!!
	 *
	 * @return true if the signature is structurally valid.
	 */
	public boolean isStructurallyValid() {
		final byte v = getV();
		if (v != 27 && v != 28) {
			return false;
		}

		final BigInteger r = new BigInteger(1, getR());
		final BigInteger s = new BigInteger(1, getS());
		final BigInteger n = Sign.CURVE_PARAMS.getN();

		if (r.signum() <= 0 || r.compareTo(n) >= 0) {
			return false;
		}
		if (s.signum() <= 0 || s.compareTo(n) >= 0) {
			return false;
		}

		if (s.compareTo(HALF_CURVE_ORDER) > 0) {
			return false;
		}

		return true;
	}

	// --- Methods for 'Map' and 'Set' compatibility ---

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Signature)) {
			return false;
		}
		// Safer comparison than direct 'Bytes.equals()'
		Signature other = (Signature) obj;
		return Arrays.equals(this.toArrayUnsafe(), other.toArrayUnsafe());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}