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

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.common.Tx;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TxRootUtil {

	/**
	 * Calculates Merkle Root using Bitcoin-style logic.
	 */
	public static Hash txRootHash(List<? extends Tx> txs) {
		if (txs == null || txs.isEmpty()) {
			return Hash.ZERO;
		}
		int size = txs.size();
		List<Hash> layer = new ArrayList<>(size);
		for (Tx tx : txs) {
			layer.add(TxUtil.hash(tx));
		}
		return buildMerkleTreeInPlace(layer);
	}

	private static Hash buildMerkleTreeInPlace(@NonNull List<Hash> layer) {
		int size = layer.size();
		if (size == 0) {
			return Hash.ZERO;
		}

		while (size > 1) {
			int writeIndex = 0;
			for (int i = 0; i < size; i += 2) {
				Hash left = layer.get(i);
				Hash right = (i + 1 < size) ? layer.get(i + 1) : left;
				Bytes combined = Bytes.concatenate(left, right);
				layer.set(writeIndex++, Hash.hash(combined));
			}
			size = writeIndex;
		}
		return layer.get(0);
	}
}