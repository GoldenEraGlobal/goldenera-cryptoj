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
package global.goldenera.cryptoj.common.state;

import java.time.Instant;

import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.enums.state.ValidatorStateVersion;

public interface ValidatorState {
	ValidatorStateVersion getVersion();

	long getCreatedAtBlockHeight();

	Instant getCreatedAtTimestamp();

	Hash getOriginTxHash();

	/** V1 states resolve to the consensus legacy default UNLIMITED policy. */
	default MiningLimitMode getMiningLimitMode() {
		return MiningLimitMode.UNLIMITED;
	}

	/** V1 states resolve to the consensus legacy default share value 0. */
	default long getMaxMiningShareBps() {
		return 0;
	}

	default Hash getPolicyUpdatedByTxHash() {
		return null;
	}

	default long getPolicyUpdatedAtBlockHeight() {
		return Long.MIN_VALUE;
	}

	default Instant getPolicyUpdatedAtTimestamp() {
		return null;
	}

	/**
	 * Checks if this address is registered as a validator.
	 */
	default boolean exists() {
		return getCreatedAtBlockHeight() != Long.MIN_VALUE;
	}
}
