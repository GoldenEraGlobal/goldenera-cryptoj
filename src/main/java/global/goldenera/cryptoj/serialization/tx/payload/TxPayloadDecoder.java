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
import global.goldenera.cryptoj.enums.TxPayloadVersion;
import global.goldenera.cryptoj.enums.TxVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAddressAliasAddDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAddressAliasRemoveDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAuthorityAddDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxAuthorityRemoveDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxBipVoteDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxNetworkParamsSetDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxNetworkParamsSetV2DecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenBurnDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenCreateDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenMintDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxTokenUpdateDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxValidatorAddDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxValidatorAddV2DecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxValidatorMiningPolicySetDecodingStrategy;
import global.goldenera.cryptoj.serialization.tx.payload.impl.decoding.TxValidatorRemoveDecodingStrategy;
import global.goldenera.rlp.RLP;
import global.goldenera.rlp.RLPInput;

public class TxPayloadDecoder {

	public static final TxPayloadDecoder INSTANCE = new TxPayloadDecoder();
	private final Map<DecoderKey, TxPayloadDecodingStrategy<?>> strategies = new HashMap<>();

	private TxPayloadDecoder() {
		// Address Alias
		registerImplicitV1(
				TxPayloadType.BIP_ADDRESS_ALIAS_ADD,
				new TxAddressAliasAddDecodingStrategy(),
				TxVersion.V1);
		registerImplicitV1(
				TxPayloadType.BIP_ADDRESS_ALIAS_REMOVE,
				new TxAddressAliasRemoveDecodingStrategy(),
				TxVersion.V1);
		// Authority
		registerImplicitV1(
				TxPayloadType.BIP_AUTHORITY_ADD,
				new TxAuthorityAddDecodingStrategy(),
				TxVersion.V1);
		registerImplicitV1(
				TxPayloadType.BIP_AUTHORITY_REMOVE,
				new TxAuthorityRemoveDecodingStrategy(),
				TxVersion.V1);
		// Bip vote
		registerImplicitV1(
				TxPayloadType.BIP_VOTE,
				new TxBipVoteDecodingStrategy(),
				TxVersion.V1);
		// Set params
		registerImplicitV1(
				TxPayloadType.BIP_NETWORK_PARAMS_SET,
				new TxNetworkParamsSetDecodingStrategy(),
				TxVersion.V1);
		registerExplicit(TxPayloadType.BIP_NETWORK_PARAMS_SET, TxPayloadVersion.V2,
				new TxNetworkParamsSetV2DecodingStrategy(), TxVersion.V1);
		// Token burn
		registerImplicitV1(
				TxPayloadType.BIP_TOKEN_BURN,
				new TxTokenBurnDecodingStrategy(),
				TxVersion.V1);
		// Token mint
		registerImplicitV1(
				TxPayloadType.BIP_TOKEN_MINT,
				new TxTokenMintDecodingStrategy(),
				TxVersion.V1);
		// Token create
		registerImplicitV1(
				TxPayloadType.BIP_TOKEN_CREATE,
				new TxTokenCreateDecodingStrategy(),
				TxVersion.V1);
		// Token update
		registerImplicitV1(
				TxPayloadType.BIP_TOKEN_UPDATE,
				new TxTokenUpdateDecodingStrategy(),
				TxVersion.V1);
		// Validator
		registerImplicitV1(
				TxPayloadType.BIP_VALIDATOR_ADD,
				new TxValidatorAddDecodingStrategy(),
				TxVersion.V1);
		registerExplicit(TxPayloadType.BIP_VALIDATOR_ADD, TxPayloadVersion.V2,
				new TxValidatorAddV2DecodingStrategy(), TxVersion.V1);
		registerImplicitV1(
				TxPayloadType.BIP_VALIDATOR_REMOVE,
				new TxValidatorRemoveDecodingStrategy(),
				TxVersion.V1);
		registerExplicit(TxPayloadType.BIP_VALIDATOR_MINING_POLICY_SET, TxPayloadVersion.V1,
				new TxValidatorMiningPolicySetDecodingStrategy(), TxVersion.V1);
	}

	public TxPayload decode(Bytes rlpBytes, TxVersion version) {
		if (rlpBytes == null || rlpBytes.isEmpty()) {
			return null;
		}

		if (version == null) {
			throw new CryptoJFailedException("Version cannot be null");
		}

		RLP.validate(rlpBytes);
		RLPInput input = RLP.input(rlpBytes);
		int fields = input.enterList();
		if (fields < 1) {
			throw new CryptoJFailedException("Invalid RLP: Missing payload type field");
		}
		TxPayloadType type = TxPayloadType.fromCode(input.readIntScalar());
		TxPayloadVersion payloadVersion = inferPayloadVersion(type, fields, input);
		DecoderKey key = new DecoderKey(type, version, payloadVersion);
		TxPayloadDecodingStrategy<?> strategy = strategies.get(key);

		if (strategy == null) {
			throw new CryptoJFailedException(
					"No payload decoder found for Type: " + type + " and Version: " + version);
		}
		TxPayload txPayload = strategy.decode(input);
		if (!input.isEndOfCurrentList()) {
			throw new CryptoJFailedException("Payload contains unexpected RLP fields");
		}
		input.leaveList();
		return txPayload;
	}

	private TxPayloadVersion inferPayloadVersion(TxPayloadType type, int fields, RLPInput input) {
		boolean implicitV1 = type == TxPayloadType.BIP_VALIDATOR_ADD && fields == 2
				|| type == TxPayloadType.BIP_NETWORK_PARAMS_SET && fields == 8
				|| type != TxPayloadType.BIP_VALIDATOR_ADD
						&& type != TxPayloadType.BIP_NETWORK_PARAMS_SET
						&& type != TxPayloadType.BIP_VALIDATOR_MINING_POLICY_SET;
		if (implicitV1) {
			return TxPayloadVersion.V1;
		}
		int expectedFields = switch (type) {
			case BIP_VALIDATOR_ADD -> 5;
			case BIP_NETWORK_PARAMS_SET -> 11;
			case BIP_VALIDATOR_MINING_POLICY_SET -> 5;
			default -> throw new CryptoJFailedException("Invalid versioned payload shape for " + type);
		};
		if (fields != expectedFields) {
			throw new CryptoJFailedException("Invalid RLP field count " + fields + " for " + type);
		}
		return TxPayloadVersion.fromCode(input.readIntScalar());
	}

	private void registerImplicitV1(TxPayloadType type, TxPayloadDecodingStrategy<?> strategy,
			TxVersion... versions) {
		for (TxVersion v : versions) {
			strategies.put(new DecoderKey(type, v, TxPayloadVersion.V1), strategy);
		}
	}

	private void registerExplicit(TxPayloadType type, TxPayloadVersion payloadVersion,
			TxPayloadDecodingStrategy<?> strategy, TxVersion... versions) {
		for (TxVersion txVersion : versions) {
			strategies.put(new DecoderKey(type, txVersion, payloadVersion), strategy);
		}
	}

	private record DecoderKey(TxPayloadType type, TxVersion txVersion, TxPayloadVersion payloadVersion) {
	}

}
