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

import java.math.BigInteger;

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.payloads.bip.TxBipNetworkParamsSetPayloadImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.TxType;

/**
 * Fluent builder for Network Parameters Set payloads.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.setNetworkParams()
 * 		.blockReward(Wei.fromEth(5))
 * 		.targetMiningTime(10000L) // 10 seconds in ms
 * 		.asertHalfLife(144L) // blocks
 * 		.minDifficulty(BigInteger.valueOf(1000))
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.sender(authorityAddress)
 * 		.nonce(1L)
 * 		.sign(myKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 */
public class NetworkParamsBuilder {

	private final TxBuilder parent;
	private Wei blockReward;
	private Address blockRewardPoolAddress;
	private Long targetMiningTimeMs;
	private Long asertHalfLifeBlocks;
	private BigInteger minDifficulty;
	private Wei minTxBaseFee;
	private Wei minTxByteFee;

	public NetworkParamsBuilder(TxBuilder parent) {
		this.parent = parent;
	}

	/**
	 * Sets the block reward (optional).
	 * 
	 * @param blockReward block mining reward in Wei
	 * @return this builder for chaining
	 */
	public NetworkParamsBuilder blockReward(Wei blockReward) {
		this.blockReward = blockReward;
		return this;
	}

	/**
	 * Sets the block reward pool address (optional).
	 * 
	 * @param blockRewardPoolAddress block reward pool address
	 * @return this builder for chaining
	 */
	public NetworkParamsBuilder blockRewardPoolAddress(Address blockRewardPoolAddress) {
		this.blockRewardPoolAddress = blockRewardPoolAddress;
		return this;
	}

	/**
	 * Sets the target mining time (optional).
	 * 
	 * @param targetMiningTimeMs target time between blocks in milliseconds
	 * @return this builder for chaining
	 */
	public NetworkParamsBuilder targetMiningTime(Long targetMiningTimeMs) {
		this.targetMiningTimeMs = targetMiningTimeMs;
		return this;
	}

	/**
	 * Sets the ASERT half-life in blocks (optional).
	 * 
	 * @param asertHalfLifeBlocks ASERT difficulty adjustment half-life
	 * @return this builder for chaining
	 */
	public NetworkParamsBuilder asertHalfLife(Long asertHalfLifeBlocks) {
		this.asertHalfLifeBlocks = asertHalfLifeBlocks;
		return this;
	}

	/**
	 * Sets the minimum difficulty (optional).
	 * 
	 * @param minDifficulty minimum mining difficulty
	 * @return this builder for chaining
	 */
	public NetworkParamsBuilder minDifficulty(BigInteger minDifficulty) {
		this.minDifficulty = minDifficulty;
		return this;
	}

	public NetworkParamsBuilder minTxBaseFee(Wei minTxBaseFee) {
		this.minTxBaseFee = minTxBaseFee;
		return this;
	}

	public NetworkParamsBuilder minTxByteFee(Wei minTxByteFee) {
		this.minTxByteFee = minTxByteFee;
		return this;
	}

	/**
	 * Completes the network params payload configuration and returns to the parent
	 * builder.
	 * 
	 * @return parent TxBuilder for continued configuration
	 */
	public TxBuilder done() {
		TxBipNetworkParamsSetPayloadImpl payload = TxBipNetworkParamsSetPayloadImpl.builder()
				.blockReward(blockReward)
				.blockRewardPoolAddress(blockRewardPoolAddress)
				.targetMiningTimeMs(targetMiningTimeMs)
				.asertHalfLifeBlocks(asertHalfLifeBlocks)
				.minDifficulty(minDifficulty)
				.minTxBaseFee(minTxBaseFee)
				.minTxByteFee(minTxByteFee)
				.build();

		return parent.type(TxType.BIP_CREATE)
				.payload(payload);
	}
}
