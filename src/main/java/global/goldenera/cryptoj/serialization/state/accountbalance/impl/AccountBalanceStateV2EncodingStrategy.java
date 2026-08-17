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

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.state.AccountBalanceState;
import global.goldenera.cryptoj.serialization.state.accountbalance.AccountBalanceStateEncodingStrategy;
import global.goldenera.rlp.RLPOutput;

public class AccountBalanceStateV2EncodingStrategy implements AccountBalanceStateEncodingStrategy {

	private final AccountBalanceStateV1EncodingStrategy legacyFields = new AccountBalanceStateV1EncodingStrategy();

	@Override
	public void encode(RLPOutput out, AccountBalanceState state) {
		Wei locked = state.getLockedMiningReward();
		if (locked == null || locked.compareTo(Wei.ZERO) < 0 || locked.compareTo(state.getBalance()) > 0) {
			throw new IllegalArgumentException("Locked mining reward must be in range 0..balance");
		}
		Wei pendingCancellation = state.getPendingMiningRewardCancellation();
		if (pendingCancellation == null || pendingCancellation.compareTo(Wei.ZERO) < 0) {
			throw new IllegalArgumentException("Pending mining reward cancellation cannot be negative");
		}
		legacyFields.encode(out, state);
		out.writeBigIntegerScalar(locked.toBigInteger());
		out.writeBigIntegerScalar(pendingCancellation.toBigInteger());
	}
}
