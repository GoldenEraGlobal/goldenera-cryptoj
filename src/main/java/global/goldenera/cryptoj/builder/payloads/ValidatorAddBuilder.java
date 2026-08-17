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
import global.goldenera.cryptoj.common.payloads.bip.TxBipValidatorAddPayloadImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.enums.TxPayloadVersion;
import global.goldenera.cryptoj.enums.TxType;
import lombok.NonNull;

/**
 * Fluent builder for Validator Add payloads.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.addValidator()
 * 		.validator(validatorAddress)
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.nonce(1L)
 * 		.sign(myKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 */
public class ValidatorAddBuilder {

	private final TxBuilder parent;
	private Address validator;
	private MiningLimitMode miningLimitMode;
	private Long maxMiningShareBps;
	private boolean legacyV1;

	public ValidatorAddBuilder(TxBuilder parent) {
		this.parent = parent;
	}

	/**
	 * Sets the address to add as a validator.
	 * 
	 * @param validator the validator address
	 * @return this builder for chaining
	 */
	public ValidatorAddBuilder validator(@NonNull Address validator) {
		this.validator = validator;
		return this;
	}

	public ValidatorAddBuilder miningPolicy(@NonNull MiningLimitMode mode, long maxMiningShareBps) {
		if (legacyV1) {
			throw new IllegalStateException("Legacy V1 validator add payload cannot contain a mining policy");
		}
		MiningConsensusRules.validatePolicy(mode, maxMiningShareBps);
		this.miningLimitMode = mode;
		this.maxMiningShareBps = maxMiningShareBps;
		return this;
	}

	/** Explicitly opts into the historical, address-only implicit V1 wire format. */
	public ValidatorAddBuilder legacyV1() {
		if (miningLimitMode != null || maxMiningShareBps != null) {
			throw new IllegalStateException("Legacy V1 validator add payload cannot contain a mining policy");
		}
		this.legacyV1 = true;
		return this;
	}

	/**
	 * Completes the validator add payload configuration and returns to the parent
	 * builder.
	 * 
	 * @return parent TxBuilder for continued configuration
	 */
	public TxBuilder done() {
		if (validator == null) {
			throw new IllegalStateException("Validator address is required");
		}
		if (!legacyV1 && (miningLimitMode == null || maxMiningShareBps == null)) {
			throw new IllegalStateException("Validator add V2 requires an explicit mining policy");
		}
		TxPayloadVersion payloadVersion = legacyV1 ? TxPayloadVersion.V1 : TxPayloadVersion.V2;
		TxBipValidatorAddPayloadImpl payload = TxBipValidatorAddPayloadImpl.builder()
				.payloadVersion(payloadVersion)
				.address(validator)
				.miningLimitMode(miningLimitMode)
				.maxMiningShareBps(maxMiningShareBps)
				.build();

		return parent.type(TxType.BIP_CREATE)
				.payload(payload);
	}
}
