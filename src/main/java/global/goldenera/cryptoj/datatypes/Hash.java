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

import global.goldenera.rlp.RLP;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.DelegatingBytes32;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Secure 32-byte hash (Keccak-256), used to
 * identify blocks, transactions, state roots etc.
 */
public class Hash extends DelegatingBytes32 {

	public static final Hash ZERO = new Hash(Bytes32.ZERO);
	public static final Hash EMPTY_LIST_HASH = Hash.hash(RLP.EMPTY_LIST);

	protected Hash(final Bytes32 bytes) {
		super(bytes);
		checkArgument(bytes != null, "Bytes cannot be null");
		checkArgument(bytes.size() == 32, "Hash must be exactly 32 bytes");
	}

	/**
	 * Create a Hash by calculating the Keccak-256 hash of the input bytes.
	 * This is the main hashing algorithm.
	 *
	 * @param value Data to hash (e.g. RLP-serialized block/tx).
	 * @return 32-byte Hash.
	 */
	public static Hash hash(final Bytes data) {
		checkArgument(data != null, "Bytes cannot be null");
		return Hash.wrap(org.web3j.crypto.Hash.sha3(data.toArray()));
	}

	/**
	 * Wrap an existing 32-byte array (e.g. from DB) into a Hash object.
	 * Does not perform hashing.
	 */
	public static Hash wrap(final Bytes32 bytes) {
		checkArgument(bytes != null, "Bytes cannot be null");
		checkArgument(bytes.size() == 32, "Hash must be exactly 32 bytes");
		if (bytes instanceof Hash) {
			return (Hash) bytes;
		}
		return new Hash(bytes);
	}

	public static Hash wrap(final byte[] bytes) {
		checkArgument(bytes != null, "Bytes cannot be null");
		checkArgument(bytes.length == 32, "Hash must be exactly 32 bytes");
		return wrap(Bytes32.wrap(bytes));
	}

	/**
	 * Create a Hash from a hexadecimal string (with or without "0x").
	 * Used for API, DTO and JSON deserialization.
	 *
	 * @param str Hex string.
	 * @return Hash.
	 */
	@JsonCreator
	public static Hash fromHexString(final String str) {
		checkArgument(str != null, "String cannot be null");
		return new Hash(Bytes32.fromHexStringStrict(str));
	}

	/**
	 * Return a shortened hex string for easier logging.
	 */
	public String toShortLogString() {
		final String hex = toHexString();
		return hex.substring(0, 6) + "..." + hex.substring(hex.length() - 4);
	}

	// --- Methods for 'Map' and 'Set' compatibility ---

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Hash)) {
			return false;
		}
		// Safer comparison than direct 'Bytes.equals()'
		Hash other = (Hash) obj;
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