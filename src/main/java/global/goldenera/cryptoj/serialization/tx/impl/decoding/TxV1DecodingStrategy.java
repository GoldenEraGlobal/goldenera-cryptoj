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
package global.goldenera.cryptoj.serialization.tx.impl.decoding;

import java.time.Instant;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.common.TxImpl;
import global.goldenera.cryptoj.common.payloads.TxPayload;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.Signature;
import global.goldenera.cryptoj.enums.Network;
import global.goldenera.cryptoj.enums.TxType;
import global.goldenera.cryptoj.enums.TxVersion;
import global.goldenera.cryptoj.serialization.tx.TxDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.TxPayloadDecoder;
import global.goldenera.rlp.RLPInput;

public class TxV1DecodingStrategy implements TxDecodingStrategy {

	private final static TxVersion VERSION = TxVersion.V1;

	@Override
	public Tx decode(RLPInput input) {
		Instant timestamp = Instant.ofEpochMilli(input.readLongScalar());
		TxType type = TxType.fromCode(input.readIntScalar());
		Network network = Network.fromCode(input.readIntScalar());
		Long nonce = input.readOptionalLongScalar();
		Bytes recipientBytes = input.readOptionalBytes();
		Address recipient = recipientBytes != null ? Address.wrap(recipientBytes) : null;
		Bytes tokenAddressBytes = input.readOptionalBytes();
		Address tokenAddress = tokenAddressBytes != null ? Address.wrap(tokenAddressBytes) : null;
		Wei amount = input.readOptionalWeiScalar();
		Wei fee = input.readWeiScalar();
		Bytes messageBytes = input.readOptionalBytes();
		TxPayload payload = TxPayloadDecoder.INSTANCE.decode(input.readOptionalRaw(), VERSION);
		Bytes32 referenceHashBytes = input.readOptionalBytes32();
		Hash referenceHash = referenceHashBytes != null ? Hash.wrap(referenceHashBytes) : null;
		Signature signature = null;
		if (!input.isEndOfCurrentList()) {
			signature = Signature.wrap(input.readBytes());
		}
		return TxImpl.builder()
				.version(VERSION)
				.timestamp(timestamp)
				.type(type)
				.network(network)
				.nonce(nonce)
				.recipient(recipient)
				.amount(amount)
				.tokenAddress(tokenAddress)
				.message(messageBytes)
				.fee(fee)
				.payload(payload)
				.referenceHash(referenceHash)
				.signature(signature)
				.build();
	}
}