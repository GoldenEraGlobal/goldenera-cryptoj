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
package global.goldenera.cryptoj.serialization.tx.impl.encoding;

import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.serialization.tx.TxEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.TxPayloadEncoder;
import global.goldenera.rlp.RLPOutput;

public class TxV1EncodingStrategy implements TxEncodingStrategy {

	@Override
	public void encode(RLPOutput out, Tx tx, boolean includeSignature) {
		out.writeLongScalar(tx.getTimestamp().toEpochMilli());
		out.writeIntScalar(tx.getType().getCode());
		out.writeIntScalar(tx.getNetwork().getCode());
		out.writeOptionalLongScalar(tx.getNonce());
		out.writeOptionalBytes(tx.getRecipient());
		out.writeOptionalBytes(tx.getTokenAddress());
		out.writeOptionalWeiScalar(tx.getAmount());
		out.writeWeiScalar(tx.getFee());
		out.writeOptionalBytes(tx.getMessage());
		out.writeOptionalRaw(TxPayloadEncoder.INSTANCE.encode(tx.getPayload(), tx.getVersion()));
		out.writeOptionalBytes32(tx.getReferenceHash());
		if (includeSignature) {
			out.writeBytes(tx.getSignature());
		}
	}
}
