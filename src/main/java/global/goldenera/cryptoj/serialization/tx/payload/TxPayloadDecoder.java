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
package global.goldenera.cryptoj.serialization.tx.payload;

import java.util.HashMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.common.payloads.TxPayload;
import global.goldenera.cryptoj.enums.TxPayloadType;
import global.goldenera.cryptoj.enums.TxVersion;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAddressAliasAddDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAddressAliasRemoveDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAuthorityAddDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAuthorityRemoveDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxBipVoteDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxNetworkParamsSetDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenBurnDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenCreateDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenMintDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenUpdateDecodingStrategy;
import global.goldenera.rlp.RLP;
import global.goldenera.rlp.RLPInput;

public class TxPayloadDecoder {

	public static final TxPayloadDecoder INSTANCE = new TxPayloadDecoder();
	private final Map<DecoderKey, TxPayloadDecodingStrategy<?>> strategies = new HashMap<>();

	private TxPayloadDecoder() {
		// Address Alias
		register(
				TxPayloadType.BIP_ADDRESS_ALIAS_ADD,
				new TxAddressAliasAddDecodingStrategy(),
				TxVersion.V1);
		register(
				TxPayloadType.BIP_ADDRESS_ALIAS_REMOVE,
				new TxAddressAliasRemoveDecodingStrategy(),
				TxVersion.V1);
		// Authority
		register(
				TxPayloadType.BIP_AUTHORITY_ADD,
				new TxAuthorityAddDecodingStrategy(),
				TxVersion.V1);
		register(
				TxPayloadType.BIP_AUTHORITY_REMOVE,
				new TxAuthorityRemoveDecodingStrategy(),
				TxVersion.V1);
		// Bip vote
		register(
				TxPayloadType.BIP_VOTE,
				new TxBipVoteDecodingStrategy(),
				TxVersion.V1);
		// Set params
		register(
				TxPayloadType.BIP_NETWORK_PARAMS_SET,
				new TxNetworkParamsSetDecodingStrategy(),
				TxVersion.V1);
		// Token burn
		register(
				TxPayloadType.BIP_TOKEN_BURN,
				new TxTokenBurnDecodingStrategy(),
				TxVersion.V1);
		// Token mint
		register(
				TxPayloadType.BIP_TOKEN_MINT,
				new TxTokenMintDecodingStrategy(),
				TxVersion.V1);
		// Token create
		register(
				TxPayloadType.BIP_TOKEN_CREATE,
				new TxTokenCreateDecodingStrategy(),
				TxVersion.V1);
		// Token update
		register(
				TxPayloadType.BIP_TOKEN_UPDATE,
				new TxTokenUpdateDecodingStrategy(),
				TxVersion.V1);
	}

	public TxPayload decode(Bytes rlpBytes, TxVersion version) {
		if (rlpBytes == null || rlpBytes.isEmpty()) {
			return null;
		}

		if (version == null) {
			throw new IllegalArgumentException("Version cannot be null");
		}

		RLP.validate(rlpBytes);
		RLPInput input = RLP.input(rlpBytes);
		int fields = input.enterList();
		if (fields < 1) {
			throw new IllegalArgumentException("Invalid RLP: Missing payload type field");
		}
		TxPayloadType type = TxPayloadType.fromCode(input.readIntScalar());
		DecoderKey key = new DecoderKey(type, version);
		TxPayloadDecodingStrategy<?> strategy = strategies.get(key);

		if (strategy == null) {
			throw new IllegalArgumentException(
					"No payload decoder found for Type: " + type + " and Version: " + version);
		}
		TxPayload txPayload = strategy.decode(input);
		input.leaveList();
		return txPayload;
	}

	private void register(TxPayloadType type, TxPayloadDecodingStrategy<?> strategy, TxVersion... versions) {
		for (TxVersion v : versions) {
			strategies.put(new DecoderKey(type, v), strategy);
		}
	}

	private record DecoderKey(TxPayloadType type, TxVersion version) {
	}

}