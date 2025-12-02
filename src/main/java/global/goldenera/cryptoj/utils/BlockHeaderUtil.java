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
package global.goldenera.cryptoj.utils;

import org.apache.tuweni.bytes.Bytes;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.serialization.blockheader.BlockHeaderEncoder;
import global.goldenera.cryptoj.common.BlockHeader;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BlockHeaderUtil {

	/**
	 * Calculates the Mining Hash (PoW Hash).
	 * Used for RandomX mining and for signing the block (PoA).
	 * EXCLUDES the signature.
	 */
	public static Hash hashForSigning(BlockHeader header) {
		Bytes data = BlockHeaderEncoder.INSTANCE.encode(header, false);
		return Hash.hash(data);
	}

	/**
	 * Calculates the Canonical Block ID.
	 * Used for DB keys and previousHash linking.
	 * INCLUDES the signature (if present).
	 */
	public static Hash hash(BlockHeader header) {
		Bytes data = BlockHeaderEncoder.INSTANCE.encode(header, true);
		return Hash.hash(data);
	}

	/**
	 * Returns the raw input bytes for RandomX.
	 * EXCLUDES the signature.
	 */
	public static byte[] powInput(BlockHeader header) {
		Bytes data = BlockHeaderEncoder.INSTANCE.encode(header, false);
		return data.toArray();
	}

	/**
	 * Returns the size of the block header in bytes.
	 */
	public static int size(BlockHeader header) {
		Bytes data = BlockHeaderEncoder.INSTANCE.encode(header, true);
		return data.size();
	}
}