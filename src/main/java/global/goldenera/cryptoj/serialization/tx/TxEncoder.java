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
package global.goldenera.cryptoj.serialization.tx;

import java.util.EnumMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.enums.TxVersion;
import global.goldenera.cryptoj.serialization.tx.impl.encoding.TxV1EncodingStrategy;
import global.goldenera.rlp.RLP;

public class TxEncoder {

	public static final TxEncoder INSTANCE = new TxEncoder();
	private final Map<TxVersion, TxEncodingStrategy> strategies = new EnumMap<>(TxVersion.class);

	private TxEncoder() {
		strategies.put(TxVersion.V1, new TxV1EncodingStrategy());
	}

	public Bytes encode(Tx tx, boolean includeSignature) {
		TxEncodingStrategy strategy = strategies.get(tx.getVersion());
		if (strategy == null) {
			throw new IllegalArgumentException("Unsupported Transaction Version: " + tx.getVersion());
		}
		return RLP.encode(out -> {
			out.startList();
			out.writeIntScalar(tx.getVersion().getCode());
			strategy.encode(out, tx, includeSignature);
			out.endList();
		});
	}
}
