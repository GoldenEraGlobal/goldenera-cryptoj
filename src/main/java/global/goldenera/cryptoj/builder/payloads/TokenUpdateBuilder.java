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
import global.goldenera.cryptoj.common.payloads.bip.TxBipTokenUpdatePayloadImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.TxType;
import lombok.NonNull;

/**
 * Fluent builder for Token Update payloads.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * Tx tx = TxBuilder.create()
 * 		.tokenUpdate()
 * 		.token(tokenAddress)
 * 		.name("NewTokenName") // Optional
 * 		.symbol("NTN") // Optional
 * 		.website("https://newsite.com") // Optional
 * 		.logo("https://newsite.com/logo.png") // Optional
 * 		.done()
 * 		.network(Network.MAINNET)
 * 		.sender(ownerAddress)
 * 		.nonce(1L)
 * 		.sign(myKey);
 * }</pre>
 * 
 * @author GoldenEra CryptoJ Team
 */
public class TokenUpdateBuilder {

	private final TxBuilder parent;
	private Address tokenAddress;
	private String name;
	private String symbol;
	private String website;
	private String logo;

	public TokenUpdateBuilder(TxBuilder parent) {
		this.parent = parent;
	}

	/**
	 * Sets the token address to update.
	 * 
	 * @param tokenAddress token contract address
	 * @return this builder for chaining
	 */
	public TokenUpdateBuilder token(@NonNull Address tokenAddress) {
		this.tokenAddress = tokenAddress;
		return this;
	}

	/**
	 * Sets the new token name (optional).
	 * 
	 * @param name new token name
	 * @return this builder for chaining
	 */
	public TokenUpdateBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets the new token symbol (optional).
	 * 
	 * @param symbol new token symbol * @return this builder for chaining
	 */
	public TokenUpdateBuilder symbol(String symbol) {
		this.symbol = symbol;
		return this;
	}

	/**
	 * Sets the new website URL (optional).
	 * 
	 * @param website new website URL
	 * @return this builder for chaining
	 */
	public TokenUpdateBuilder website(String website) {
		this.website = website;
		return this;
	}

	/**
	 * Sets the new logo URL (optional).
	 * 
	 * @param logo new logo URL
	 * @return this builder for chaining
	 */
	public TokenUpdateBuilder logo(String logo) {
		this.logo = logo;
		return this;
	}

	/**
	 * Completes the token update payload configuration and returns to the parent
	 * builder.
	 * 
	 * @return parent TxBuilder for continued configuration
	 */
	public TxBuilder done() {
		TxBipTokenUpdatePayloadImpl payload = TxBipTokenUpdatePayloadImpl.builder()
				.tokenAddress(tokenAddress)
				.name(name)
				.smallestUnitName(symbol)
				.websiteUrl(website)
				.logoUrl(logo)
				.build();

		return parent.type(TxType.BIP_CREATE)
				.payload(payload);
	}
}
