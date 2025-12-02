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

import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.rlp.RLP;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.DelegatingBytes;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Secure 20-byte address identifier (token, account, etc.)
 */
public class Address extends DelegatingBytes {

	/** Fixed address length in bytes. */
	public static final int SIZE = 20;

	/** Zero address (0x00...00). */
	public static final Address ZERO = new Address(Bytes.wrap(new byte[SIZE]));

	/** Native token address (0x00...00). */
	public static final Address NATIVE_TOKEN = ZERO;

	protected Address(final Bytes bytes) {
		super(bytes);
		checkArgument(bytes != null, "Bytes cannot be null");
		checkArgument(bytes.size() == SIZE, "Address must be %s bytes, but got %s", SIZE, bytes.size());
	}

	/**
	 * Wrap a 20-byte array into an Address object.
	 * Enforces exactly 20 bytes.
	 *
	 * @param value 20-byte array.
	 * @return Address object.
	 */
	public static Address wrap(final Bytes value) {
		checkArgument(value != null, "Bytes cannot be null");
		checkArgument(
				value.size() == SIZE,
				"Address must be %s bytes, but got %s",
				SIZE,
				value.size());
		if (value instanceof Address) {
			return (Address) value;
		}
		// Create a copy to make the object immutable
		return new Address(value.copy());
	}

	public static Address wrap(final byte[] value) {
		checkArgument(value != null, "Bytes cannot be null");
		checkArgument(value.length == SIZE, "Address must be %s bytes, but got %s", SIZE, value.length);
		return wrap(Bytes.wrap(value));
	}

	/**
	 * Create an Address from a public key.
	 * 
	 * @param publicKey Public key.
	 * @return Address.
	 */
	public static Address fromPublicKey(BigInteger publicKey) {
		String hexAddress = Keys.getAddress(publicKey);
		return Address.wrap(Numeric.hexStringToByteArray(hexAddress));
	}

	/**
	 * Get the 20-byte address from a key pair.
	 * 
	 * @param privateKey Private key.
	 * @return Address.
	 */
	public static Address fromKeyPair(PrivateKey privateKey) {
		return privateKey.getAddress();
	}

	/**
	 * Convert a "0x..." string back to a 20-byte address.
	 * 
	 * @param str Hex string.
	 * @return Address.
	 */
	public static Address fromHexString(final String str) {
		if (str == null)
			return null;
		if (!isValid(str)) {
			throw new CryptoJFailedException("Invalid address format: " + str);
		}
		return wrap(Bytes.fromHexStringLenient(str, SIZE));
	}

	/**
	 * Extract the last 20 bytes from a 32-byte Keccak-256 hash.
	 * This is the standard Ethereum method to convert a hash to an address.
	 *
	 * @param hash 32-byte Keccak-256 hash.
	 * @return 20-byte Address.
	 */
	public static Address extract(final Hash hash) {
		checkArgument(hash != null, "Hash cannot be null");
		return wrap(hash.slice(12, 20));
	}

	/**
	 * Calculate a unique and deterministic token address
	 * based on the sender address and its nonce.
	 *
	 * @param senderAddress Address (20b) of the creator.
	 * @param nonce         Transaction nonce that the CREATE creates.
	 * @return New 20-byte address for this token.
	 */
	public static Address generateTokenAddress(final Address senderAddress, final long nonce) {
		checkArgument(senderAddress != null, "Sender address cannot be null");
		checkArgument(nonce >= 0, "Nonce must be non-negative");
		Bytes rlpData = RLP.encode(
				out -> {
					out.startList();
					out.writeBytes(senderAddress);
					out.writeLongScalar(nonce);
					out.endList();
				});

		Hash hash = Hash.hash(rlpData);

		// 3. Extract address from hash
		return Address.extract(hash);
	}

	/**
	 * Check if a string is a valid Ethereum-style address (42 characters, 0x,
	 * EIP-55).
	 * 
	 * @param address String to check.
	 * @return True if the string is a valid Ethereum-style address, false
	 *         otherwise.
	 */
	public static boolean isValid(String address) {
		if (address == null || !address.matches("^(0x)?[0-9a-fA-F]{40}$")) {
			return false;
		}
		String addressNoPrefix = Numeric.cleanHexPrefix(address);
		if (addressNoPrefix.equals(addressNoPrefix.toLowerCase())
				|| addressNoPrefix.equals(addressNoPrefix.toUpperCase())) {
			return true;
		}
		try {
			String checksummed = Keys.toChecksumAddress(address);
			return address.equals(checksummed);
		} catch (Exception e) {
			return false;
		}
	}

	public static void validate(String address) {
		if (!isValid(address)) {
			throw new CryptoJFailedException("Invalid address format: " + address);
		}
	}

	/**
	 * Return an EIP-55 checksummed hex address (e.g. "0xAb58...").
	 * Recommended for display in API.
	 */
	public String toChecksumAddress() {
		return Keys.toChecksumAddress(toHexString());
	}

	// --- Methods for 'Map' and 'Set' compatibility ---

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Address)) {
			return false;
		}
		// Safer comparison than direct 'Bytes.equals()'
		Address other = (Address) obj;
		return Arrays.equals(this.toArrayUnsafe(), other.toArrayUnsafe());
	}

	@Override
	public int hashCode() {
		// Inherited from 'AbstractBytes', which correctly hashes the content
		return super.hashCode();
	}

	@Override
	public BigInteger toBigInteger() {
		return new BigInteger(1, this.toArrayUnsafe());
	}
}