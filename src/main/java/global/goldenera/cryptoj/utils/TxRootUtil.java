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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.datatypes.Hash;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TxRootUtil {

	private static final int PARALLEL_THRESHOLD = 2048;

	/**
	 * Calculates Merkle Root.
	 * Assumes tx.getHash() is pre-calculated/cached (O(1) access).
	 */
	public static Hash txRootHash(List<? extends Tx> txs) {
		if (txs == null || txs.isEmpty()) {
			return Hash.ZERO;
		}

		int size = txs.size();
		List<Hash> currentLayer = new ArrayList<>(size);
		for (Tx tx : txs) {
			currentLayer.add(tx.getHash());
		}

		return buildMerkleTree(currentLayer);
	}

	private static Hash buildMerkleTree(List<Hash> layer) {
		while (layer.size() > 1) {
			layer = calculateNextLayer(layer);
		}
		return layer.get(0);
	}

	private static List<Hash> calculateNextLayer(List<Hash> currentLayer) {
		int size = currentLayer.size();
		int nextSize = (size + 1) / 2;
		boolean useParallel = size > PARALLEL_THRESHOLD;
		IntStream indexStream = IntStream.range(0, nextSize);

		if (useParallel) {
			indexStream = indexStream.parallel();
		}

		return indexStream
				.mapToObj(i -> {
					int leftIndex = i * 2;
					int rightIndex = leftIndex + 1;
					Hash left = currentLayer.get(leftIndex);
					Hash right = (rightIndex < size) ? currentLayer.get(rightIndex) : left;
					return hashPair(left, right);
				})
				.collect(Collectors.toList());
	}

	private static Hash hashPair(Hash left, Hash right) {
		Bytes combined = Bytes.concatenate(left, right);
		return Hash.hash(combined);
	}
}