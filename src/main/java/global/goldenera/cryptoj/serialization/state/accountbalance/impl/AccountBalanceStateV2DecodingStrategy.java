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
package global.goldenera.cryptoj.serialization.state.accountbalance.impl;

import java.time.Instant;

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.state.AccountBalanceState;
import global.goldenera.cryptoj.common.state.impl.AccountBalanceStateImpl;
import global.goldenera.cryptoj.enums.state.AccountBalanceStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.state.accountbalance.AccountBalanceStateDecodingStrategy;
import global.goldenera.rlp.RLPInput;

public class AccountBalanceStateV2DecodingStrategy implements AccountBalanceStateDecodingStrategy {

	@Override
	public AccountBalanceState decode(RLPInput input) {
		Wei balance = Wei.valueOf(input.readBigIntegerScalar());
		long updatedAtBlockHeight = input.readLongScalar();
		Long updatedAtTimestampMillis = input.readOptionalLongScalar();
		Instant updatedAtTimestamp = updatedAtTimestampMillis == null
				? null
				: Instant.ofEpochMilli(updatedAtTimestampMillis);
		Wei lockedMiningReward = Wei.valueOf(input.readBigIntegerScalar());
		if (lockedMiningReward.compareTo(balance) > 0) {
			throw new CryptoJFailedException("Locked mining reward cannot exceed balance");
		}
		Wei pendingMiningRewardCancellation = Wei.valueOf(input.readBigIntegerScalar());
		return AccountBalanceStateImpl.builder()
				.version(AccountBalanceStateVersion.V2)
				.balance(balance)
				.lockedMiningReward(lockedMiningReward)
				.pendingMiningRewardCancellation(pendingMiningRewardCancellation)
				.updatedAtBlockHeight(updatedAtBlockHeight)
				.updatedAtTimestamp(updatedAtTimestamp)
				.build();
	}
}
