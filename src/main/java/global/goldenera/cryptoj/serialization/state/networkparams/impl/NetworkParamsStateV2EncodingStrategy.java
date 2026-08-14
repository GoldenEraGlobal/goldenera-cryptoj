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
package global.goldenera.cryptoj.serialization.state.networkparams.impl;

import global.goldenera.cryptoj.common.MiningConsensusRules;
import global.goldenera.cryptoj.common.state.NetworkParamsState;
import global.goldenera.cryptoj.serialization.state.networkparams.NetworkParamsStateEncodingStrategy;
import global.goldenera.rlp.RLPOutput;

public class NetworkParamsStateV2EncodingStrategy implements NetworkParamsStateEncodingStrategy {

	private final NetworkParamsStateV1EncodingStrategy legacyFields = new NetworkParamsStateV1EncodingStrategy();

	@Override
	public void encode(RLPOutput out, NetworkParamsState state) {
		MiningConsensusRules.validateWindowSize(state.getValidatorMiningWindowBlocks());
		if (state.getCurrentValidatorCount() == 0 && state.getCurrentUnlimitedValidatorCount() != 0) {
			throw new IllegalArgumentException(
					"A zero-validator set requires currentUnlimitedValidatorCount = 0");
		}
		if (state.getCurrentValidatorCount() != 0
				&& (state.getCurrentUnlimitedValidatorCount() < 1
						|| state.getCurrentUnlimitedValidatorCount() > state.getCurrentValidatorCount())) {
			throw new IllegalArgumentException(
					"A non-empty validator set requires currentUnlimitedValidatorCount in range 1..currentValidatorCount");
		}
		if (state.getLimitedValidatorMiningSharesBps().size()
				!= state.getCurrentValidatorCount() - state.getCurrentUnlimitedValidatorCount()) {
			throw new IllegalArgumentException("LIMITED validator policy summary is inconsistent");
		}
		long previous = 0;
		for (long bps : state.getLimitedValidatorMiningSharesBps()) {
			MiningConsensusRules.validateLimitedPolicyForWindow(state.getValidatorMiningWindowBlocks(), bps);
			if (bps < previous) {
				throw new IllegalArgumentException("LIMITED validator policy summary must be sorted");
			}
			previous = bps;
		}
		legacyFields.encode(out, state);
		out.writeLongScalar(state.getValidatorMiningWindowBlocks());
		out.writeLongScalar(state.getCurrentUnlimitedValidatorCount());
		out.writeList(state.getLimitedValidatorMiningSharesBps(), (bps, listOut) -> listOut.writeLongScalar(bps));
	}
}
