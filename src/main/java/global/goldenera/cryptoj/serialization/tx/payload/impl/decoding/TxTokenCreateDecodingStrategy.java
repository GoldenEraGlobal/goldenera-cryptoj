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
package global.goldenera.cryptoj.serialization.tx.payload.impl.decoding;

import java.math.BigInteger;

import global.goldenera.cryptoj.common.payloads.bip.TxBipTokenCreatePayload;
import global.goldenera.cryptoj.common.payloads.bip.TxBipTokenCreatePayloadImpl;
import global.goldenera.cryptoj.serialization.tx.payload.TxPayloadDecodingStrategy;
import global.goldenera.rlp.RLPInput;

public class TxTokenCreateDecodingStrategy implements TxPayloadDecodingStrategy<TxBipTokenCreatePayload> {

	@Override
	public TxBipTokenCreatePayload decode(RLPInput input) {
		String name = input.readString();
		String smallestUnitName = input.readString();
		int numberOfDecimals = input.readIntScalar();
		String websiteUrl = input.readOptionalString();
		String logoUrl = input.readOptionalString();
		BigInteger maxSupply = input.readOptionalBigIntegerScalar();
		boolean userBurnable = input.readIntScalar() == 1;

		return TxBipTokenCreatePayloadImpl.builder()
				.name(name)
				.smallestUnitName(smallestUnitName)
				.numberOfDecimals(numberOfDecimals)
				.websiteUrl(websiteUrl)
				.logoUrl(logoUrl)
				.maxSupply(maxSupply)
				.userBurnable(userBurnable)
				.build();
	}
}