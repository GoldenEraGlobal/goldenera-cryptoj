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
import global.goldenera.cryptoj.serialization.state.miningwindow.impl.MiningWindowStateV1EncodingStrategy;
import global.goldenera.rlp.RLP;

public class MiningWindowStateEncoder {

	public static final MiningWindowStateEncoder INSTANCE = new MiningWindowStateEncoder();
	private final Map<MiningWindowStateVersion, MiningWindowStateEncodingStrategy> strategies =
			new EnumMap<>(MiningWindowStateVersion.class);

	private MiningWindowStateEncoder() {
		strategies.put(MiningWindowStateVersion.V1, new MiningWindowStateV1EncodingStrategy());
	}

	public Bytes encode(MiningWindowState state) {
		if (state == null || state.getVersion() == null) {
			throw new CryptoJFailedException("MiningWindowState and version cannot be null");
		}
		MiningWindowStateEncodingStrategy strategy = strategies.get(state.getVersion());
		if (strategy == null) {
			throw new CryptoJFailedException("Unsupported MiningWindowState version: " + state.getVersion());
		}
		return RLP.encode(out -> {
			out.startList();
			out.writeIntScalar(state.getVersion().getCode());
			strategy.encode(out, state);
			out.endList();
		});
	}
}
