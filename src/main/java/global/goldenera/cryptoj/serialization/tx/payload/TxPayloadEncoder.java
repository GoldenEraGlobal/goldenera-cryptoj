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
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxAddressAliasAddEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxAddressAliasRemoveEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxAuthorityAddEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxAuthorityRemoveEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxBipVoteEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxNetworkParamsSetEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxTokenBurnEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxTokenCreateEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxTokenMintEncodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.encoding.TxTokenUpdateEncodingStrategy;
import global.goldenera.rlp.RLP;

public class TxPayloadEncoder {

	public static final TxPayloadEncoder INSTANCE = new TxPayloadEncoder();
	private final Map<EncoderKey, TxPayloadEncodingStrategy<?>> strategies = new HashMap<>();

	private TxPayloadEncoder() {
		register(TxPayloadType.BIP_ADDRESS_ALIAS_ADD, new TxAddressAliasAddEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_ADDRESS_ALIAS_REMOVE, new TxAddressAliasRemoveEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_AUTHORITY_ADD, new TxAuthorityAddEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_AUTHORITY_REMOVE, new TxAuthorityRemoveEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_TOKEN_CREATE, new TxTokenCreateEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_TOKEN_UPDATE, new TxTokenUpdateEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_TOKEN_MINT, new TxTokenMintEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_TOKEN_BURN, new TxTokenBurnEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_NETWORK_PARAMS_SET, new TxNetworkParamsSetEncodingStrategy(), TxVersion.V1);
		register(TxPayloadType.BIP_VOTE, new TxBipVoteEncodingStrategy(), TxVersion.V1);
	}

	@SuppressWarnings("unchecked")
	public <T extends TxPayload> Bytes encode(T payload, TxVersion version) {
		if (payload == null)
			return null;

		if (version == null) {
			throw new CryptoJFailedException("Version cannot be null");
		}

		EncoderKey key = new EncoderKey(payload.getPayloadType(), version);
		TxPayloadEncodingStrategy<T> strategy = (TxPayloadEncodingStrategy<T>) strategies.get(key);

		if (strategy == null) {
			throw new CryptoJFailedException(
					String.format("No serializer found for Payload: %s and TxVersion: %s", payload.getPayloadType(),
							version));
		}

		return RLP.encode(out -> {
			out.startList();
			out.writeIntScalar(payload.getPayloadType().getCode());
			strategy.encode(out, payload);
			out.endList();
		});
	}

	private <T extends TxPayload> void register(TxPayloadType type, TxPayloadEncodingStrategy<?> strategy,
			TxVersion... versions) {
		for (TxVersion v : versions) {
			strategies.put(new EncoderKey(type, v), strategy);
		}
	}

	private record EncoderKey(TxPayloadType type, TxVersion version) {
	}
}