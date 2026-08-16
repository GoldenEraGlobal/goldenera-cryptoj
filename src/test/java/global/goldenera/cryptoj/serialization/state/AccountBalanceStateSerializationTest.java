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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;

import global.goldenera.cryptoj.common.state.AccountBalanceState;
import global.goldenera.cryptoj.common.state.impl.AccountBalanceStateImpl;
import global.goldenera.cryptoj.enums.state.AccountBalanceStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.state.accountbalance.AccountBalanceStateDecoder;
import global.goldenera.cryptoj.serialization.state.accountbalance.AccountBalanceStateEncoder;

class AccountBalanceStateSerializationTest {

	private static final Instant TIME = Instant.ofEpochMilli(1_000);

	@Test
	void preservesHistoricalV1BytesAndExplicitV1Zero() {
		AccountBalanceState state = AccountBalanceStateImpl.builder()
				.version(AccountBalanceStateVersion.V1)
				.balance(Wei.valueOf(10))
				.updatedAtBlockHeight(5)
				.updatedAtTimestamp(TIME)
				.build();
		Bytes historical = Bytes.fromHexString("0xc7010a05c38203e8");

		assertEquals(historical, AccountBalanceStateEncoder.INSTANCE.encode(state));
		assertEquals(state, AccountBalanceStateDecoder.INSTANCE.decode(historical));
		assertEquals(Wei.ZERO, state.getLockedMiningReward());
		assertEquals(Wei.ZERO, state.getPendingMiningRewardCancellation());
		assertEquals(Wei.valueOf(10), state.getSpendableBalance());
		assertEquals(AccountBalanceStateVersion.V1, AccountBalanceStateImpl.ZERO.getVersion());
		assertEquals(AccountBalanceStateVersion.V2, AccountBalanceStateImpl.ZERO_V2.getVersion());
	}

	@Test
	void roundTripsV2AndEnforcesSpendableBalance() {
		AccountBalanceStateImpl state = AccountBalanceStateImpl.builder()
				.version(AccountBalanceStateVersion.V2)
				.balance(Wei.valueOf(10))
				.lockedMiningReward(Wei.valueOf(4))
				.pendingMiningRewardCancellation(Wei.valueOf(3))
				.updatedAtBlockHeight(5)
				.updatedAtTimestamp(TIME)
				.build();
		Bytes expected = Bytes.fromHexString("0xc9020a05c38203e80403");

		assertEquals(expected, AccountBalanceStateEncoder.INSTANCE.encode(state));
		assertEquals(state, AccountBalanceStateDecoder.INSTANCE.decode(expected));
		assertEquals(Wei.valueOf(6), state.getSpendableBalance());
		assertEquals(Wei.valueOf(3), state.getPendingMiningRewardCancellation());
		assertEquals(Wei.valueOf(4), state.debit(Wei.valueOf(6), 6, TIME).getBalance());
		assertThrows(IllegalArgumentException.class, () -> state.debit(Wei.valueOf(7), 6, TIME));
	}

	@Test
	void creditsFeesNormallyAndMiningRewardsAsLockedThenUnlocksWithoutChangingTotal() {
		AccountBalanceStateImpl legacy = (AccountBalanceStateImpl) AccountBalanceStateImpl.ZERO;
		AccountBalanceStateImpl upgraded = legacy.upgradeToV2();
		assertEquals(AccountBalanceStateVersion.V2, upgraded.getVersion());
		assertEquals(Wei.ZERO, upgraded.getLockedMiningReward());
		assertEquals(Wei.ZERO, upgraded.getPendingMiningRewardCancellation());
		assertSame(upgraded, upgraded.upgradeToV2());

		AccountBalanceStateImpl withFees = upgraded.credit(Wei.valueOf(2), 1, TIME);
		AccountBalanceStateImpl withReward = withFees.creditLockedMiningReward(Wei.valueOf(5), 1, TIME);
		assertEquals(Wei.valueOf(7), withReward.getBalance());
		assertEquals(Wei.valueOf(5), withReward.getLockedMiningReward());
		assertEquals(Wei.ZERO, withReward.getPendingMiningRewardCancellation());
		assertEquals(Wei.valueOf(2), withReward.getSpendableBalance());

		AccountBalanceStateImpl partiallyUnlocked = withReward.unlockMiningReward(Wei.valueOf(3), 2, TIME);
		assertEquals(Wei.valueOf(7), partiallyUnlocked.getBalance());
		assertEquals(Wei.valueOf(2), partiallyUnlocked.getLockedMiningReward());
		assertEquals(Wei.ZERO, partiallyUnlocked.getPendingMiningRewardCancellation());
		assertEquals(Wei.valueOf(5), partiallyUnlocked.getSpendableBalance());
		assertThrows(IllegalArgumentException.class,
				() -> partiallyUnlocked.unlockMiningReward(Wei.valueOf(3), 3, TIME));
	}

	@Test
	void burnsSpendableBeforeLockedAndConsumesCancellationDebtAtMaturity() {
		AccountBalanceStateImpl state = AccountBalanceStateImpl.builder()
				.version(AccountBalanceStateVersion.V2)
				.balance(Wei.valueOf(150))
				.lockedMiningReward(Wei.valueOf(100))
				.pendingMiningRewardCancellation(Wei.ZERO)
				.updatedAtBlockHeight(5)
				.updatedAtTimestamp(TIME)
				.build();

		AccountBalanceStateImpl burned = state.burnIncludingLockedMiningReward(Wei.valueOf(80), 6, TIME);
		assertEquals(Wei.valueOf(70), burned.getBalance());
		assertEquals(Wei.valueOf(70), burned.getLockedMiningReward());
		assertEquals(Wei.valueOf(30), burned.getPendingMiningRewardCancellation());
		assertEquals(Wei.ZERO, burned.getSpendableBalance());

		AccountBalanceStateImpl firstMaturity = burned.unlockMiningReward(Wei.valueOf(50), 10, TIME);
		assertEquals(Wei.valueOf(70), firstMaturity.getBalance());
		assertEquals(Wei.valueOf(50), firstMaturity.getLockedMiningReward());
		assertEquals(Wei.ZERO, firstMaturity.getPendingMiningRewardCancellation());
		assertEquals(Wei.valueOf(20), firstMaturity.getSpendableBalance());

		AccountBalanceStateImpl secondMaturity = firstMaturity.unlockMiningReward(Wei.valueOf(50), 20, TIME);
		assertEquals(Wei.valueOf(70), secondMaturity.getBalance());
		assertEquals(Wei.ZERO, secondMaturity.getLockedMiningReward());
		assertEquals(Wei.ZERO, secondMaturity.getPendingMiningRewardCancellation());
		assertEquals(Wei.valueOf(70), secondMaturity.getSpendableBalance());
	}

	@Test
	void fullLockedBurnIsNotResurrectedByLaterMaturity() {
		AccountBalanceStateImpl state = ((AccountBalanceStateImpl) AccountBalanceStateImpl.ZERO_V2)
				.creditLockedMiningReward(Wei.valueOf(25), 1, TIME);

		AccountBalanceStateImpl burned = state.burnIncludingLockedMiningReward(Wei.valueOf(25), 2, TIME);
		assertEquals(Wei.ZERO, burned.getBalance());
		assertEquals(Wei.ZERO, burned.getLockedMiningReward());
		assertEquals(Wei.valueOf(25), burned.getPendingMiningRewardCancellation());

		AccountBalanceStateImpl matured = burned.unlockMiningReward(Wei.valueOf(25), 10, TIME);
		assertEquals(Wei.ZERO, matured.getBalance());
		assertEquals(Wei.ZERO, matured.getLockedMiningReward());
		assertEquals(Wei.ZERO, matured.getPendingMiningRewardCancellation());
		assertEquals(Wei.ZERO, matured.getSpendableBalance());
	}

	@Test
	void spendableOnlyBurnPreservesLockDebtAndLegacyVersion() {
		AccountBalanceStateImpl v2 = AccountBalanceStateImpl.builder()
				.version(AccountBalanceStateVersion.V2)
				.balance(Wei.valueOf(50))
				.lockedMiningReward(Wei.valueOf(20))
				.pendingMiningRewardCancellation(Wei.valueOf(7))
				.updatedAtBlockHeight(1)
				.updatedAtTimestamp(TIME)
				.build();

		AccountBalanceStateImpl burnedV2 = v2.burnIncludingLockedMiningReward(Wei.valueOf(10), 2, TIME);
		assertEquals(Wei.valueOf(40), burnedV2.getBalance());
		assertEquals(Wei.valueOf(20), burnedV2.getLockedMiningReward());
		assertEquals(Wei.valueOf(7), burnedV2.getPendingMiningRewardCancellation());

		AccountBalanceStateImpl legacy = AccountBalanceStateImpl.builder()
				.version(AccountBalanceStateVersion.V1)
				.balance(Wei.valueOf(10))
				.updatedAtBlockHeight(1)
				.updatedAtTimestamp(TIME)
				.build();
		AccountBalanceStateImpl burnedLegacy = legacy.burnIncludingLockedMiningReward(Wei.valueOf(4), 2, TIME);
		assertEquals(AccountBalanceStateVersion.V1, burnedLegacy.getVersion());
		assertEquals(Wei.valueOf(6), burnedLegacy.getBalance());
		assertEquals(Wei.ZERO, burnedLegacy.getPendingMiningRewardCancellation());
	}

	@Test
	void rejectsInvalidV2StateAndMalformedFieldCounts() {
		AccountBalanceState invalid = AccountBalanceStateImpl.builder()
				.version(AccountBalanceStateVersion.V2)
				.balance(Wei.valueOf(3))
				.lockedMiningReward(Wei.valueOf(4))
				.pendingMiningRewardCancellation(Wei.ZERO)
				.updatedAtBlockHeight(1)
				.updatedAtTimestamp(TIME)
				.build();
		assertThrows(CryptoJFailedException.class, () -> AccountBalanceStateEncoder.INSTANCE.encode(invalid));
		assertThrows(RuntimeException.class,
				() -> AccountBalanceStateDecoder.INSTANCE.decode(Bytes.fromHexString("0xc502030104")));
	}
}
