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
package global.goldenera.cryptoj.serialization.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;

import global.goldenera.cryptoj.common.state.NetworkParamsState;
import global.goldenera.cryptoj.common.state.ValidatorState;
import global.goldenera.cryptoj.common.state.impl.NetworkParamsStateImpl;
import global.goldenera.cryptoj.common.state.impl.ValidatorStateImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.enums.state.NetworkParamsStateVersion;
import global.goldenera.cryptoj.enums.state.ValidatorStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.state.networkparams.NetworkParamsStateDecoder;
import global.goldenera.cryptoj.serialization.state.networkparams.NetworkParamsStateEncoder;
import global.goldenera.cryptoj.serialization.state.validator.ValidatorStateDecoder;
import global.goldenera.cryptoj.serialization.state.validator.ValidatorStateEncoder;

class ConsensusStateSerializationTest {

	@Test
	void preservesValidatorStateV1BytesAndResolvesLegacyPolicy() {
		String zeros = "00".repeat(32);
		Bytes historical = Bytes.fromHexString("0xe70105c38203e8a0" + zeros);
		ValidatorState decoded = ValidatorStateDecoder.INSTANCE.decode(historical);
		assertEquals(ValidatorStateVersion.V1, decoded.getVersion());
		assertEquals(MiningLimitMode.UNLIMITED, decoded.getMiningLimitMode());
		assertEquals(0, decoded.getMaxMiningShareBps());
		assertEquals(historical, ValidatorStateEncoder.INSTANCE.encode(decoded));
	}

	@Test
	void roundTripsValidatorStateV2WithPolicyAuditMetadata() {
		ValidatorState state = ValidatorStateImpl.builder()
				.version(ValidatorStateVersion.V2)
				.createdAtBlockHeight(5)
				.createdAtTimestamp(Instant.ofEpochMilli(1_000))
				.originTxHash(hash((byte) 1))
				.miningLimitMode(MiningLimitMode.LIMITED)
				.maxMiningShareBps(4_000)
				.policyUpdatedByTxHash(hash((byte) 2))
				.policyUpdatedAtBlockHeight(10)
				.policyUpdatedAtTimestamp(Instant.ofEpochMilli(2_000))
				.build();
		assertEquals(state, ValidatorStateDecoder.INSTANCE.decode(ValidatorStateEncoder.INSTANCE.encode(state)));
	}

	@Test
	void roundTripsBothNetworkParamsStateVersionsWithLegacyEffectiveCounter() {
		NetworkParamsState v1 = baseNetworkParams(NetworkParamsStateVersion.V1).build();
		String zeros20 = "00".repeat(20);
		String zeros32 = "00".repeat(32);
		Bytes historicalV1 = Bytes.fromHexString("0xf846010a94" + zeros20
				+ "82753064050a0101a0" + zeros32 + "0203058203e8");
		assertEquals(historicalV1, NetworkParamsStateEncoder.INSTANCE.encode(v1));
		NetworkParamsState decodedV1 = NetworkParamsStateDecoder.INSTANCE.decode(historicalV1);
		assertEquals(historicalV1, NetworkParamsStateEncoder.INSTANCE.encode(decodedV1));
		assertEquals(3, decodedV1.getCurrentUnlimitedValidatorCount());
		assertEquals(0, decodedV1.getValidatorMiningWindowBlocks());

		NetworkParamsState v2 = baseNetworkParams(NetworkParamsStateVersion.V2)
				.validatorMiningWindowBlocks(1_000)
				.currentUnlimitedValidatorCount(2)
				.limitedValidatorMiningSharesBps(List.of(1_000L))
				.build();
		Bytes v2Bytes = NetworkParamsStateEncoder.INSTANCE.encode(v2);
		assertEquals("0xf84e020a94000000000000000000000000000000000000000082753064050a0101"
				+ "a00000000000000000000000000000000000000000000000000000000000000000"
				+ "0203058203e88203e802c38203e8", v2Bytes.toHexString());
		assertEquals(v2, NetworkParamsStateDecoder.INSTANCE.decode(v2Bytes));
	}

	@Test
	void rejectsInconsistentOrNonCanonicalLimitedPolicySummary() {
		NetworkParamsState inconsistent = baseNetworkParams(NetworkParamsStateVersion.V2)
				.validatorMiningWindowBlocks(1_000)
				.currentUnlimitedValidatorCount(2)
				.build();
		assertThrows(IllegalArgumentException.class,
				() -> NetworkParamsStateEncoder.INSTANCE.encode(inconsistent));

		NetworkParamsState unsorted = baseNetworkParams(NetworkParamsStateVersion.V2)
				.validatorMiningWindowBlocks(1_000)
				.currentUnlimitedValidatorCount(1)
				.limitedValidatorMiningSharesBps(List.of(2_000L, 1_000L))
				.build();
		assertThrows(IllegalArgumentException.class,
				() -> NetworkParamsStateEncoder.INSTANCE.encode(unsorted));
	}

	@Test
	void roundTripsCanonicalZeroValidatorNetworkParamsStateV2() {
		NetworkParamsState state = baseNetworkParams(NetworkParamsStateVersion.V2)
				.currentValidatorCount(0)
				.validatorMiningWindowBlocks(1_000)
				.currentUnlimitedValidatorCount(0)
				.limitedValidatorMiningSharesBps(List.of())
				.build();

		Bytes encoded = NetworkParamsStateEncoder.INSTANCE.encode(state);

		assertEquals(state, NetworkParamsStateDecoder.INSTANCE.decode(encoded));
	}

	@Test
	void rejectsNonCanonicalZeroValidatorNetworkParamsStateV2() {
		NetworkParamsState canonical = baseNetworkParams(NetworkParamsStateVersion.V2)
				.currentValidatorCount(0)
				.validatorMiningWindowBlocks(1_000)
				.currentUnlimitedValidatorCount(0)
				.limitedValidatorMiningSharesBps(List.of())
				.build();
		NetworkParamsState nonZeroUnlimited = baseNetworkParams(NetworkParamsStateVersion.V2)
				.currentValidatorCount(0)
				.validatorMiningWindowBlocks(1_000)
				.currentUnlimitedValidatorCount(1)
				.limitedValidatorMiningSharesBps(List.of())
				.build();
		NetworkParamsState limitedEntry = baseNetworkParams(NetworkParamsStateVersion.V2)
				.currentValidatorCount(0)
				.validatorMiningWindowBlocks(1_000)
				.currentUnlimitedValidatorCount(0)
				.limitedValidatorMiningSharesBps(List.of(1_000L))
				.build();

		assertThrows(IllegalArgumentException.class,
				() -> NetworkParamsStateEncoder.INSTANCE.encode(nonZeroUnlimited));
		assertThrows(IllegalArgumentException.class,
				() -> NetworkParamsStateEncoder.INSTANCE.encode(limitedEntry));

		Bytes canonicalBytes = NetworkParamsStateEncoder.INSTANCE.encode(canonical);
		Bytes nonZeroUnlimitedBytes = Bytes.concatenate(
				canonicalBytes.slice(0, canonicalBytes.size() - 2),
				Bytes.of(1),
				canonicalBytes.slice(canonicalBytes.size() - 1));
		assertThrows(CryptoJFailedException.class,
				() -> NetworkParamsStateDecoder.INSTANCE.decode(nonZeroUnlimitedBytes));
	}

	private NetworkParamsStateImpl.NetworkParamsStateImplBuilder baseNetworkParams(NetworkParamsStateVersion version) {
		return NetworkParamsStateImpl.builder()
				.version(version)
				.blockReward(Wei.valueOf(10))
				.blockRewardPoolAddress(Address.ZERO)
				.targetMiningTimeMs(30_000)
				.asertHalfLifeBlocks(100)
				.asertAnchorHeight(5)
				.minDifficulty(BigInteger.TEN)
				.minTxBaseFee(Wei.valueOf(1))
				.minTxByteFee(Wei.valueOf(1))
				.updatedByTxHash(Hash.ZERO)
				.currentAuthorityCount(2)
				.currentValidatorCount(3)
				.updatedAtBlockHeight(5)
				.updatedAtTimestamp(Instant.ofEpochMilli(1_000));
	}

	private Hash hash(byte value) {
		byte[] bytes = new byte[32];
		bytes[31] = value;
		return Hash.wrap(bytes);
	}
}
