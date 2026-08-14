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

import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;

/** Canonical consensus limits shared by payload, state and node validation. */
public final class MiningConsensusRules {

	public static final long BASIS_POINTS_DENOMINATOR = 10_000;
	public static final long MAX_VALIDATOR_MINING_SHARE_BPS = 4_000;
	public static final long MIN_VALIDATOR_MINING_WINDOW_BLOCKS = 100;
	public static final long MAX_VALIDATOR_MINING_WINDOW_BLOCKS = 10_000;

	private MiningConsensusRules() {
	}

	public static void validatePolicy(MiningLimitMode mode, long maxMiningShareBps) {
		if (mode == null) {
			throw new CryptoJFailedException("Mining limit mode cannot be null");
		}
		if (mode == MiningLimitMode.UNLIMITED && maxMiningShareBps != 0) {
			throw new CryptoJFailedException("UNLIMITED mining policy requires maxMiningShareBps = 0");
		}
		if (mode == MiningLimitMode.LIMITED
				&& (maxMiningShareBps < 1 || maxMiningShareBps > MAX_VALIDATOR_MINING_SHARE_BPS)) {
			throw new CryptoJFailedException(
					"LIMITED mining policy requires maxMiningShareBps in range 1.."
							+ MAX_VALIDATOR_MINING_SHARE_BPS);
		}
	}

	public static void validateWindowSize(long validatorMiningWindowBlocks) {
		if (validatorMiningWindowBlocks < MIN_VALIDATOR_MINING_WINDOW_BLOCKS
				|| validatorMiningWindowBlocks > MAX_VALIDATOR_MINING_WINDOW_BLOCKS) {
			throw new CryptoJFailedException("validatorMiningWindowBlocks must be in range "
					+ MIN_VALIDATOR_MINING_WINDOW_BLOCKS + ".." + MAX_VALIDATOR_MINING_WINDOW_BLOCKS);
		}
	}

	public static void validateLimitedPolicyForWindow(long validatorMiningWindowBlocks, long maxMiningShareBps) {
		validateWindowSize(validatorMiningWindowBlocks);
		validatePolicy(MiningLimitMode.LIMITED, maxMiningShareBps);
		if (validatorMiningWindowBlocks * maxMiningShareBps < BASIS_POINTS_DENOMINATOR) {
			throw new CryptoJFailedException(
					"LIMITED mining policy must allow at least one block in the configured window");
		}
	}
}
