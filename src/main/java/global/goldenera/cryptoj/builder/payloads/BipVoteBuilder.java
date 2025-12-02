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
import global.goldenera.cryptoj.common.payloads.bip.TxBipVotePayloadImpl;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.enums.BipVoteType;
import global.goldenera.cryptoj.enums.TxType;
import lombok.NonNull;

/**
 * Fluent builder for BIP Vote payloads.
 * 
 * <p>
 * Example usage - Approve:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.vote()
 * 		.approve(bipProposalHash)
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.sender(authorityAddress)
 * 		.nonce(1L)
 * 		.sign(myKey);
 * }</pre>
 * 
 * <p>
 * Example usage - Disapprove:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.vote()
 * 		.disapprove(bipProposalHash)
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.sender(authorityAddress)
 * 		.nonce(1L)
 * 		.sign(myKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 */
public class BipVoteBuilder {

	private final TxBuilder parent;
	private Hash referenceHash;
	private BipVoteType voteType;

	public BipVoteBuilder(TxBuilder parent) {
		this.parent = parent;
	}

	/**
	 * Vote to approve the BIP proposal.
	 * 
	 * @param proposalHash hash of the BIP_CREATE proposal to approve
	 * @return this builder for chaining
	 */
	public BipVoteBuilder approve(@NonNull Hash proposalHash) {
		this.referenceHash = proposalHash;
		this.voteType = BipVoteType.APPROVAL;
		return this;
	}

	/**
	 * Vote to disapprove the BIP proposal.
	 * 
	 * @param proposalHash hash of the BIP_CREATE proposal to disapprove
	 * @return this builder for chaining
	 */
	public BipVoteBuilder disapprove(@NonNull Hash proposalHash) {
		this.referenceHash = proposalHash;
		this.voteType = BipVoteType.DISAPPROVAL;
		return this;
	}

	/**
	 * Completes the BIP vote configuration and returns to the parent builder.
	 * 
	 * @return parent TxBuilder for continued configuration
	 */
	public TxBuilder done() {
		TxBipVotePayloadImpl payload = TxBipVotePayloadImpl.builder()
				.type(voteType)
				.build();

		return parent.type(TxType.BIP_VOTE)
				.payload(payload)
				.referenceHash(referenceHash);
	}
}
