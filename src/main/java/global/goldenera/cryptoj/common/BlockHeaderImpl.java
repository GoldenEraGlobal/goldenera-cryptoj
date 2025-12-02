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
package global.goldenera.cryptoj.common;

import java.math.BigInteger;
import java.time.Instant;

import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.Signature;
import global.goldenera.cryptoj.enums.BlockVersion;
import global.goldenera.cryptoj.utils.BlockHeaderUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class BlockHeaderImpl implements BlockHeader {

	// --- RLP Fields ---
	BlockVersion version;
	long height;
	Instant timestamp;
	Hash previousHash;
	Hash txRootHash;
	Hash stateRootHash;
	BigInteger difficulty;
	Address coinbase;
	long nonce;
	Signature signature;

	// --- Calculated Fields (Lazy Loaded) ---

	@Getter(lazy = true)
	Hash hash = BlockHeaderUtil.hash(this);

	@Getter(lazy = true)
	int size = BlockHeaderUtil.size(this);
}