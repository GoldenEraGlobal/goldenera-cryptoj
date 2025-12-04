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
package global.goldenera.cryptoj.serialization.block.impl.decoding;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.common.Block;
import global.goldenera.cryptoj.common.BlockHeader;
import global.goldenera.cryptoj.common.BlockImpl;
import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.serialization.block.BlockDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.TxDecoder;
import global.goldenera.rlp.RLPInput;

public class BlockV1DecodingStrategy implements BlockDecodingStrategy {

	@Override
	public Block decodeBody(RLPInput input, BlockHeader header, boolean excludeTxs) {
		// Enter the list and capture the item count to optimize list allocation
		int txCount = input.enterList();

		List<Tx> txs;

		if (excludeTxs) {
			// Optimization: If transactions are excluded, we set the list to null (or empty
			// list if required by your logic)
			// and simply skip the underlying RLP elements without allocating memory for
			// Bytes.
			txs = null;
			while (!input.isEndOfCurrentList()) {
				input.skipNext();
			}
		} else {
			// Optimization: Pre-allocate the ArrayList to avoid internal array resizing
			// overhead.
			txs = new ArrayList<>(txCount);
			while (!input.isEndOfCurrentList()) {
				// We proceed to read and decode the transaction only if required.
				Bytes txBytes = input.readRaw();
				txs.add(TxDecoder.INSTANCE.decode(txBytes));
			}
		}

		input.leaveList();

		return BlockImpl.builder()
				.header(header)
				.txs(txs)
				.build();
	}
}
