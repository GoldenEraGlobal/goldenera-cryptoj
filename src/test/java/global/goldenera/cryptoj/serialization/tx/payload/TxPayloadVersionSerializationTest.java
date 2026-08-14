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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import global.goldenera.cryptoj.common.payloads.bip.TxBipNetworkParamsSetPayload;
import global.goldenera.cryptoj.common.payloads.bip.TxBipNetworkParamsSetPayloadImpl;
import global.goldenera.cryptoj.common.payloads.bip.TxBipValidatorAddPayload;
import global.goldenera.cryptoj.common.payloads.bip.TxBipValidatorAddPayloadImpl;
import global.goldenera.cryptoj.common.payloads.bip.TxBipValidatorMiningPolicySetPayload;
import global.goldenera.cryptoj.common.payloads.bip.TxBipValidatorMiningPolicySetPayloadImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.enums.TxPayloadVersion;
import global.goldenera.cryptoj.enums.TxVersion;

class TxPayloadVersionSerializationTest {

	private static final Address VALIDATOR = Address.fromHexString("0x1111111111111111111111111111111111111111");

	@Test
	void preservesHistoricalValidatorAddBytesAsImplicitV1() {
		Bytes historicalBytes = Bytes.fromHexString("0xd60a941111111111111111111111111111111111111111");
		TxBipValidatorAddPayload decoded = assertInstanceOf(TxBipValidatorAddPayload.class,
				TxPayloadDecoder.INSTANCE.decode(historicalBytes, TxVersion.V1));
		assertEquals(TxPayloadVersion.V1, decoded.getPayloadVersion());
		assertEquals(VALIDATOR, decoded.getAddress());
		assertEquals(historicalBytes, TxPayloadEncoder.INSTANCE.encode(decoded, TxVersion.V1));
	}

	@Test
	void encodesAndDecodesValidatorAddV2WithExplicitPayloadVersion() {
		TxBipValidatorAddPayload payload = TxBipValidatorAddPayloadImpl.builder()
				.payloadVersion(TxPayloadVersion.V2)
				.address(VALIDATOR)
				.miningLimitMode(MiningLimitMode.LIMITED)
				.maxMiningShareBps(3_000L)
				.build();
		Bytes expected = Bytes.fromHexString("0xdb0a0294111111111111111111111111111111111111111180820bb8");
		assertEquals(expected, TxPayloadEncoder.INSTANCE.encode(payload, TxVersion.V1));
		assertEquals(payload, TxPayloadDecoder.INSTANCE.decode(expected, TxVersion.V1));
	}

	@Test
	void preservesHistoricalNetworkParamsBytesAndRoundTripsV2() {
		Bytes v1Bytes = Bytes.fromHexString("0xc804c0c0c0c0c0c0c0");
		TxBipNetworkParamsSetPayload v1 = assertInstanceOf(TxBipNetworkParamsSetPayload.class,
				TxPayloadDecoder.INSTANCE.decode(v1Bytes, TxVersion.V1));
		assertEquals(TxPayloadVersion.V1, v1.getPayloadVersion());
		assertEquals(v1Bytes, TxPayloadEncoder.INSTANCE.encode(v1, TxVersion.V1));

		TxBipNetworkParamsSetPayload v2 = TxBipNetworkParamsSetPayloadImpl.builder()
				.payloadVersion(TxPayloadVersion.V2)
				.validatorMiningWindowBlocks(100L)
				.build();
		Bytes v2Bytes = Bytes.fromHexString("0xcb0402c0c0c0c0c0c0c0c164");
		assertEquals(v2Bytes, TxPayloadEncoder.INSTANCE.encode(v2, TxVersion.V1));
		assertEquals(v2, TxPayloadDecoder.INSTANCE.decode(v2Bytes, TxVersion.V1));

		TxBipNetworkParamsSetPayload v2WithoutResize = TxBipNetworkParamsSetPayloadImpl.builder()
				.payloadVersion(TxPayloadVersion.V2)
				.build();
		Bytes v2WithoutResizeBytes = Bytes.fromHexString("0xca0402c0c0c0c0c0c0c0c0");
		assertEquals(v2WithoutResizeBytes,
				TxPayloadEncoder.INSTANCE.encode(v2WithoutResize, TxVersion.V1));
		assertEquals(v2WithoutResize,
				TxPayloadDecoder.INSTANCE.decode(v2WithoutResizeBytes, TxVersion.V1));
	}

	@Test
	void roundTripsNewPolicyPayloadAndRejectsNonCanonicalPolicy() {
		TxBipValidatorMiningPolicySetPayload payload = TxBipValidatorMiningPolicySetPayloadImpl.builder()
				.validatorAddress(VALIDATOR)
				.miningLimitMode(MiningLimitMode.UNLIMITED)
				.maxMiningShareBps(0)
				.build();
		Bytes expected = Bytes.fromHexString("0xd90c019411111111111111111111111111111111111111110180");
		assertEquals(expected, TxPayloadEncoder.INSTANCE.encode(payload, TxVersion.V1));
		assertEquals(payload, TxPayloadDecoder.INSTANCE.decode(expected, TxVersion.V1));

		TxBipValidatorMiningPolicySetPayload invalid = TxBipValidatorMiningPolicySetPayloadImpl.builder()
				.validatorAddress(VALIDATOR)
				.miningLimitMode(MiningLimitMode.UNLIMITED)
				.maxMiningShareBps(1)
				.build();
		assertThrows(RuntimeException.class, () -> TxPayloadEncoder.INSTANCE.encode(invalid, TxVersion.V1));
	}
}
