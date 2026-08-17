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
import global.goldenera.cryptoj.serialization.state.miningrewardmaturity.impl.MiningRewardMaturityStateV1EncodingStrategy;
import global.goldenera.rlp.RLP;

public class MiningRewardMaturityStateEncoder {

	public static final MiningRewardMaturityStateEncoder INSTANCE = new MiningRewardMaturityStateEncoder();
	private final Map<MiningRewardMaturityStateVersion, MiningRewardMaturityStateEncodingStrategy> strategies =
			new EnumMap<>(MiningRewardMaturityStateVersion.class);

	private MiningRewardMaturityStateEncoder() {
		strategies.put(MiningRewardMaturityStateVersion.V1, new MiningRewardMaturityStateV1EncodingStrategy());
	}

	public Bytes encode(MiningRewardMaturityState state) {
		if (state == null || state.getVersion() == null) {
			throw new CryptoJFailedException("MiningRewardMaturityState and version cannot be null");
		}
		MiningRewardMaturityStateEncodingStrategy strategy = strategies.get(state.getVersion());
		if (strategy == null) {
			throw new CryptoJFailedException("Unsupported MiningRewardMaturityState version: " + state.getVersion());
		}
		return RLP.encode(out -> {
			out.startList();
			out.writeIntScalar(state.getVersion().getCode());
			strategy.encode(out, state);
			out.endList();
		});
	}
}
