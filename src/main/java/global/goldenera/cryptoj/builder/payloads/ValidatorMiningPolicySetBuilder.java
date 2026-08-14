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
package global.goldenera.cryptoj.builder.payloads;

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.MiningConsensusRules;
import global.goldenera.cryptoj.common.payloads.bip.TxBipValidatorMiningPolicySetPayloadImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.enums.TxType;
import lombok.NonNull;

public class ValidatorMiningPolicySetBuilder {

	private final TxBuilder parent;
	private Address validatorAddress;
	private MiningLimitMode miningLimitMode;
	private Long maxMiningShareBps;

	public ValidatorMiningPolicySetBuilder(TxBuilder parent) {
		this.parent = parent;
	}

	public ValidatorMiningPolicySetBuilder validator(@NonNull Address validatorAddress) {
		this.validatorAddress = validatorAddress;
		return this;
	}

	public ValidatorMiningPolicySetBuilder miningPolicy(@NonNull MiningLimitMode mode, long maxMiningShareBps) {
		MiningConsensusRules.validatePolicy(mode, maxMiningShareBps);
		this.miningLimitMode = mode;
		this.maxMiningShareBps = maxMiningShareBps;
		return this;
	}

	public TxBuilder done() {
		if (validatorAddress == null || miningLimitMode == null || maxMiningShareBps == null) {
			throw new IllegalStateException("Validator and mining policy are required");
		}
		return parent.type(TxType.BIP_CREATE)
				.payload(TxBipValidatorMiningPolicySetPayloadImpl.builder()
						.validatorAddress(validatorAddress)
						.miningLimitMode(miningLimitMode)
						.maxMiningShareBps(maxMiningShareBps)
						.build());
	}
}
