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

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.payloads.bip.TxBipTokenBurnPayloadImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.TxType;
import lombok.NonNull;

/**
 * Fluent builder for Token Burn payloads (BIP_CREATE transaction).
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.tokenBurn()
 * 		.token(tokenAddress)
 * 		.from(holderAddress)
 * 		.amount(Amounts.tokens(100))
 * 		.minerFee(Amounts.tokensDecimal("0.001"))
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.sender(myAddress)
 * 		.nonce(1L)
 * 		.fee(Amounts.tokensDecimal("0.005"))
 * 		.sign(myKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 */
public class TokenBurnBuilder {

	private final TxBuilder parent;
	private Address tokenAddress;
	private Address sender;
	private Wei amount;

	public TokenBurnBuilder(TxBuilder parent) {
		this.parent = parent;
		this.amount = Wei.ZERO;
	}

	/**
	 * Sets the token contract address to burn from.
	 * 
	 * @param tokenAddress token contract address
	 * @return this builder for chaining
	 */
	public TokenBurnBuilder token(@NonNull Address tokenAddress) {
		this.tokenAddress = tokenAddress;
		return this;
	}

	/**
	 * Sets the address from which tokens will be burned.
	 * 
	 * @param sender holder's address
	 * @return this builder for chaining
	 */
	public TokenBurnBuilder from(@NonNull Address sender) {
		this.sender = sender;
		return this;
	}

	/**
	 * Sets the amount of tokens to burn.
	 * 
	 * @param amount amount in Wei
	 * @return this builder for chaining
	 */
	public TokenBurnBuilder amount(@NonNull Wei amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Completes the token burn payload configuration and returns to the parent
	 * builder.
	 * Creates a BIP_CREATE transaction with token burn payload.
	 * 
	 * @return parent TxBuilder for continued configuration
	 */
	public TxBuilder done() {
		TxBipTokenBurnPayloadImpl payload = TxBipTokenBurnPayloadImpl.builder()
				.tokenAddress(tokenAddress)
				.sender(sender)
				.amount(amount)
				.build();

		return parent.type(TxType.BIP_CREATE)
				.payload(payload);
	}
}
