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
package global.goldenera.cryptoj.serialization.state.miningwindow;

import java.util.EnumMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.common.state.MiningWindowState;
import global.goldenera.cryptoj.enums.state.MiningWindowStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.state.miningwindow.impl.MiningWindowStateV1DecodingStrategy;
import global.goldenera.rlp.RLP;
import global.goldenera.rlp.RLPInput;

public class MiningWindowStateDecoder {

	public static final MiningWindowStateDecoder INSTANCE = new MiningWindowStateDecoder();
	private final Map<MiningWindowStateVersion, MiningWindowStateDecodingStrategy> strategies =
			new EnumMap<>(MiningWindowStateVersion.class);

	private MiningWindowStateDecoder() {
		strategies.put(MiningWindowStateVersion.V1, new MiningWindowStateV1DecodingStrategy());
	}

	public MiningWindowState decode(Bytes rlpBytes) {
		if (rlpBytes == null || rlpBytes.isEmpty()) {
			throw new CryptoJFailedException("Cannot decode empty MiningWindowState bytes");
		}
		RLP.validate(rlpBytes);
		RLPInput input = RLP.input(rlpBytes);
		int fields = input.enterList();
		if (fields != 5) {
			throw new CryptoJFailedException("MiningWindowState must have exactly five fields");
		}
		MiningWindowStateVersion version = MiningWindowStateVersion.fromCode(input.readIntScalar());
		MiningWindowStateDecodingStrategy strategy = strategies.get(version);
		if (strategy == null) {
			throw new CryptoJFailedException("Unsupported MiningWindowState version: " + version);
		}
		MiningWindowState state = strategy.decode(input);
		input.leaveList();
		return state;
	}
}
