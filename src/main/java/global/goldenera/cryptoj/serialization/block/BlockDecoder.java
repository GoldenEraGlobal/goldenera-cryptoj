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
package global.goldenera.cryptoj.serialization.block;

import java.util.EnumMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.common.Block;
import global.goldenera.cryptoj.common.BlockHeader;
import global.goldenera.cryptoj.enums.BlockVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.block.impl.decoding.BlockV1DecodingStrategy;
import global.goldenera.cryptoj.serialization.blockheader.BlockHeaderDecoder;
import global.goldenera.rlp.RLP;
import global.goldenera.rlp.RLPInput;

public class BlockDecoder {

	public static final BlockDecoder INSTANCE = new BlockDecoder();
	private final Map<BlockVersion, BlockDecodingStrategy> strategies = new EnumMap<>(BlockVersion.class);

	private BlockDecoder() {
		strategies.put(BlockVersion.V1, new BlockV1DecodingStrategy());
	}

	public Block decode(Bytes rlpBytes, boolean excludeTxs) {
		if (rlpBytes == null || rlpBytes.isEmpty()) {
			throw new CryptoJFailedException("Cannot decode empty bytes");
		}
		RLP.validate(rlpBytes);
		RLPInput input = RLP.input(rlpBytes);
		input.enterList();
		Bytes headerBytes = input.readRaw();
		BlockHeader header = BlockHeaderDecoder.INSTANCE.decode(headerBytes);
		BlockVersion version = header.getVersion();
		BlockDecodingStrategy strategy = strategies.get(version);
		if (strategy == null)
			throw new CryptoJFailedException("Unknown block version");
		Block block = strategy.decodeBody(input, header, excludeTxs);
		input.leaveList();
		return block;
	}

	public Block decode(Bytes rlpBytes) {
		return decode(rlpBytes, false);
	}
}