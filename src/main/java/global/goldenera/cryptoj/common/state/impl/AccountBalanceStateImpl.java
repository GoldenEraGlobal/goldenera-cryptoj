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
package global.goldenera.cryptoj.common.state.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.Instant;

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.state.AccountBalanceState;
import global.goldenera.cryptoj.enums.state.AccountBalanceStateVersion;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AccountBalanceStateImpl implements AccountBalanceState {

    public static final AccountBalanceState ZERO = AccountBalanceStateImpl.builder()
            .version(AccountBalanceStateVersion.V1)
            .balance(Wei.ZERO)
            .updatedAtBlockHeight(Long.MIN_VALUE)
            .updatedAtTimestamp(Instant.EPOCH)
            .build();
    public static final AccountBalanceState ZERO_V2 = AccountBalanceStateImpl.builder()
            .version(AccountBalanceStateVersion.V2)
            .balance(Wei.ZERO)
            .lockedMiningReward(Wei.ZERO)
            .pendingMiningRewardCancellation(Wei.ZERO)
            .updatedAtBlockHeight(Long.MIN_VALUE)
            .updatedAtTimestamp(Instant.EPOCH)
            .build();

    AccountBalanceStateVersion version;
    Wei balance;
    Wei lockedMiningReward;
    @Builder.Default
    Wei pendingMiningRewardCancellation;
    long updatedAtBlockHeight;
    Instant updatedAtTimestamp;

    public AccountBalanceStateImpl debit(Wei amount, long blockHeight, Instant time) {
        checkArgument(amount.compareTo(Wei.ZERO) >= 0, "Cannot debit negative amount");
        checkArgument(getSpendableBalance().compareTo(amount) >= 0, "Insufficient spendable funds");

        return this.toBuilder()
                .balance(this.balance.subtractExact(amount))
                .updatedAtBlockHeight(blockHeight)
                .updatedAtTimestamp(time)
                .build();
    }

    @Override
    public Wei getLockedMiningReward() {
        if (version == AccountBalanceStateVersion.V1 || lockedMiningReward == null) {
            return Wei.ZERO;
        }
        return lockedMiningReward;
    }

    @Override
    public Wei getPendingMiningRewardCancellation() {
        if (version == AccountBalanceStateVersion.V1 || pendingMiningRewardCancellation == null) {
            return Wei.ZERO;
        }
        return pendingMiningRewardCancellation;
    }

    public AccountBalanceStateImpl upgradeToV2() {
        if (version == AccountBalanceStateVersion.V2) {
            return this;
        }
        return this.toBuilder()
                .version(AccountBalanceStateVersion.V2)
                .lockedMiningReward(Wei.ZERO)
                .pendingMiningRewardCancellation(Wei.ZERO)
                .build();
    }

    public AccountBalanceStateImpl credit(Wei amount, long blockHeight, Instant time) {
        checkArgument(amount.compareTo(Wei.ZERO) >= 0, "Cannot credit negative amount");

        return this.toBuilder()
                .balance(this.balance.add(amount))
                .updatedAtBlockHeight(blockHeight)
                .updatedAtTimestamp(time)
                .build();
    }

    public AccountBalanceStateImpl creditLockedMiningReward(Wei amount, long blockHeight, Instant time) {
        checkArgument(amount.compareTo(Wei.ZERO) >= 0, "Cannot credit negative mining reward");
        Wei currentLocked = getLockedMiningReward();
        return this.toBuilder()
                .version(AccountBalanceStateVersion.V2)
                .balance(this.balance.add(amount))
                .lockedMiningReward(currentLocked.add(amount))
                .pendingMiningRewardCancellation(getPendingMiningRewardCancellation())
                .updatedAtBlockHeight(blockHeight)
                .updatedAtTimestamp(time)
                .build();
    }

    public AccountBalanceStateImpl burnIncludingLockedMiningReward(Wei amount, long blockHeight, Instant time) {
        checkArgument(amount.compareTo(Wei.ZERO) >= 0, "Cannot burn negative amount");
        checkArgument(balance.compareTo(amount) >= 0, "Insufficient total funds");

        Wei spendable = getSpendableBalance();
        if (spendable.compareTo(amount) >= 0) {
            return debit(amount, blockHeight, time);
        }

        Wei lockedBurned = amount.subtractExact(spendable);
        Wei currentLocked = getLockedMiningReward();
        checkArgument(currentLocked.compareTo(lockedBurned) >= 0,
                "Cannot burn more than total mining reward balance");
        return this.toBuilder()
                .version(AccountBalanceStateVersion.V2)
                .balance(balance.subtractExact(amount))
                .lockedMiningReward(currentLocked.subtractExact(lockedBurned))
                .pendingMiningRewardCancellation(
                        getPendingMiningRewardCancellation().addExact(lockedBurned))
                .updatedAtBlockHeight(blockHeight)
                .updatedAtTimestamp(time)
                .build();
    }

    public AccountBalanceStateImpl unlockMiningReward(Wei amount, long blockHeight, Instant time) {
        checkArgument(amount.compareTo(Wei.ZERO) >= 0, "Cannot unlock negative mining reward");
        Wei currentLocked = getLockedMiningReward();
        Wei pendingCancellation = getPendingMiningRewardCancellation();
        Wei cancellationApplied = pendingCancellation.compareTo(amount) < 0
                ? pendingCancellation
                : amount;
        Wei amountToUnlock = amount.subtractExact(cancellationApplied);
        checkArgument(currentLocked.compareTo(amountToUnlock) >= 0,
                "Cannot mature more than locked and cancelled mining reward");
        return this.toBuilder()
                .version(AccountBalanceStateVersion.V2)
                .lockedMiningReward(currentLocked.subtractExact(amountToUnlock))
                .pendingMiningRewardCancellation(pendingCancellation.subtractExact(cancellationApplied))
                .updatedAtBlockHeight(blockHeight)
                .updatedAtTimestamp(time)
                .build();
    }
}
