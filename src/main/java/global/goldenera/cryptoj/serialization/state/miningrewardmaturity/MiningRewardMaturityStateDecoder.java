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
package global.goldenera.cryptoj.serialization.state.miningrewardmaturity;

import java.util.EnumMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.common.state.MiningRewardMaturityState;
import global.goldenera.cryptoj.enums.state.MiningRewardMaturityStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.state.miningrewardmaturity.impl.MiningRewardMaturityStateV1DecodingStrategy;
import global.goldenera.rlp.RLP;
import global.goldenera.rlp.RLPInput;

public class MiningRewardMaturityStateDecoder {

	public static final MiningRewardMaturityStateDecoder INSTANCE = new MiningRewardMaturityStateDecoder();
	private final Map<MiningRewardMaturityStateVersion, MiningRewardMaturityStateDecodingStrategy> strategies =
			new EnumMap<>(MiningRewardMaturityStateVersion.class);

	private MiningRewardMaturityStateDecoder() {
		strategies.put(MiningRewardMaturityStateVersion.V1, new MiningRewardMaturityStateV1DecodingStrategy());
	}

	public MiningRewardMaturityState decode(Bytes rlpBytes) {
		if (rlpBytes == null || rlpBytes.isEmpty()) {
			throw new CryptoJFailedException("Cannot decode empty MiningRewardMaturityState bytes");
		}
		RLP.validate(rlpBytes);
		RLPInput input = RLP.input(rlpBytes);
		int fields = input.enterList();
		if (fields != 2) {
			throw new CryptoJFailedException("MiningRewardMaturityState must have exactly two fields");
		}
		MiningRewardMaturityStateVersion version = MiningRewardMaturityStateVersion.fromCode(input.readIntScalar());
		MiningRewardMaturityStateDecodingStrategy strategy = strategies.get(version);
		if (strategy == null) {
			throw new CryptoJFailedException("Unsupported MiningRewardMaturityState version: " + version);
		}
		MiningRewardMaturityState state = strategy.decode(input);
		if (!input.isEndOfCurrentList()) {
			throw new CryptoJFailedException("MiningRewardMaturityState contains unexpected RLP fields");
		}
		input.leaveList();
		return state;
	}
}
