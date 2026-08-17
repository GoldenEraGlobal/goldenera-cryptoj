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
package global.goldenera.cryptoj.common;

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.state.AccountBalanceState;
import global.goldenera.cryptoj.enums.state.AccountBalanceStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;

public final class AccountBalanceStateValidation {

	private AccountBalanceStateValidation() {
	}

	public static void validate(AccountBalanceState state) {
		if (state == null || state.getVersion() == null || state.getBalance() == null) {
			throw new CryptoJFailedException("Account balance state, version and balance cannot be null");
		}
		if (state.getBalance().compareTo(Wei.ZERO) < 0) {
			throw new CryptoJFailedException("Account balance cannot be negative");
		}

		Wei locked = state.getLockedMiningReward();
		Wei pendingCancellation = state.getPendingMiningRewardCancellation();
		if (locked == null || pendingCancellation == null
				|| locked.compareTo(Wei.ZERO) < 0
				|| pendingCancellation.compareTo(Wei.ZERO) < 0) {
			throw new CryptoJFailedException("Mining reward lock and cancellation cannot be null or negative");
		}
		if (state.getVersion() == AccountBalanceStateVersion.V1
				&& (!locked.isZero() || !pendingCancellation.isZero())) {
			throw new CryptoJFailedException("AccountBalanceState V1 cannot contain mining reward fields");
		}
		if (locked.compareTo(state.getBalance()) > 0) {
			throw new CryptoJFailedException("Locked mining reward cannot exceed balance");
		}
	}
}
