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

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.DelegatingBytes32;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import global.goldenera.cryptoj.exceptions.CryptoJException;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Secure 32-byte wrapper for the secp256k1 private key.
 * Enforces that the key is in the valid range (not 0, < N).
 */
public class PrivateKey extends DelegatingBytes32 {

	public static final int SIZE = 32;

	// Standard BIP-44 derivation path (m/44'/60'/0'/0/0)
	private static final int[] DERIVATION_PATH = {
			44 | Bip32ECKeyPair.HARDENED_BIT,
			60 | Bip32ECKeyPair.HARDENED_BIT,
			0 | Bip32ECKeyPair.HARDENED_BIT,
			0,
			0
	};

	protected PrivateKey(Bytes32 bytes) {
		super(bytes);
		checkArgument(bytes != null, "Bytes cannot be null");
		checkArgument(bytes.size() == SIZE, "Private key must be exactly %s bytes, got %s", SIZE, bytes.size());
	}

	/**
	 * Wrap a 32-byte array into a PrivateKey object.
	 * Performs validation of the key.
	 *
	 * @param value 32-byte array.
	 * @return PrivateKey.
	 * @throws IllegalArgumentException if the key is invalid.
	 */
	public static PrivateKey wrap(final Bytes value) {
		checkArgument(value != null, "Bytes cannot be null");
		checkArgument(value.size() == SIZE, "Private key must be exactly %s bytes, got %s", SIZE, value.size());

		BigInteger d = new BigInteger(1, value.toArray());
		if (d.signum() <= 0 || d.compareTo(Sign.CURVE_PARAMS.getN()) >= 0) {
			throw new CryptoJFailedException("Private key value out of valid secp256k1 range");
		}

		if (value instanceof PrivateKey) {
			return (PrivateKey) value;
		}
		return new PrivateKey(Bytes32.wrap(value.copy()));
	}

	/**
	 * Wrap a 32-byte array into a PrivateKey object.
	 * Performs validation of the key.
	 *
	 * @param value 32-byte array.
	 * @return PrivateKey.
	 * @throws IllegalArgumentException if the key is invalid.
	 */
	public static PrivateKey wrap(final byte[] value) {
		checkArgument(value != null, "Bytes cannot be null");
		checkArgument(value.length == SIZE, "Private key must be exactly %s bytes, got %s", SIZE, value.length);
		return wrap(Bytes.wrap(value));
	}

	/**
	 * Create a new random key pair (secp256k1).
	 */
	public static PrivateKey create() throws CryptoJException {
		try {
			ECKeyPair keyPair = Keys.createEcKeyPair();
			byte[] privateKeyBytes = Numeric.toBytesPadded(
					keyPair.getPrivateKey(),
					PrivateKey.SIZE);
			return PrivateKey.wrap(privateKeyBytes);
		} catch (Exception e) {
			throw new CryptoJException("Failed to create key pair", e);
		}
	}

	/**
	 * Generates a new, secure 12-word mnemonic code.
	 * 
	 * @return String mnemonic code (words separated by spaces).
	 */
	public static String generateMnemonic() {
		byte[] entropy = new byte[16];
		new SecureRandom().nextBytes(entropy);
		return MnemonicUtils.generateMnemonic(entropy);
	}

	/**
	 * Generates a wallet (and keys) from a mnemonic phrase and password
	 * for a specific account index (accountIndex).
	 *
	 * @param mnemonic     Mnemonic phrase
	 * @param password     Password (can be "")
	 * @param accountIndex Index of the account (0 for the first address, 1 for the
	 *                     second, etc.)
	 * @return Pair of keys for the given index.
	 * @throws CryptoJException
	 */
	public static PrivateKey load(String mnemonic, String password, int accountIndex)
			throws CryptoJException {
		try {
			// 1. Generate master key from seed
			Bip32ECKeyPair masterKeyPair = Bip32ECKeyPair.generateKeyPair(
					MnemonicUtils.generateSeed(mnemonic, password));

			// 2. Generate final path by adding the index
			int[] finalPath = Arrays.copyOf(DERIVATION_PATH, 5);
			finalPath[4] = accountIndex; // Add the index as the last element

			// 3. Derive key for this specific path
			Bip32ECKeyPair derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(
					masterKeyPair,
					finalPath);

			BigInteger privateKeyInt = derivedKeyPair.getPrivateKey();
			byte[] privateKeyBytes = Numeric.toBytesPadded(
					privateKeyInt,
					PrivateKey.SIZE);

			return PrivateKey.wrap(privateKeyBytes);
		} catch (Exception e) {
			throw new CryptoJException("Failed to derive key from mnemonic for index " + accountIndex, e);
		}
	}

	/**
	 * Helper method to call the first (default) address.
	 */
	public static PrivateKey load(String mnemonic, String password) throws CryptoJException {
		return load(mnemonic, password, 0);
	}

	// --- Utility Methods ---

	/**
	 * Calculate the corresponding public key (as BigInteger).
	 * 
	 * @return BigInteger representation of the public key.
	 */
	public BigInteger getPublicKey() {
		return Sign.publicKeyFromPrivate(this.toBigInteger());
	}

	/**
	 * Calculate the corresponding 20-byte address from the public key.
	 * 
	 * @return 20-byte Address.
	 */
	public Address getAddress() {
		String hexAddress = Keys.getAddress(this.getPublicKey());
		return Address.fromHexString(hexAddress);
	}

	/**
	 * Sign a 32-byte hash (already hashed data!).
	 *
	 * @param messageHash 32-byte hash to sign (e.g. Keccak-256 RLP transaction).
	 * @return 65-byte signature (r, s, v).
	 */
	public Signature sign(final Hash messageHash) {
		ECKeyPair keyPair = ECKeyPair.create(this.toBigInteger());
		Sign.SignatureData sigData = Sign.signMessage(messageHash.toArray(), keyPair, false);

		byte[] r = sigData.getR();
		byte[] s = sigData.getS();
		byte v = sigData.getV()[0];

		byte[] signatureBytes = new byte[Signature.SIZE];
		System.arraycopy(r, 0, signatureBytes, 0, 32);
		System.arraycopy(s, 0, signatureBytes, 32, 32);
		signatureBytes[Signature.SIZE - 1] = v;

		return Signature.wrap(signatureBytes);
	}

	// --- Methods for 'Map' and 'Set' compatibility ---

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PrivateKey)) {
			return false;
		}
		// Safer comparison than direct 'Bytes.equals()'
		PrivateKey other = (PrivateKey) obj;
		return Arrays.equals(this.toArrayUnsafe(), other.toArrayUnsafe());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public BigInteger toBigInteger() {
		return new BigInteger(1, this.toArrayUnsafe());
	}
}